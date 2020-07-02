package net.pretronic.discordbot

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.pretronic.discordbot.extensions.sendMessageKey
import net.pretronic.discordbot.message.Messages

class BotListeners(private val discordBot: DiscordBot): ListenerAdapter() {

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        val pretronicUser = discordBot.userManager.getUserByDiscord(event.user.idLong)
        if(pretronicUser != null && pretronicUser.isVerified()) {
            pretronicUser.addVerifiedRoles()
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if(event.author == discordBot.jda.selfUser) return
        discordBot.ticketManager.tickets.cachedObjects.forEach {
            it.state.onMessageReceive(it, event)
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

    private fun ticketEventExecutionAndCloseCheck(event: GuildMessageReactionAddEvent) {
        discordBot.ticketManager.tickets.get("openMemberId", event.member.idLong)?.let {
            if(event.messageIdLong == it.discordControlMessageId &&
                    discordBot.config.ticketCloseEmoji.isDiscordEmoji(event.reactionEmote)) {
                it.close(event.userIdLong)
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
}