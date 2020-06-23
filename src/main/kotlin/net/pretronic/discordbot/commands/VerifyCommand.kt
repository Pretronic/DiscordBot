package net.pretronic.discordbot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.extensions.sendMessageKey

class VerifyCommand(private val discordBot: DiscordBot) : Command() {

    init {
        name = "verify"
    }

    override fun execute(event: CommandEvent) {
        try {
            if(discordBot.userManager.getUserByDiscord(event.member.idLong) == null) {
                val pendingUserVerification = discordBot.userManager.createPendingVerificationUser(event.author.idLong)
                event.author.openPrivateChannel().queue {
                    it.sendMessageKey(Messages.COMMAND_VERIFY_START, mapOf(Pair("secret", pendingUserVerification.secret))).queue()
                }
            } else {
                event.author.openPrivateChannel().queue {
                    it.sendMessageKey(Messages.COMMAND_VERIFY_ALREADY).queue()
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

    }
}