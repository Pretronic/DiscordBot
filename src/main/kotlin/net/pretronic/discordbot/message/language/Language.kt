package net.pretronic.discordbot.message.language

import net.pretronic.discordbot.DiscordEmoji

data class Language(val name : String, val localizedName : String, val default: Boolean, val emoji: DiscordEmoji?) {

    constructor(name: String, localizedName: String, emoji: DiscordEmoji?): this(name, localizedName, false, emoji)

    override fun equals(other: Any?): Boolean {
        return other is Language && other.name.equals(name, true) && other.localizedName.equals(localizedName, true)
    }
}