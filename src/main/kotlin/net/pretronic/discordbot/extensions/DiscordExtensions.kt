package net.pretronic.discordbot.extensions

import net.dv8tion.jda.api.entities.PrivateChannel
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.pretronic.discordbot.DiscordBot

fun PrivateChannel.sendMessageKey(messageKey: String, replacements: Map<String, String>): MessageAction {
    val message0 = DiscordBot.INSTANCE.messageManager.getMessage(null, messageKey)
    var message = message0.message
    replacements.forEach { message = message.replace("%${it.key}%", it.value) }
    return sendMessage(message)
}

fun PrivateChannel.sendMessageKey(messageKey: String): MessageAction {
    return sendMessageKey(messageKey, emptyMap())
}