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
        discordBot.userManager.verify(event.member)
    }
}