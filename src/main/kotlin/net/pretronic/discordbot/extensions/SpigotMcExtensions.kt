package net.pretronic.discordbot.extensions

import net.pretronic.discordbot.DiscordBot
import net.pretronic.spigotsite.api.user.Conversation
import net.pretronic.spigotsite.api.user.User

fun Conversation.replyKey(user: User, messageKey: String, replacements: Map<String, String>) {
    val message0 = DiscordBot.INSTANCE.messageManager.getMessage(null, messageKey)
    var message = message0.message
    replacements.forEach { message = message.replace("%${it.key}%", it.value) }
    reply(user, message)
}

fun Conversation.replyKey(user: User, messageKey: String) {
    replyKey(user, messageKey, emptyMap())
}