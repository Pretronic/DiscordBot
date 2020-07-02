package net.pretronic.discordbot.ticket

import net.dv8tion.jda.api.entities.Member
import net.pretronic.databasequery.api.dsl.find
import net.pretronic.databasequery.api.dsl.insert
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.extensions.addReaction
import net.pretronic.discordbot.extensions.sendMessageKey
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.message.language.Language
import net.pretronic.discordbot.ticket.state.TicketState
import net.pretronic.discordbot.ticket.topic.TicketTopic
import net.pretronic.discordbot.ticket.topic.TicketTopicContent
import net.pretronic.libraries.caching.ArrayCache
import net.pretronic.libraries.caching.Cache
import net.pretronic.libraries.caching.CacheQuery
import net.pretronic.libraries.utility.Validate
import net.pretronic.libraries.utility.map.index.IndexLinkedHashMap
import net.pretronic.libraries.utility.map.index.IndexMap
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

class TicketManager(private val discordBot: DiscordBot) {

    val tickets: Cache<Ticket> = ArrayCache()

    init {
        tickets.setMaxSize(50)
        tickets.registerQuery("openMemberId", MemberIdQuery())
    }

    fun createTicket(member: Member, language: Language): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val ticket0 = tickets.get("openMemberId", member.idLong)
        if(ticket0 == null) {
            discordBot.config.ticketCategory.createTextChannel(language.localizedName+"-"+member.effectiveName).queue {
                it.sendMessageKey(Messages.DISCORD_TICKET_CONTROL_MESSAGE).queue { message ->
                    message.addReaction(discordBot.config.ticketCloseEmoji)?.queue()

                    val id = discordBot.storage.ticket.insert {
                        set("State", TicketState.TOPIC_CHOOSING.name)
                        set("ChannelId", it.idLong)
                        set("Language", language.name+"_"+language.localizedName)
                        set("DiscordControlMessageId", message.idLong)
                    }.executeAndGetGeneratedKeyAsInt("Id")

                    val ticket = Ticket(id, it.idLong, TicketState.TOPIC_CHOOSING, language, member.idLong, message.idLong)
                    tickets.insert(ticket)

                    discordBot.storage.ticketParticipants.insert {
                        set("TicketId", id)
                        set("DiscordUserId", member.idLong)
                        set("Role", TicketParticipantRole.CREATOR)
                    }.execute()

                    it.sendMessageKey(Messages.DISCORD_TICKET_TOPIC_CHOOSE).queue { message2 ->
                        ticket.topicChooseMessageId = message2.idLong
                        discordBot.config.getAccessAbleTicketTopics(member.idLong, ticket.topics).forEach { topic ->
                            message2.addReaction(topic.emoji)?.queue()
                        }
                    }

                    future.complete(null)
                }
            }
        } else {
            future.complete(null)
            member.user.openPrivateChannel().queue { channel ->
                channel.sendMessageKey(Messages.DISCORD_TICKET_ALREADY_CREATED).queue()
            }
        }
        return future
    }



    private class MemberIdQuery: CacheQuery<Ticket> {

        override fun check(ticket: Ticket, identifiers: Array<out Any>): Boolean {
            return ticket.isCreator(identifiers[0] as Long) && ticket.state != TicketState.CLOSED
        }

        override fun validate(identifiers: Array<out Any>) {
            Validate.isTrue(identifiers.size == 1 && identifiers[0] is Long)
        }

        override fun load(identifiers: Array<out Any>): Ticket? {
            val result = DiscordBot.INSTANCE.storage.ticket.find {
                join(DiscordBot.INSTANCE.storage.ticketParticipants)
                on("Id", "TicketId")
                whereNot("State", "Closed")
                where("DiscordUserId", identifiers[0] as Long)
                where("Role", TicketParticipantRole.CREATOR)
            }.execute()
            if(!result.isEmpty) {
                val entry = result.first()
                val languageSplit = entry.getString("Language").split("_")


                val ticketId = entry.getInt("Id")
                return Ticket(ticketId,
                        entry.getLong("ChannelId"),
                        TicketState.byName(entry.getString("State")),
                        DiscordBot.INSTANCE.languageManager.getOrCreate(languageSplit[0], languageSplit[1]),
                        loadParticipants(ticketId),
                        entry.getLong("DiscordControlMessageId"),
                        loadTopics(entry.getString("Topics")),
                        entry.getLong("TopicChooseMessageId"))
            }
            return null
        }

        private fun loadParticipants(ticketId: Int): MutableCollection<TicketParticipant> {
            val participants = ArrayList<TicketParticipant>()
            DiscordBot.INSTANCE.storage.ticketParticipants.find {
                where("TicketId", ticketId)
            }.execute().forEach {
                participants.add(TicketParticipant(it.getLong("DiscordUserId"), TicketParticipantRole.parse(it.getString("Role"))))
            }
            return participants
        }

        private fun loadTopics(value: String): MutableCollection<TicketTopicContent> {
            //return DiscordBot.INSTANCE.storage.topicContext.deserialize(Document.newDocument(), TypeReference<MutableMap<TicketTopic, Boolean>>().type)
            //return DocumentFileType.JSON.reader.read(value).getAsObject(TypeReference<MutableMap<TicketTopic, Boolean>>().type)
            return arrayListOf()//@Todo laoding context
        }
    }
}