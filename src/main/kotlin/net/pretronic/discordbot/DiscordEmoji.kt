package net.pretronic.discordbot

import com.vdurmont.emoji.Emoji
import com.vdurmont.emoji.EmojiManager
import net.dv8tion.jda.api.entities.MessageReaction
import net.pretronic.discordbot.extensions.unicodeName
import net.pretronic.libraries.document.adapter.DocumentAdapter
import net.pretronic.libraries.document.entry.DocumentBase
import net.pretronic.libraries.document.entry.DocumentEntry
import net.pretronic.libraries.document.entry.PrimitiveEntry
import net.pretronic.libraries.document.simple.SimplePrimitiveEntry
import net.pretronic.libraries.utility.reflect.TypeReference

class DiscordEmoji(val id: Long?, val name: String?) {

    constructor(id: Long): this(id, null)

    constructor(name: String): this(null, name)

    fun isDiscordEmoji(emoji: MessageReaction.ReactionEmote): Boolean {
        if(emoji.isEmote) return emoji.idLong == id
        return emoji.unicodeName() == name
    }

    fun toEmoji(): Emoji? {
        if(name != null) return EmojiManager.getForAlias(this.name)
        return null
    }

    class Adapter: DocumentAdapter<DiscordEmoji> {

        override fun write(key: String, emoji: DiscordEmoji): DocumentEntry {
            val value = emoji.name ?: emoji.id
            return SimplePrimitiveEntry(key, value)
        }

        override fun read(base: DocumentBase, type: TypeReference<DiscordEmoji>): DiscordEmoji {
            base as PrimitiveEntry
            if(base.asObject is Long) return DiscordEmoji(base.asLong)
            return DiscordEmoji(base.asString)
        }
    }
}