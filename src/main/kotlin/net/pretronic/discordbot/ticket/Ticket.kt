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
            field = value
            value.handleChange(this)
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
        this.topics.add(TicketTopicContent(ticketTopic, arrayListOf()))
    }

    fun isCreator(discordId: Long): Boolean {
        return participants.firstOrNull { it.role == TicketParticipantRole.CREATOR && it.discordId == discordId } != null
    }

    fun close() {
        DiscordBot.INSTANCE.jda.getTextChannelById(this.discordChannelId)?.delete()?.queue()
        creator.asMember()?.user?.openPrivateChannel()?.queue { channel ->
            channel.sendMessageKey(Messages.DISCORD_TICKET_CLOSED_SELF, language).queue()
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
}