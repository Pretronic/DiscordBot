package net.pretronic.discordbot.ticket.state

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.pretronic.discordbot.ticket.Ticket

interface TicketState {

    companion object {

        val TOPIC_CHOOSING = TopicChoosingTicketState()
        val PROVIDE_INFORMATION = ProvideInformationTicketState()
        val OPEN = OpenTicketState()
        val CLOSED = ClosedTicketState()

        fun byName(name: String): TicketState {
            return when (name) {
                "TopicChoosing" -> TOPIC_CHOOSING
                "ProvideInformation" -> PROVIDE_INFORMATION
                "Closed" -> CLOSED
                else -> OPEN
            }
        }
    }

    val name: String

    fun onMessageReceive(ticket: Ticket, event: GuildMessageReceivedEvent)

    fun onReactionAdd(ticket: Ticket, event: GuildMessageReactionAddEvent)

    fun nextState(ticket: Ticket) {
        ticket.nextState()
    }
}