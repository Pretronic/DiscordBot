package net.pretronic.discordbot.ticket

import java.awt.Color

enum class TicketAction(val title: String, val color: Color) {

    CREATE("Ticket Creation Event", Color.GREEN),
    CLOSE("Ticket Deletion Event", Color.RED);
}