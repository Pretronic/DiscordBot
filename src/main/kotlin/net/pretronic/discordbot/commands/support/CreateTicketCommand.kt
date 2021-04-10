package net.pretronic.discordbot.commands.support

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.MiscUtil
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.message.embed.EmbedData
import net.pretronic.discordbot.message.language.Language
import net.pretronic.discordbot.ticket.state.TicketState
import java.util.*
import java.util.concurrent.CompletableFuture


class CreateTicketCommand: Command() {

    init {
        name = "createTicket"
        guildOnly = true
        hidden = true
    }

    //!createTicket <member> [reason]
    override fun execute(event: CommandEvent) {
        if(event.member.roles.any { role -> role.idLong == DiscordBot.INSTANCE.config.teamRoleId }) {
            val args = event.args.split(" ")
            if(event.args.trim().isEmpty() || args.isEmpty()) {
                event.replyError("Wrong usage of command: Use !createTicket <member> [reason]")
                return
            }

            val rawMember = args[0]
            val reason = if(args.size == 2) args[1] else "none"
            val matcher = MentionType.USER.pattern.matcher(rawMember)

            var sent = false

            while (matcher.find()) {
                sent = true
                try {
                    val id = MiscUtil.parseSnowflake(matcher.group(1))
                    event.guild.retrieveMemberById(id).queue({ member ->
                        DiscordBot.INSTANCE.ticketManager.createTicket(member, DiscordBot.INSTANCE.languageManager.defaultLanguage).thenAccept { ticket ->
                            if(ticket == null) {
                                event.replyError(member.effectiveName + " has already an open ticket")
                                return@thenAccept
                            }

                            ticket.discordChannel?.let { ticketChannel ->
                                ticket.topicChooseMessageId?.let {
                                    ticketChannel.deleteMessageById(it).queue()
                                }
                                ticketChannel.sendMessage(EmbedBuilder()
                                        .setDescription("${member.asMention} ${event.member.effectiveName} has created a ticket for you with the reason: $reason")
                                        .setColor(Messages.getRandomBrightColor())
                                        .build())
                                        .queue()

                                ticket.state = TicketState.OPEN
                                member.user.openPrivateChannel().queue { privateChannel ->
                                    val ticketChannelLink = "https://discord.com/channels/${event.guild.idLong}/${ticketChannel.idLong}/"
                                    privateChannel.sendMessage(EmbedBuilder()
                                            .setDescription("${member.asMention} ${event.member.effectiveName} has created a ticket for you with the reason: $reason ($ticketChannelLink)")
                                            .setColor(Messages.getRandomBrightColor())
                                            .build())
                                            .queue()
                                }
                            }

                        }
                    },{
                        sendNotSentMessage(event)
                    })
                } catch (ignored: NumberFormatException) {
                    sendNotSentMessage(event)
                }
            }
            if(!sent) sendNotSentMessage(event)
        }
    }

    private fun sendNotSentMessage(event: CommandEvent) {
        event.replyError("Wrong usage of command: Use !createTicket <member> [reason]")
    }
}