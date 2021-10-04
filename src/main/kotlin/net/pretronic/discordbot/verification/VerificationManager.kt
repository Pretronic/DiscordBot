package net.pretronic.discordbot.verification

import net.dv8tion.jda.api.entities.Member
import net.pretronic.databasequery.api.dsl.find
import net.pretronic.discordbot.DiscordBot
import net.pretronic.libraries.utility.interfaces.ObjectOwner
import java.util.concurrent.TimeUnit

class VerificationManager(private val discordBot: DiscordBot) {

    init {
        discordBot.scheduler.createTask(ObjectOwner.SYSTEM)
            .delay(1, TimeUnit.MINUTES)
            .interval(1, TimeUnit.MINUTES)
            .execute(this::scheduleVerifications)

        discordBot.scheduler.createTask(ObjectOwner.SYSTEM)
            .delay(1, TimeUnit.MINUTES)
            .interval(1, TimeUnit.MINUTES)
            .execute(this::scheduleResourceUserRoles)
    }

    fun checkMemberRoles(member: Member) {
        checkVerifiedRole(member)
        checkResourceRoles(member)
    }

    private fun checkVerifiedRole(member: Member) {
        val isVerified: Boolean = !this.discordBot.storage.accountUsers.find {
            get("DiscordId")
            where("DiscordId", member.idLong)
        }.execute().isEmpty
        val hasVerifiedRole = member.roles.firstOrNull { it.idLong == DiscordBot.INSTANCE.config.verifiedRoleId } != null
        if(isVerified && !hasVerifiedRole) {
            member.guild.addRoleToMember(member, DiscordBot.INSTANCE.config.verifiedRole).queue()
        } else if(!isVerified && hasVerifiedRole) {
            member.guild.removeRoleFromMember(member, DiscordBot.INSTANCE.config.verifiedRole).queue()
        }
    }

    private fun checkResourceRoles(member: Member) {
        val resourceIds: Collection<String> = this.discordBot.storage.mcnativeResourceOwners.find {
            get("ResourceId")
            where("DiscordUserId", member.idLong)
        }.execute().map { it.getString("ResourceId") }

        resourceIds.forEach { resourceId ->
            this.discordBot.config.getResourceRole(resourceId)?.let { role ->
                val hasRole = member.roles.firstOrNull { it.idLong == role.idLong } != null

                if(!hasRole) {
                    member.guild.addRoleToMember(member, role).queue()
                }
            }
        }


        member.roles.forEach {
            val resourceId = DiscordBot.INSTANCE.config.getResourceIdByRoleId(it.idLong)
            if(resourceId != null) {
                if(!resourceIds.contains(resourceId)) {
                    member.guild.removeRoleFromMember(member, it).queue()
                }
            }
        }
    }

    private fun scheduleVerifications() {
        this.discordBot.storage.accountUsers.find {
            get("DiscordId")
        }.execute().forEach { verify(it.getLong("DiscordId")) }
    }

    private fun verify(discordId: Long) {
        println("MemberId:")
        this.discordBot.getPretronicGuild().retrieveMemberById(discordId).queue ({
            discordBot.config.verifiedRole.let { role ->
                it.guild.addRoleToMember(it, role).queue()
            }
        })
    }

    private fun scheduleResourceUserRoles() {
        this.discordBot.storage.mcnativeResourceOwners.find {
            get("ResourceId")
            get("DiscordUserId")
        }.execute().forEach { checkResourceRoleAdd(it.getString("ResourceId"), it.getLong("DiscordUserId")) }
    }

    private fun checkResourceRoleAdd(resourceId: String, discordId: Long) {
        this.discordBot.getPretronicGuild().retrieveMemberById(discordId).queue ({ member ->
            this.discordBot.config.getResourceRole(resourceId)?.let { role ->
                member.guild.addRoleToMember(member, role).queue()
            }
        })
    }
}