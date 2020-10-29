package net.pretronic.discordbot.ticket.state

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.extensions.addReaction
import net.pretronic.discordbot.extensions.sendMessageKey
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.ticket.Ticket

class TopicChoosingTicketState : TicketState {

    override val name: String = "TopicChoosing"

    override fun handleChange(ticket: Ticket) {
        ticket.creator.asMember()?.let {
            ticket.discordChannel?.upsertPermissionOverride(it)?.setAllow(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ)?.setDeny(Permission.MESSAGE_WRITE)?.queue() }
    }

    override fun onMessageReceive(ticket: Ticket, event: GuildMessageReceivedEvent) {

    }

    override fun onReactionAdd(ticket: Ticket, event: GuildMessageReactionAddEvent) {
        val config = DiscordBot.INSTANCE.config
        config.ticketTopics.forEach {
            if(it.emoji.isDiscordEmoji(event.reactionEmote)) {
                ticket.addTopic(it)
                event.channel.deleteMessageById(event.messageIdLong).queue()
                event.channel.sendMessageKey(Messages.DISCORD_TICKET_PROVIDE_INFORMATION, ticket.language, mapOf(Pair("project", it.name))).queue { message ->
                    message.addReaction(DiscordBot.INSTANCE.config.ticketProvideInformationFinishEmoji)?.queue {
                        val topics = config.getAccessAbleTicketTopics(event.userIdLong, ticket.topics)
                        if(topics.isNotEmpty()) {
                            message.addReaction(DiscordBot.INSTANCE.config.ticketProvideInformationNextTopicEmoji)?.queue({},{/*Ignored*/})
                        }
                    }
                }
                ticket.clearTicketNotOpenedNotifications(event.channel)
                ticket.state = TicketState.PROVIDE_INFORMATION
                return
            }
        }
    }


}