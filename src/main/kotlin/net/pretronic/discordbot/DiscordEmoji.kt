package net.pretronic.discordbot

import net.dv8tion.jda.api.entities.MessageReaction
import net.pretronic.libraries.document.adapter.DocumentAdapter
import net.pretronic.libraries.document.entry.DocumentBase
import net.pretronic.libraries.document.entry.DocumentEntry
import net.pretronic.libraries.document.entry.PrimitiveEntry
import net.pretronic.libraries.document.simple.SimplePrimitiveEntry
import net.pretronic.libraries.utility.Convert
import net.pretronic.libraries.utility.reflect.TypeReference

class DiscordEmoji(val id: Long? = null, val unicode: String? = null) {

    companion object {
        fun of(value: Any): DiscordEmoji {
            if(value is String || value is Char) {
                return DiscordEmoji(unicode = value.toString())
            }
            return DiscordEmoji(id=Convert.toLong(value))
        }
    }

    constructor(id: Long): this(id, null)

    constructor(name: String): this(null, name)

    fun isDiscordEmoji(emoji: MessageReaction.ReactionEmote): Boolean {
        if(emoji.isEmote) return emoji.idLong == id
        return emoji.name == unicode
    }

    class Adapter: DocumentAdapter<DiscordEmoji> {

        override fun write(key: String, emoji: DiscordEmoji): DocumentEntry {
            val value = emoji.unicode ?: emoji.id
            return SimplePrimitiveEntry(key, value)
        }

        override fun read(base: DocumentBase, type: TypeReference<DiscordEmoji>): DiscordEmoji {
            base as PrimitiveEntry
            return try {
                val id = Convert.toLong(base.asObject)
                DiscordEmoji(id)
            } catch (exception: IllegalArgumentException) {
                DiscordEmoji(base.asString)
            }
        }
    }
}