package net.pretronic.discordbot.message.embed

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.pretronic.discordbot.message.Messages
import java.awt.Color

class EmbedData(val author: EmbedAuthorData?, val description: String?, private val color: String?, val thumbnail: String?) {

    fun toEmbed(replacements: Map<String, String>): MessageEmbed {
        val builder = EmbedBuilder()

        if(author?.name != null) {
            if(author.url != null && author.iconUrl != null) {
                builder.setAuthor(replaceContent(author.name, replacements),
                        author.url,
                        author.iconUrl)
            } else {
                builder.setAuthor(replaceContent(author.name, replacements))
            }
        }
        if(description != null) {
            builder.setDescription(replaceContent(description, replacements))
        }
        if(thumbnail != null) {
            builder.setThumbnail(thumbnail)
        }
        builder.setColor(buildColor())
        return builder.build()
    }

    private fun buildColor(): Color {
        return when(color) {
            "RANDOM_BRIGHT" -> Messages.getRandomBrightColor()
            else -> Color.decode(color)
        }
    }

    private fun replaceContent(message0: String, replacements: Map<String, String>): String {
        var message = message0
        replacements.forEach { message = message.replace("%${it.key}%", it.value) }
        return message
    }
}