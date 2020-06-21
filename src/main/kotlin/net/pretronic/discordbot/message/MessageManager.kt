package net.pretronic.discordbot.message

import net.pretronic.discordbot.user.DiscordUser

class MessageManager {

    private val defaultLanguage : Language = Language("English", "en")
    private val packs : Collection<MessagePack> = ArrayList()

    fun getMessage(discordUser: DiscordUser, messageKey: Messages) : Message {
        return getMessage(discordUser.language, messageKey)
    }

    fun getMessage(language: Language, messageKey : Messages) : Message {
        val pack : MessagePack = packs.firstOrNull { it.language == language }?:getDefaultPack()

        return pack.messages.firstOrNull { it.messageKey == messageKey}?: getDefaultPack().messages.first { it.messageKey == messageKey }
    }

    private fun getDefaultPack() : MessagePack {
        return packs.first { it.language == this.defaultLanguage }
    }

    fun loadPacks() {

    }
}