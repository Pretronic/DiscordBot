package net.pretronic.discordbot.ticket.topic

import net.pretronic.discordbot.ticket.TicketMessage
import net.pretronic.libraries.document.annotations.DocumentIgnored

class TicketTopicContent(@DocumentIgnored val topic: TicketTopic, val description: MutableCollection<TicketMessage>) {

    private val topicName = topic.name

}