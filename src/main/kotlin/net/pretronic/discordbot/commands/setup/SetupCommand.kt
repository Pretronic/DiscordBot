package net.pretronic.discordbot.commands.setup

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import java.util.function.BiConsumer

class SetupCommand: Command() {

    init {
        name = "setup"
        userPermissions = arrayOf(Permission.ADMINISTRATOR)
        guildOnly = false
        hidden = true
        children = arrayOf(SetupSendMessageCommand(), SetupAddReactionCommand())
        helpBiConsumer = BiConsumer { event, command ->
            run {
                event.author.openPrivateChannel().queue {
                    it.sendMessage("!setup sendMessage <messageKey>").queue()
                    it.sendMessage("!setup addReaction <messageId> <reaction>").queue()
                }
            }
        }
    }

    override fun execute(event: CommandEvent) {

    }


}