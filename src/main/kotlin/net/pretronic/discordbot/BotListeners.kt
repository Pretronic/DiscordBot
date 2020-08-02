package net.pretronic.discordbot

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.pretronic.discordbot.extensions.addReactionById
import net.pretronic.discordbot.extensions.sendMessageKey
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.ticket.state.TicketState

class BotListeners(private val discordBot: DiscordBot): ListenerAdapter() {

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val pretronicUser = discordBot.userManager.getUserByDiscord(event.user.idLong)
        if(pretronicUser != null && pretronicUser.isVerified()) {
            pretronicUser.addVerifiedRoles()
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if(event.author == discordBot.jda.selfUser) return
        channelAutoEmojisCheck(event)
        discordBot.ticketManager.tickets.cachedObjects.forEach {
            if(it.discordChannelId == event.channel.idLong && it.creator.discordId == event.author.idLong) {
                it.state.onMessageReceive(it, event)
            }
        }
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        if(event.user == discordBot.jda.selfUser) return
        ticketEventExecutionAndCloseCheck(event)
        if(event.messageIdLong == discordBot.config.ticketCreateMessageId) {
            if(discordBot.config.ticketVerifyOpenReactionEmoji.isDiscordEmoji(event.reactionEmote)) {
                ticketEventVerify(event)
            } else {
                ticketEventCreateCheck(event)
            }
        }
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        discordBot.ticketManager.getTicket(event.user.idLong)?.let {
            it.state = TicketState.CLOSED
        }
    }

    private fun ticketEventExecutionAndCloseCheck(event: GuildMessageReactionAddEvent) {
        discordBot.ticketManager.tickets.get("openDiscordChannelId", event.channel.idLong)?.let {
            if(event.messageIdLong == it.discordControlMessageId &&
                    discordBot.config.ticketCloseEmoji.isDiscordEmoji(event.reactionEmote)) {
                it.state = TicketState.CLOSED
            }else {
                it.state.onReactionAdd(it, event)
            }
        }
    }

    private fun ticketEventCreateCheck(event: GuildMessageReactionAddEvent) {
        discordBot.config.languages.forEach {
            if(it.emoji != null && it.emoji.isDiscordEmoji(event.reactionEmote)) {
                discordBot.ticketManager.createTicket(event.member, it).thenAccept {
                    event.reaction.removeReaction(event.user).queue()
                }
            }
        }
    }

    private fun ticketEventVerify(event: GuildMessageReactionAddEvent) {
        event.reaction.removeReaction(event.user).queue()
        discordBot.userManager.verify(event.member)
    }

    private fun channelAutoEmojisCheck(event: GuildMessageReceivedEvent) {
        discordBot.config.channelAutoEmojis.forEach {
            if(it.key == event.channel.idLong) {
                it.value.forEach { emoji ->
                    event.channel.addReactionById(event.messageIdLong, emoji)?.queue()
                }
            }
        }
    }
}