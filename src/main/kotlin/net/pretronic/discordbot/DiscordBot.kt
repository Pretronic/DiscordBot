package net.pretronic.discordbot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.pretronic.discordbot.message.MessageManager

class DiscordBot {

    companion object {
        lateinit var INSTANCE : DiscordBot
    }

    val config : Config
    val messageManager : MessageManager
    val jda : JDA


    init {
        INSTANCE = this

        this.config = Config()

        this.messageManager = MessageManager()
        this.messageManager.loadPacks()

        this.jda = JDABuilder.create(this.config.botToken, GatewayIntent.values().toList())
                .setAutoReconnect(true)
                .build()
        this.jda.awaitReady()
    }
}