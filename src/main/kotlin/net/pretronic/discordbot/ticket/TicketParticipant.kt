package net.pretronic.discordbot.ticket

import net.dv8tion.jda.api.entities.Member
import net.pretronic.discordbot.DiscordBot

class TicketParticipant(val discordId: Long, val role: TicketParticipantRole) {

    fun asMember(): Member? {
        return DiscordBot.INSTANCE.getPretronicGuild().getMemberById(this.discordId)
    }

    override fun equals(other: Any?): Boolean {
        return other is TicketParticipant && other.discordId == discordId
    }
}