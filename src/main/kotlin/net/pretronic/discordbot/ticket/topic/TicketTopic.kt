package net.pretronic.discordbot.ticket.topic

import net.pretronic.discordbot.DiscordEmoji

class TicketTopic(val name: String, val emoji: DiscordEmoji, val roleIds: Array<Long>?): Comparable<TicketTopic> {

    override fun compareTo(other: TicketTopic): Int {
        return 0
    }
}