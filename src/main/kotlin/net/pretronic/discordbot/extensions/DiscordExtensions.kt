package net.pretronic.discordbot.extensions

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.DiscordEmoji
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.message.language.Language

fun MessageChannel.sendMessageKey(messageKey: String, language: Language?, replacements: Map<String, String>): MessageAction {
    val message0 = DiscordBot.INSTANCE.messageManager.getMessage(language, messageKey)
    return if(message0.content != null) {
        var message: String = message0.content
        replacements.forEach { message = message.replace("%${it.key}%", it.value) }
        if(this is PrivateChannel) {
            sendMessage(EmbedBuilder().setColor(Messages.getRandomBrightColor()).setDescription(message).build())
        } else {
            sendMessage(message)
        }
    } else {
        sendMessage(message0.buildEmbed(replacements)!!)
    }
}

fun MessageChannel.sendMessageKey(messageKey: String, replacements: Map<String, String>): MessageAction {
    return sendMessageKey(messageKey, null, replacements)
}

fun MessageChannel.sendMessageKey(messageKey: String): MessageAction {
    return sendMessageKey(messageKey, emptyMap())
}

fun MessageChannel.sendMessageKey(messageKey: String, language: Language?): MessageAction {
    return sendMessageKey(messageKey, language, emptyMap())
}

fun MessageChannel.addReactionById(messageId: Long, emoji: DiscordEmoji): RestAction<Void>? {
    if(emoji.unicode != null) return addReactionById(messageId, emoji.unicode)
    return emoji.id?.let { it -> DiscordBot.INSTANCE.jda.getEmoteById(it)?.let { addReactionById(messageId, it) } }
}

fun Message.addReaction(emoji: DiscordEmoji): RestAction<Void>? {
    if(emoji.unicode != null) return addReaction(emoji.unicode)
    return emoji.id?.let { emote -> DiscordBot.INSTANCE.jda.getEmoteById(emote)?.let { addReaction(it) } }
}

fun Message.retrieveReactionUsers(emoji: DiscordEmoji): ReactionPaginationAction? {
    if(emoji.unicode != null) return retrieveReactionUsers(emoji.unicode)
    return emoji.id?.let { it -> DiscordBot.INSTANCE.jda.getEmoteById(it)?.let { retrieveReactionUsers(it) } }
}