package net.pretronic.discordbot.user

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.message.language.Language
import net.pretronic.discordbot.resource.PretronicResource
import java.sql.Date
import java.sql.Timestamp

class PretronicUser(val id : Int, var spigotMcId : Int, var spigotMcName : String, var discordId : Long?, val key : String
                    , val verificationTime : Timestamp?, val created : Timestamp) {

    val resources = ArrayList<PretronicResource>()

    init {
        loadResources()
    }

    fun asUser() : User? {
        return discordId?.let { DiscordBot.INSTANCE.jda.getUserById(it) }
    }

    fun asMember(guild: Guild) : Member? {
        return discordId?.let {
            val user = DiscordBot.INSTANCE.jda.getUserById(it)
            if(user == null) return null
            if(guild.isMember(user)) return guild.getMemberById(it) else null
        }
    }

    fun asPretronicMember(): Member? {
        return asMember(DiscordBot.INSTANCE.getPretronicGuild())
    }

    fun isTeamMember(): Boolean {
        asPretronicMember()?.let { member ->
            return member.roles.firstOrNull { it.idLong == DiscordBot.INSTANCE.config.teamRoleId } != null
        }
        return false
    }

    fun isVerified(): Boolean {
        return !(discordId == null || discordId!! <= 0)
    }

    fun verify(discordId : Long) {
        this.discordId = discordId
        loadResources()
        DiscordBot.INSTANCE.storage.user.update()
                .set("Discord", discordId)
                .set("VerifiedTime", Date(System.currentTimeMillis()))
                .where("Id", id).execute()
    }

    fun addVerifiedRoles() {
        if(discordId == null || discordId!! <= 0) return
        val guild = DiscordBot.INSTANCE.getPretronicGuild()

        asPretronicMember()?.let {
            guild.addRoleToMember(it, DiscordBot.INSTANCE.getPretronicGuild().getRoleById(DiscordBot.INSTANCE.config.verifiedRoleId)!!).queue()
        }
        val member = asPretronicMember() ?: return
        resources.forEach { resource ->
            resource.getDiscordRole()?.let {

                if (member.roles.firstOrNull { memberRole -> memberRole.idLong == it.idLong } == null) {
                    guild.addRoleToMember(member, it).queue()
                }
            }
        }
    }

    fun addResource(resource: PretronicResource) {
        this.resources.add(resource)
        addVerifiedRoles()
        DiscordBot.INSTANCE.storage.license.insert()
                .set("UserId", id)
                .set("ResourceId", resource.id)
                .set("Disabled", false)
                .set("Registered", Date(System.currentTimeMillis()))
                .set("MaxInstances", 10)
                .execute()
    }

    private fun loadResources() {
        try {
            val result = DiscordBot.INSTANCE.storage.license.find().where("UserId", id).execute()
            for (entry in result) {
                resources.add(DiscordBot.INSTANCE.resourceManager.getResource(entry.getInt("ResourceId")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        addVerifiedRoles()
    }
}