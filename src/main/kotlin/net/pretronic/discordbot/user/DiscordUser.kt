package net.pretronic.discordbot.user

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.message.Language

class DiscordUser(val discordId : Long, val language: Language) {

    fun getAsUser() : User {
        return DiscordBot.INSTANCE.jda.getUserById(discordId)!!
    }

    fun getAsMember(guild: Guild) : Member {
        return guild.getMemberById(discordId)!!
    }
}