package net.pretronic.discordbot.ticket.state

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.pretronic.discordbot.ticket.Ticket
import net.pretronic.discordbot.ticket.TicketParticipant
import net.pretronic.discordbot.ticket.TicketParticipantRole

class OpenTicketState: TicketState {

    override val name: String = "Open"

    override fun onMessageReceive(ticket: Ticket, event: GuildMessageReceivedEvent) {
        if(ticket.participants.firstOrNull { it.discordId == event.author.idLong } == null) {
            ticket.addParticipant(TicketParticipant(event.author.idLong, TicketParticipantRole.TEAM))
        }
    }

    override fun onReactionAdd(ticket: Ticket, event: GuildMessageReactionAddEvent) {

    }
}