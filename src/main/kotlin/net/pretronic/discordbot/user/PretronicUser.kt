package net.pretronic.discordbot.user

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.pretronic.databasequery.api.query.result.QueryResult
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.message.Language
import net.pretronic.discordbot.resource.PretronicResource
import java.sql.Date
import java.sql.Timestamp
import java.util.*
import kotlin.collections.ArrayList

class PretronicUser(val id : Int, var spigotMcId : Int, var spigotMcName : String, var discordId : Long?, var language: Language?, val key : String
                    , val verificationTime : Date?, val created : Timestamp) {

    val resources = ArrayList<PretronicResource>()

    init {
        loadResources()
    }

    fun asUser() : User {
        return DiscordBot.INSTANCE.jda.getUserById(discordId!!)!!
    }

    fun asMember(guild: Guild) : Member {
        return guild.getMemberById(discordId!!)!!
    }

    fun isVerified(): Boolean {
        return !(discordId == null || discordId!! <= 0)
    }

    fun verify(discordId : Long) {
        this.discordId = discordId
        DiscordBot.INSTANCE.storage.userTable.update()
                .set("Discord", discordId)
                .set("VerifiedTime", Date(System.currentTimeMillis()))
                .where("Id", id).execute()
        DiscordBot.INSTANCE.getPretronicGuild()
                .addRoleToMember(discordId, DiscordBot.INSTANCE.getPretronicGuild().getRoleById(DiscordBot.INSTANCE.config.verifiedRole)!!).queue()
    }

    private fun loadResources() {
        try {
            val result = DiscordBot.INSTANCE.storage.licenseTable.find().where("UserId", id).execute()
            for (entry in result) {
                resources.add(DiscordBot.INSTANCE.resourceManager.getResource(entry.getInt("ResourceId")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}