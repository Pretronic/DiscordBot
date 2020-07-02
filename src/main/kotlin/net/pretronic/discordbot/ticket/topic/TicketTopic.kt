package net.pretronic.discordbot.ticket.topic

import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.DiscordEmoji
import net.pretronic.discordbot.resource.PretronicResource
import net.pretronic.libraries.document.adapter.DocumentAdapter
import net.pretronic.libraries.document.entry.DocumentBase
import net.pretronic.libraries.document.entry.DocumentEntry
import net.pretronic.libraries.document.simple.SimplePrimitiveEntry
import net.pretronic.libraries.utility.reflect.TypeReference

class TicketTopic(val name: String, val emoji: DiscordEmoji, private val resourceId: Int?): Comparable<TicketTopic> {

    val resource : PretronicResource?
        get() = if(resourceId != null && resourceId > 0) DiscordBot.INSTANCE.resourceManager.getResource(resourceId) else null

    class Adapter: DocumentAdapter<TicketTopic> {

        override fun write(key: String, topic: TicketTopic): DocumentEntry {
            return SimplePrimitiveEntry(key, topic.name)
        }

        override fun read(document: DocumentBase, p1: TypeReference<TicketTopic>?): TicketTopic {
            return DiscordBot.INSTANCE.config.ticketTopicByName(document.toPrimitive().asString)
        }

    }

    override fun compareTo(other: TicketTopic): Int {
        return 0
    }
}