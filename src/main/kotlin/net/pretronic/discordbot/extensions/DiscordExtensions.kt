package net.pretronic.discordbot.extensions

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.DiscordEmoji

fun MessageChannel.sendMessageKey(messageKey: String, replacements: Map<String, String>): MessageAction {
    val message0 = DiscordBot.INSTANCE.messageManager.getMessage(null, messageKey)
    if(message0.content != null) {
        var message: String = message0.content
        replacements.forEach { message = message.replace("%${it.key}%", it.value) }
        return sendMessage(message)
    } else {
        return sendMessage(message0.buildEmbed(replacements)!!)
    }
}

fun MessageChannel.sendMessageKey(messageKey: String): MessageAction {
    return sendMessageKey(messageKey, emptyMap())
}

fun Message.addReaction(emoji: DiscordEmoji): RestAction<Void>? {
    if(emoji.name != null) return emoji.toEmoji()?.unicode?.let { addReaction(it) }
    return emoji.id?.let { it -> DiscordBot.INSTANCE.jda.getEmoteById(it)?.let { addReaction(it) } }
}