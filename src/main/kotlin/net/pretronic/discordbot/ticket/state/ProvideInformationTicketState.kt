package net.pretronic.discordbot.ticket.state

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.pretronic.discordbot.ticket.Ticket

class ProvideInformationTicketState: TicketState {

    override val name: String = "ProvideInformation"

    override fun onMessageReceive(ticket: Ticket, event: GuildMessageReceivedEvent) {

    }

    override fun onReactionAdd(ticket: Ticket, event: GuildMessageReactionAddEvent) {

    }
}