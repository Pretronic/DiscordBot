package net.pretronic.discordbot.ticket.state

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.extensions.addReaction
import net.pretronic.discordbot.extensions.sendMessageKey
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.ticket.Ticket
import net.pretronic.discordbot.ticket.TicketMessage
import java.lang.StringBuilder
import java.time.Duration
import java.util.function.Consumer

class ProvideInformationTicketState: TicketState {

    override val name: String = "ProvideInformation"

    override fun onMessageReceive(ticket: Ticket, event: GuildMessageReceivedEvent) {
        val entry = ticket.topics.last()
        entry.description.add(TicketMessage(event.messageIdLong, event.message.contentDisplay, event.member?.idLong, event.member?.effectiveName))
    }

    override fun onReactionAdd(ticket: Ticket, event: GuildMessageReactionAddEvent) {
        val config = DiscordBot.INSTANCE.config
        if(config.ticketProvideInformationFinishEmoji.isDiscordEmoji(event.reactionEmote)) {
            if(checkInformationInput(ticket, event)){
                clearAndDisplayTopicDescription(ticket, event)

                event.channel.sendMessageKey(Messages.DISCORD_TICKET_PROVIDE_INFORMATION_FINISH, ticket.language)
                        .delay(Duration.ofSeconds(60))
                        .flatMap(Message::delete)
                        .queue({  }, { /*@Todo ignored*/ })
                ticket.state = TicketState.OPEN
            }
        } else if(config.ticketProvideInformationNextTopicEmoji.isDiscordEmoji(event.reactionEmote)) {
            if(checkInformationInput(ticket, event)) {
                clearAndDisplayTopicDescription(ticket, event)

                event.channel.sendMessageKey(Messages.DISCORD_TICKET_TOPIC_CHOOSE, ticket.language).queue { message2 ->
                    ticket.topicChooseMessageId = message2.idLong
                    DiscordBot.INSTANCE.config.getAccessAbleTicketTopics(event.userIdLong, ticket.topics).forEach { topic ->
                        message2.addReaction(topic.emoji)?.queue()
                    }
                    ticket.state = TicketState.TOPIC_CHOOSING
                }
            }
        }
    }

    private fun clearAndDisplayTopicDescription(ticket: Ticket, event: GuildMessageReactionAddEvent) {
        event.channel.deleteMessageById(event.messageIdLong).queue()
        val content = ticket.topics.last()

        content.description.forEach {
            event.channel.deleteMessageById(it.messageId).queue()
        }

        val descriptionBuilder = StringBuilder()
        content.description.forEach { descriptionBuilder.append(it.content).append("\n") }

        event.channel.sendMessageKey(Messages.DISCORD_TICKET_DISPLAY_INFORMATION, ticket.language,
                mapOf(Pair("topic", content.topic.name), Pair("description", descriptionBuilder.toString()))).queue()
    }

    private fun checkInformationInput(ticket: Ticket, event: GuildMessageReactionAddEvent): Boolean {
        if(ticket.topics.last().description.isEmpty()) {
            event.reaction.removeReaction(event.user).queue()
            event.channel.sendMessageKey(Messages.DISCORD_TICKET_PROVIDE_INFORMATION_NEED, ticket.language)
                    .delay(Duration.ofSeconds(5))
                    .flatMap(Message::delete)
                    .queue()
            return false
        }
        return true
    }
}