package net.pretronic.discordbot.commands.setup

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.pretronic.discordbot.extensions.sendMessageKey

class SetupSendMessageCommand: Command() {

    init {
        name = "sendMessage"
        userPermissions = arrayOf(Permission.ADMINISTRATOR)
        guildOnly = false
        hidden = true
    }

    override fun execute(event: CommandEvent) {
        if(event.args.isEmpty()) {
            event.author.openPrivateChannel().queue {
                it.sendMessage("!setup sendMessage <messageKey>").queue()
            }
            return
        }
        event.channel.sendMessageKey(event.args).queue()
    }
}