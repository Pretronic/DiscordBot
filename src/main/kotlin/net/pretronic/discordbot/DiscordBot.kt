package net.pretronic.discordbot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.requests.GatewayIntent
import net.pretronic.databasequery.api.driver.DatabaseDriverFactory
import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig
import net.pretronic.discordbot.commands.VerifyCommand
import net.pretronic.discordbot.message.MessageManager
import net.pretronic.discordbot.resource.PretronicResourceManager
import net.pretronic.discordbot.user.PretronicUserManager
import net.pretronic.libraries.concurrent.TaskScheduler
import net.pretronic.libraries.concurrent.simple.SimpleTaskScheduler
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.type.DocumentFileType
import net.pretronic.libraries.logging.PretronicLogger
import net.pretronic.libraries.logging.PretronicLoggerFactory
import net.pretronic.libraries.logging.bridge.slf4j.SLF4JStaticBridge
import net.pretronic.libraries.logging.level.DebugLevel
import net.pretronic.spigotsite.api.SpigotSite
import net.pretronic.spigotsite.core.SpigotSiteCore
import java.io.File
import kotlin.math.log

class DiscordBot {

    companion object {
        lateinit var INSTANCE : DiscordBot
    }

    val logger: PretronicLogger = PretronicLoggerFactory.getLogger("DiscordBot")
    val scheduler: TaskScheduler = SimpleTaskScheduler()
    val config : Config
    val messageManager: MessageManager
    val storage: Storage
    val jda : JDA
    val resourceManager: PretronicResourceManager
    val userManager: PretronicUserManager

    init {
        logger.info("DiscordBot starting...")
        INSTANCE = this
        this.logger.debugLevel = DebugLevel.ALL
        SpigotSite.setAPI(SpigotSiteCore())

        this.config = initConfig()

        this.messageManager = MessageManager(this)
        this.messageManager.loadPacks()

        this.storage = initStorage()


        this.resourceManager = PretronicResourceManager(this).init()

        this.userManager = PretronicUserManager(this)
        this.userManager.startConversationProcessor()

        this.jda = initJDA()
        logger.info("DiscordBot successful started")
    }

    fun stop() {
        this.jda.shutdown()
    }

    fun getPretronicGuild() : Guild {
        return jda.getGuildById(config.guildId)!!
    }

    private fun initConfig(): Config {
        DatabaseDriverConfig.registerDocumentAdapter()
        val location = File("config.yml")
        if(!location.exists()) {
            location.createNewFile()
            DocumentFileType.YAML.writer.write(location, Document.newDocument(Config()))
        }
        return DocumentFileType.YAML.reader.read(File("config.yml")).getAsObject(Config::class.java).init()
    }

    private fun initJDA() : JDA {
        val commandClientBuilder = CommandClientBuilder()
                .setPrefix("!")
                .addCommand(VerifyCommand(this))
                .setOwnerId("246659669077131264")

        val jda = JDABuilder.create(this.config.botToken, GatewayIntent.values().toList())
                .setAutoReconnect(true)
                .addEventListeners(commandClientBuilder.build())
                .build()
        jda.awaitReady()
        return jda
    }

    private fun initStorage() : Storage {
        SLF4JStaticBridge.setLogger(this.logger)
        val driver = DatabaseDriverFactory.create("DiscordBot", this.config.storage, this.logger)
        driver.connect()
        return Storage(driver.getDatabase(this.config.databaseName))
    }
}