package net.pretronic.discordbot.ticket.topic

import net.pretronic.databasequery.api.dsl.update
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.ticket.Ticket
import net.pretronic.discordbot.ticket.TicketMessage
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.annotations.DocumentIgnored
import net.pretronic.libraries.document.type.DocumentFileType

class TicketTopicContent(@DocumentIgnored var ticket: Ticket, topic: TicketTopic, val description: MutableCollection<TicketMessage>) {

    private val topicName = topic.name

    val topic: TicketTopic
        get() {
            return DiscordBot.INSTANCE.config.ticketTopicByName(topicName)
        }

    fun addDescription(message: TicketMessage) {
        this.description.add(message)
        DiscordBot.INSTANCE.storage.ticket.update {
            set("Topics", DocumentFileType.JSON.writer.write(Document.newDocument(ticket.topics), false))
            where("Id", ticket.id)
        }.execute()
    }

}