package net.pretronic.discordbot

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageReaction
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.pretronic.discordbot.extensions.addReaction
import net.pretronic.discordbot.extensions.addReactionById
import net.pretronic.discordbot.ticket.state.TicketState
import javax.annotation.Nonnull

class BotListeners(private val discordBot: DiscordBot): ListenerAdapter() {

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if(event.author == discordBot.jda.selfUser) return
        channelAutoEmojisCheck(event)
        discordBot.ticketManager.getTicketByChannelId(event.channel.idLong)?.let {
            if(it.creator.discordId == event.author.idLong) {
                it.state.onMessageReceive(it, event)
            }
        }
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        if(event.user == discordBot.jda.selfUser) return
        ticketEventExecutionAndCloseCheck(event)
        if(event.messageIdLong == discordBot.config.ticketCreateMessageId) {
            ticketEventCreateCheck(event)
        } else {
            discordBot.config.subscribeGroupEmoji.forEach {
                if(event.messageIdLong == it.messageId) {
                    it.emojiRoles.forEach { (emoji, roleId)->
                        if(emoji.isDiscordEmoji(event.reactionEmote)) {
                            toggleRole(roleId, event.member, event.reaction)
                            return
                        }
                    }
                }
            }
            discordBot.config.channelAutoEmojis.forEach {
                if(it.channelId == event.channel.idLong) {
                    it.emojiRoles.forEach { autoEmoji ->
                        if(autoEmoji.value > 0) {
                            if(autoEmoji.key.isDiscordEmoji(event.reactionEmote)) {
                                toggleRole(autoEmoji.value, event.member, event.reaction)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        discordBot.verificationManager.checkPendingVerification(event.member.idLong)
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        discordBot.ticketManager.getTicket(event.user.idLong)?.let {
            it.state = TicketState.CLOSED
        }

        event.member?.let { member ->
            if(member.roles.any { role -> role.idLong == DiscordBot.INSTANCE.config.verifiedRoleId }) {
                discordBot.verificationManager.addPendingVerification(event.user.idLong)
            }
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

    private fun channelAutoEmojisCheck(event: GuildMessageReceivedEvent) {
        discordBot.config.channelAutoEmojis.forEach {

            if(it.channelId == event.channel.idLong) {
                it.emojiRoles.forEach { autoEmoji ->
                    event.channel.addReactionById(event.messageIdLong, autoEmoji.key)?.queue({},{/*Ignored*/})
                }
            }
        }
    }

    private fun toggleRole(roleId: Long, member: Member, reaction: MessageReaction? = null) {
        val guild = member.guild
        guild.getRoleById(roleId)?.let { role ->
            if(member.roles.contains(role)) {
                guild.removeRoleFromMember(member, role).queue()
            } else {
                guild.addRoleToMember(member, role).queue()
            }
            reaction?.removeReaction(member.user)?.queue()
        }
    }
}