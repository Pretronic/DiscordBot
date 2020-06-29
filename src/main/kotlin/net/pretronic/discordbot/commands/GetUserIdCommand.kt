package net.pretronic.discordbot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.extensions.sendMessageKey
import net.pretronic.discordbot.message.Messages

class GetUserIdCommand(private val discordBot: DiscordBot): Command() {

    init {
        name = "getuserid"
    }

    override fun execute(event: CommandEvent) {
        val pretronicUser = discordBot.userManager.getUserByDiscord(event.member.idLong)
        if(pretronicUser == null) {
            event.author.openPrivateChannel().queue {
                it.sendMessageKey(Messages.DISCORD_GETUSERID_NOT_EXIST).queue()
            }
        } else {
            event.author.openPrivateChannel().queue {
                it.sendMessageKey(Messages.DISCORD_GETUSERID_SEND, mapOf(Pair("key", pretronicUser.key))).queue()
            }
        }
    }
}