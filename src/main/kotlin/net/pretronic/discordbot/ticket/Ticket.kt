package net.pretronic.discordbot.ticket

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel
import net.pretronic.databasequery.api.dsl.insert
import net.pretronic.databasequery.api.dsl.update
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.extensions.sendMessageKey
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.message.language.Language
import net.pretronic.discordbot.ticket.state.TicketState
import net.pretronic.discordbot.ticket.topic.TicketTopic
import net.pretronic.discordbot.ticket.topic.TicketTopicContent
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.type.DocumentFileType
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

class Ticket(val id: Int,
             val discordChannelId: Long,
             state: TicketState,
             val language: Language,
             participants: MutableCollection<TicketParticipant>,
             val discordControlMessageId: Long,
             val topics: MutableCollection<TicketTopicContent>,
             topicChooseMessageId: Long?,
             val creationTime: Long,
             lastNotOpenedNotifyTime: Long,
             val ticketNotOpenedNotificationMessages: MutableCollection<Long>) {

    constructor(id: Int,
                discordChannelId: Long,
                state: TicketState,
                language: Language,
                creatorId: Long,
                discordControlMessageId: Long,
                creationTime: Long,
                lastNotOpenedNotifyTime: Long,
                ticketNotOpenedNotificationMessages: MutableCollection<Long>):
            this(id, discordChannelId, state, language,
                    mutableListOf(TicketParticipant(creatorId, TicketParticipantRole.CREATOR)),
                    discordControlMessageId, ArrayList(), null, creationTime, lastNotOpenedNotifyTime, ticketNotOpenedNotificationMessages)

    var state: TicketState = state
        set(value) {
            DiscordBot.INSTANCE.storage.ticket.update {
                set("State", value.name)
                where("Id", id)
            }.executeAsync()
            value.handleChange(field, this)
            field = value
        }
    val participants: MutableCollection<TicketParticipant> = participants
    val creator: TicketParticipant
        get() = participants.first { it.role == TicketParticipantRole.CREATOR }
    var topicChooseMessageId = topicChooseMessageId
        set(value) {
            field = value
            DiscordBot.INSTANCE.storage.ticket.update {
                set("TopicChooseMessageId", value)
                where("Id", id)
            }.executeAsync()
        }
    val discordChannel: TextChannel?
        get() = DiscordBot.INSTANCE.jda.getTextChannelById(discordChannelId)

    var lastNotOpenedNotifyTime: Long = lastNotOpenedNotifyTime
        set(value) {
            field = value
            DiscordBot.INSTANCE.storage.ticket.update {
                set("LastNotOpenedNotifyTime", value)
                where("Id", id)
            }.executeAsync()
        }

    fun addParticipant(participant: TicketParticipant) {
        this.participants.add(participant)
        DiscordBot.INSTANCE.storage.ticketParticipants.insert {
            set("TicketId", id)
            set("DiscordUserId", participant.discordId)
            set("Role", participant.role)
        }.execute()
    }

    fun addTopic(ticketTopic: TicketTopic) {
        this.topics.add(TicketTopicContent(this, ticketTopic, arrayListOf()))
        DiscordBot.INSTANCE.storage.ticket.update {
            set("Topics", DocumentFileType.JSON.writer.write(Document.newDocument(topics), false))
            where("Id", id)
        }.execute()
    }

    fun isCreator(discordId: Long): Boolean {
        return participants.firstOrNull { it.role == TicketParticipantRole.CREATOR && it.discordId == discordId } != null
    }

    fun close(oldState: TicketState?) {
        generateLog().thenAccept { pasteKey ->
            if(oldState == TicketState.OPEN) logTicketAction(TicketAction.CLOSE, pasteKey)

            DiscordBot.INSTANCE.jda.getTextChannelById(this.discordChannelId)?.delete()?.queue()
            creator.asMember()?.user?.openPrivateChannel()?.queue ({ channel ->
                channel.sendMessageKey(Messages.DISCORD_TICKET_CLOSED_SELF, language, mapOf(Pair("pasteKey", pasteKey?:"No log"))).queue()
            }, {
                //Ignored
            })
        }

    }

    fun logTicketAction(ticketAction: TicketAction, pasteKey: String?) {
        val builder = EmbedBuilder()
        builder.setTitle(ticketAction.title)

        creator.asMember()?.let {
            builder.setThumbnail(it.user.effectiveAvatarUrl)
            builder.addField("Created By", it.asMention, true)
        }?:builder.addField("Created By", "User Left The Server", true)

        builder.addField("Projects", topics.joinToString(", ") { it.topic.name }, true)
        if(pasteKey != null) builder.addField("Transcript", pasteKey, true)
        builder.setColor(ticketAction.color)

        DiscordBot.INSTANCE.getPretronicGuild().getTextChannelById(DiscordBot.INSTANCE.config.ticketLogChannelId)?.sendMessage(builder.build())?.queue()
    }

    fun clearTicketNotOpenedNotifications(channel: TextChannel) {
        this.ticketNotOpenedNotificationMessages.forEach {
            channel.deleteMessageById(it).queue()
        }
        this.ticketNotOpenedNotificationMessages.clear()
        DiscordBot.INSTANCE.storage.ticket.update {
            set("TicketNotOpenedNotificationMessages", "{}")
            where("Id", id)
        }.executeAsync()
    }

    private fun generateLog(): CompletableFuture<String?> {
        val future = CompletableFuture<String?>()
        DiscordBot.INSTANCE.getPretronicGuild().getTextChannelById(discordChannelId)?.history?.retrievePast(100)?.queue ({
            var longerHistory = false
            if (it.isEmpty()) {
                future.complete("Empty")
                return@queue
            }
            it.reverse()

            if (!it[0].author.isBot) {
                longerHistory = true
            }
            val chatLog = StringBuilder()
            if (longerHistory) {
                chatLog.append("*** HISTORY LIMITED TO 100 MESSAGES ***").append("\n")
            }
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    .withLocale(Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault())
            for (message in it) {
                val authorAndDate = "[" + formatter.format(message.timeCreated.toInstant()) + "] " + message.author.name + ": "
                var content = message.contentDisplay
                if (message.attachments.isNotEmpty()) {
                    content += " [+" + message.attachments.size + " attachments uploaded]"
                }
                if (message.embeds.isNotEmpty()) {
                    val embed = StringBuilder()
                    for (messageEmbed in message.embeds) {
                        embed.append("Embed: ").append(messageEmbed?.author?.name?:"none").append(" -> ").append(messageEmbed.description)
                    }
                    content += embed.toString()
                }
                chatLog.append(authorAndDate).append(content).append("\n")
            }
            future.complete(DiscordBot.INSTANCE.pasteAndGetKey(chatLog.toString()))
        },{
            future.complete(null)
        })
        return future
    }

    fun init(): Ticket {
        this.topics.forEach {
            it.ticket = this
        }
        return this
    }
}