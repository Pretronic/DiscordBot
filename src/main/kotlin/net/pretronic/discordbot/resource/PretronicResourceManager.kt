package net.pretronic.discordbot.resource

import net.pretronic.databasequery.api.query.result.QueryResult
import net.pretronic.discordbot.DiscordBot

class PretronicResourceManager(val discordBot: DiscordBot) {

    val resources = ArrayList<PretronicResource>()

    fun init(): PretronicResourceManager {
        val result: QueryResult = DiscordBot.INSTANCE.storage.resource.find().execute()
        for (entry in result) {
            resources.add(PretronicResource(entry.getInt("Id"), entry.getString("PublicId"), entry.getString("Name")
                    , entry.getInt("SpigotMcResourceId"), entry.getBoolean("Licensed")))
        }

        //startResourceSynchronizer()
        return this
    }

    fun getResource(id: Int): PretronicResource {
        return resources.first { it.id == id }
    }

    fun createDiscordResourceRoles() {
        resources.forEach { it.setDiscordRole() }
    }

    fun startResourceSynchronizer() {
        /*discordBot.scheduler.createTask(ObjectOwner.SYSTEM)
                .interval(1, TimeUnit.MINUTES)
                .delay(30, TimeUnit.SECONDS)
                .async()
                .execute {
                    for (resource in resources) {
                        if (resource.licensed) {
                            if(resource.spigotResource != null) {
                                val spigotResource = resource.spigotResource as PremiumResource
                                for (buyer in SpigotSite.getAPI().resourceManager.getPremiumResourceBuyers(spigotResource, discordBot.config.spigotUser)) {
                                    var user = discordBot.userManager.getUserBySpigotMc(buyer.userId)
                                    if (user == null) user = discordBot.userManager.createUser(buyer.userId, buyer.username)
                                    if (!user.resources.contains(resource)) user.addResource(resource)
                                    user.addVerifiedRoles()
                                }
                            }
                        }
                    }
                }*/
    }
}