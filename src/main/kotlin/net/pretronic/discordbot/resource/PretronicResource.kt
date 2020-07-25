package net.pretronic.discordbot.resource

import net.dv8tion.jda.api.entities.Role
import net.pretronic.discordbot.DiscordBot
import net.pretronic.spigotsite.api.SpigotSite
import net.pretronic.spigotsite.api.resource.Resource

class PretronicResource(val id: Int, val publicId: String, val name: String, val spigotMcResourceId: Int, val licensed: Boolean) {

    //val spigotResource: Resource? = if(spigotMcResourceId > 0) SpigotSite.getAPI().resourceManager.getResourceById(spigotMcResourceId, DiscordBot.INSTANCE.config.spigotUser) else null
    private var discordRoleId: Long? = null

    fun getDiscordRole(): Role? {
        val role = if(this.discordRoleId != null) DiscordBot.INSTANCE.getPretronicGuild().getRoleById(this.discordRoleId!!) else null
        return role
    }

    fun setDiscordRole() {
        val guild = DiscordBot.INSTANCE.getPretronicGuild()

        val availableRoles = guild.getRolesByName(this.name, true)
        if(availableRoles.isEmpty()) {
            guild.createRole().setName(this.name).queue {
                this.discordRoleId = it.idLong
            }
        } else {
            this.discordRoleId = availableRoles[0].idLong
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is PretronicResource && other.id == id
    }
}