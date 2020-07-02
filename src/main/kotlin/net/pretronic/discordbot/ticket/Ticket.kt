package net.pretronic.discordbot.ticket

import net.pretronic.databasequery.api.dsl.insert
import net.pretronic.databasequery.api.dsl.update
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.extensions.sendMessageKey
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.message.language.Language
import net.pretronic.discordbot.ticket.state.TicketState
import net.pretronic.discordbot.ticket.topic.TicketTopic
import net.pretronic.discordbot.ticket.topic.TicketTopicContent
import net.pretronic.libraries.utility.map.index.IndexLinkedHashMap
import net.pretronic.libraries.utility.map.index.IndexMap

class Ticket(val id: Int,
             val discordChannelId: Long,
             state: TicketState,
             val language: Language,
             participants: MutableCollection<TicketParticipant>,
             val discordControlMessageId: Long,
             val topics: MutableCollection<TicketTopicContent>,
             topicChooseMessageId: Long?) {

    constructor(id: Int,
                discordChannelId: Long,
                state: TicketState,
                language: Language,
                creatorId: Long,
                discordControlMessageId: Long) :
            this(id, discordChannelId, state, language,
                    mutableListOf(TicketParticipant(creatorId, TicketParticipantRole.CREATOR)),
                    discordControlMessageId, ArrayList(), null)

    var state: TicketState = state
        set(value) {
            field = value
            DiscordBot.INSTANCE.storage.ticket.update {
                set("State", state.name)
                where("Id", id)
            }.executeAsync()
        }
    val participants: MutableCollection<TicketParticipant> = participants
    val creator: TicketParticipant
        get() = participants.first { it.role == TicketParticipantRole.CREATOR }
    var topicChooseMessageId = topicChooseMessageId
        set(value) {
            DiscordBot.INSTANCE.storage.ticket.update {
                set("TopicChooseMessageId", value)
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
        DiscordBot.INSTANCE.storage.ticket.update {
            set("Topics", "{}")//DocumentFileType.JSON.writer.write(DiscordBot.INSTANCE.storage.topicContext.serialize(topics).toDocument(), false) @Todo insert
            where("Id", id)
        }.executeAsync()
    }

    fun isCreator(discordId: Long): Boolean {
        return participants.firstOrNull { it.role == TicketParticipantRole.CREATOR && it.discordId == discordId } != null
    }

    fun nextState() {
        when(this.state) {
            TicketState.TOPIC_CHOOSING -> this.state = TicketState.PROVIDE_INFORMATION
            TicketState.PROVIDE_INFORMATION -> this.state = TicketState.OPEN
            TicketState.OPEN -> this.state = TicketState.CLOSED
        }
    }

    fun close(executor: Long) {
        this.state = TicketState.CLOSED
        DiscordBot.INSTANCE.jda.getTextChannelById(this.discordChannelId)?.delete()?.queue()
        creator.asMember()?.user?.openPrivateChannel()?.queue { channel ->
            if(executor == creator.asMember()?.idLong) {
                channel.sendMessageKey(Messages.DISCORD_TICKET_CLOSED_SELF, mapOf(Pair("name", creator.asMember()!!.effectiveName))).queue()
            }
        }
    }
}