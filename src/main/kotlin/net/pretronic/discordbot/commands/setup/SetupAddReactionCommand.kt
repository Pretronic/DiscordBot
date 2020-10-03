package net.pretronic.discordbot.commands.setup

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class SetupAddReactionCommand: Command() {

    init {
        name = "addReaction"
        userPermissions = arrayOf(Permission.ADMINISTRATOR)
        guildOnly = false
        hidden = true
    }

    override fun execute(event: CommandEvent) {
        if(event.args.isEmpty()) {
            event.author.openPrivateChannel().queue {
                it.sendMessage("!setup addReaction <messageId> <reaction>").queue()
            }
            return
        }
        val split = event.args.split(" ")
        val messageId = split[0].toLong()

        event.textChannel.retrieveMessageById(messageId).queue {
            if(split[1].toLongOrNull() != null) {
                it.addReaction(event.jda.getEmoteById(split[1].toLong())!!).queue({},{/*Ignored*/})
            } else {
                it.addReaction(split[1]).queue({},{/*Ignored*/})
            }
        }

    }
}