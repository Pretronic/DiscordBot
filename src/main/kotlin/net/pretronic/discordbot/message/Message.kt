package net.pretronic.discordbot.message

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.pretronic.discordbot.message.embed.EmbedData

data class Message(val key : String, val content : String?, private val embedData: EmbedData?) {

    fun buildEmbed(replacements: Map<String, String>): MessageEmbed? {
        return embedData?.toEmbed(replacements)
    }
}