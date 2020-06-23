package net.pretronic.discordbot.resource

import net.pretronic.discordbot.DiscordBot
import net.pretronic.spigotsite.api.SpigotSite
import net.pretronic.spigotsite.api.resource.PremiumResource
import net.pretronic.spigotsite.api.resource.Resource

class PretronicResource(val id: Int, val publicId: String, val name: String, val spigotMcResourceId: Int, val licensed: Boolean) {

    val spigotResource: Resource? = if(spigotMcResourceId > 0) SpigotSite.getAPI().resourceManager.getResourceById(spigotMcResourceId, DiscordBot.INSTANCE.config.spigotUser) else null

    override fun equals(other: Any?): Boolean {
        return other != null && other is PretronicResource && other.id == id
    }
}