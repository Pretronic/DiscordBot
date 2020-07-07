package net.pretronic.discordbot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.requests.GatewayIntent
import net.pretronic.databasequery.api.driver.DatabaseDriverFactory
import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig
import net.pretronic.discordbot.commands.GetUserIdCommand
import net.pretronic.discordbot.commands.VerifyCommand
import net.pretronic.discordbot.commands.setup.SetupCommand
import net.pretronic.discordbot.message.MessageManager
import net.pretronic.discordbot.message.language.LanguageManager
import net.pretronic.discordbot.resource.PretronicResourceManager
import net.pretronic.discordbot.ticket.TicketManager
import net.pretronic.discordbot.user.PretronicUserManager
import net.pretronic.libraries.concurrent.TaskScheduler
import net.pretronic.libraries.concurrent.simple.SimpleTaskScheduler
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.DocumentRegistry
import net.pretronic.libraries.document.type.DocumentFileType
import net.pretronic.libraries.logging.PretronicLogger
import net.pretronic.libraries.logging.PretronicLoggerFactory
import net.pretronic.libraries.logging.bridge.slf4j.SLF4JStaticBridge
import net.pretronic.spigotsite.api.SpigotSite
import net.pretronic.spigotsite.core.SpigotSiteCore
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

class DiscordBot {

    companion object {
        lateinit var INSTANCE : DiscordBot
    }

    val logger: PretronicLogger = PretronicLoggerFactory.getLogger("DiscordBot")

    val scheduler: TaskScheduler = SimpleTaskScheduler()
    val config : Config
    val languageManager: LanguageManager
    val messageManager: MessageManager
    val storage: Storage
    val jda : JDA
    val resourceManager: PretronicResourceManager
    val userManager: PretronicUserManager
    val ticketManager: TicketManager

    init {
        SLF4JStaticBridge.setLogger(this.logger)
        logger.info("DiscordBot starting...")
        INSTANCE = this
        SpigotSite.setAPI(SpigotSiteCore())

        this.config = initConfig()

        this.languageManager = LanguageManager(this)
        this.languageManager.loadLanguages()

        this.messageManager = MessageManager(this)
        this.messageManager.loadPacks()

        this.storage = initStorage()


        this.resourceManager = PretronicResourceManager(this).init()

        this.userManager = PretronicUserManager(this)
        this.userManager.startConversationProcessor()

        this.ticketManager = TicketManager(this)

        this.jda = initJDA()
        this.config.jdaInit()
        this.resourceManager.createDiscordResourceRoles()
        logger.info("DiscordBot successful started")
    }

    fun stop() {
        this.jda.shutdown()
    }

    fun getPretronicGuild() : Guild {
        return jda.getGuildById(config.guildId)!!
    }

    private fun initConfig(): Config {
        DocumentRegistry.getDefaultContext().registerAdapter(DiscordEmoji::class.java, DiscordEmoji.Adapter())

        DatabaseDriverConfig.registerDocumentAdapter()
        val location = File("configurations/config.yml")
        if(!location.exists()) {
            location.createNewFile()
            DocumentFileType.YAML.writer.write(location, Document.newDocument(Config()))
        }
        val document = DocumentFileType.YAML.reader.read(File("configurations/config.yml"))
        return document.getAsObject(Config::class.java).init()
    }

    private fun initJDA() : JDA {
        val commandClientBuilder = CommandClientBuilder()
                .setPrefix("!")
                .addCommand(VerifyCommand(this))
                .addCommand(GetUserIdCommand(this))
                .addCommand(SetupCommand())
                .setOwnerId("246659669077131264")

        val jda = JDABuilder.create(this.config.botToken, GatewayIntent.values().toList())
                .setAutoReconnect(true)
                .setStatus(config.botOnlineStatus)
                .setActivity(Activity.of(config.botActivityType, config.botActivityName, config.botActivityUrl))
                .addEventListeners(commandClientBuilder.build(), BotListeners(this))
                .build()
        jda.awaitReady()


        return jda
    }

    private fun initStorage() : Storage {
        val driver = DatabaseDriverFactory.create("DiscordBot", this.config.storage, this.logger)
        driver.connect()
        return Storage(driver.getDatabase(this.config.databaseName))
    }

    fun pasteAndGetKey(data: String): String {
        val client: HttpClient = HttpClientBuilder.create().build()
        val post = HttpPost("https://paste.pretronic.net/documents")
        try {
            post.entity = StringEntity(data, StandardCharsets.UTF_8)
            val response = client.execute(post)
            val result = EntityUtils.toString(response.entity)
            return "https://paste.pretronic.net/" + DocumentFileType.JSON.reader.read(result).getString("key")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return "Connection with https://paste.pretronic.net failed!"
    }
}