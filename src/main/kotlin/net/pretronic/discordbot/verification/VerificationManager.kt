package net.pretronic.discordbot.verification

import net.pretronic.databasequery.api.dsl.delete
import net.pretronic.databasequery.api.dsl.find
import net.pretronic.databasequery.api.dsl.insert
import net.pretronic.discordbot.DiscordBot
import net.pretronic.libraries.utility.interfaces.ObjectOwner
import java.util.concurrent.TimeUnit

class VerificationManager(private val discordBot: DiscordBot) {

    init {
        discordBot.scheduler.createTask(ObjectOwner.SYSTEM)
            .delay(1, TimeUnit.MINUTES)
            .interval(1, TimeUnit.MINUTES)
            .execute(this::checkPendingVerifications)

        discordBot.scheduler.createTask(ObjectOwner.SYSTEM)
            .delay(1, TimeUnit.MINUTES)
            .interval(1, TimeUnit.MINUTES)
            .execute(this::checkResourceRole)
    }

    private fun checkPendingVerifications() {
        this.discordBot.storage.pendingVerifications.find {
            get("DiscordId")
            where("AutoCheck", true)
        }.execute().forEach { verify(it.getLong("DiscordId")) }
    }

    fun checkPendingVerification(discordId: Long) {
        this.discordBot.storage.pendingVerifications.find {
            get("DiscordId")
            where("DiscordId", discordId)
        }.execute().forEach { _ -> verify(discordId) }
    }

    private fun verify(discordId: Long) {
        this.discordBot.getPretronicGuild().getMemberById(discordId)?.let {
            discordBot.config.verifiedRole?.let { role ->
                it.guild.addRoleToMember(it, role).queue {
                    discordBot.storage.pendingVerifications.delete {
                        where("DiscordId", discordId)
                    }.execute()
                }
            }
        }
    }

    fun addPendingVerification(discordId: Long, autoCheck: Boolean = false) {
        this.discordBot.storage.pendingVerifications.insert {
            set("DiscordId", discordId)
            set("AutoCheck", autoCheck)
        }.execute()
    }

    private fun checkResourceRole() {
        this.discordBot.storage.mcnativeResourceOwners.find {
            get("ResourceId")
            get("DiscordUserId")
        }.execute().forEach { checkResourceRoleAdd(it.getString("ResourceId"), it.getLong("DiscordUserId")) }

    }

    private fun checkResourceRoleAdd(resourceId: String, discordId: Long) {
        this.discordBot.getPretronicGuild().getMemberById(discordId)?.let { member ->
            this.discordBot.config.getResourceRole(resourceId)?.let { role ->
                member.guild.addRoleToMember(member, role).queue()
            }
        }

    }
}