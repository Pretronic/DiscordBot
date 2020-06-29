package net.pretronic.discordbot.user

import net.pretronic.discordbot.DiscordBot

data class PendingUserVerification(val secret : String, val discordId : Long, val expiry : Long) {

    fun complete(spigotMcId: Int, spigotMcName: String): PretronicUser? {
        if(System.currentTimeMillis() > expiry) return null
        DiscordBot.INSTANCE.storage.pendingVerification.delete().where("Secret", secret).execute()
        var user = DiscordBot.INSTANCE.userManager.getUserBySpigotMc(spigotMcId)
        if(user == null) {
            user = DiscordBot.INSTANCE.userManager.createUser(spigotMcId, spigotMcName)
        }
        if(!user.isVerified()) {
            user.verify(discordId)
        }
        return user
    }
}