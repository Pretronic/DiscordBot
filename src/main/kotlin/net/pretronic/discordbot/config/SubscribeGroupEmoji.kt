package net.pretronic.discordbot.config

import com.vdurmont.emoji.EmojiManager
import net.pretronic.discordbot.DiscordEmoji
import net.pretronic.libraries.document.annotations.DocumentIgnored
import net.pretronic.libraries.utility.Convert

class SubscribeGroupEmoji(val messageId: Long, private val roles: Collection<String>) {

    @DocumentIgnored
    lateinit var emojiRoles: MutableMap<DiscordEmoji, Long>

    fun init() {
        emojiRoles = HashMap()
        roles.forEach {
            val split = it.split(":")
            this.emojiRoles[DiscordEmoji.of(EmojiManager.getByUnicode(split[0]).unicode)] = Convert.toLong(split[1])
        }
    }
}