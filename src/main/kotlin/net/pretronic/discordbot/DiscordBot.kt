package net.pretronic.discordbot

import com.jagrosh.jdautilities.command.CommandClientBuilder
import io.sentry.Sentry
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.pretronic.databasequery.api.driver.DatabaseDriverFactory
import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig
import net.pretronic.discordbot.commands.setup.SetupCommand
import net.pretronic.discordbot.commands.support.CreateTicketCommand
import net.pretronic.discordbot.config.Config
import net.pretronic.discordbot.message.MessageManager
import net.pretronic.discordbot.message.language.LanguageManager
import net.pretronic.discordbot.ticket.TicketManager
import net.pretronic.discordbot.verification.VerificationManager
import net.pretronic.libraries.concurrent.TaskScheduler
import net.pretronic.libraries.concurrent.simple.SimpleTaskScheduler
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.DocumentRegistry
import net.pretronic.libraries.document.type.DocumentFileType
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets

class DiscordBot {

    companion object {
        lateinit var INSTANCE : DiscordBot
        val logger: Logger = LoggerFactory.getLogger(DiscordBot::class.java)
    }

    val scheduler: TaskScheduler = SimpleTaskScheduler()
    val config : Config
    val languageManager: LanguageManager
    val messageManager: MessageManager
    val storage: Storage
    val jda : JDA
    val ticketManager: TicketManager
    val verificationManager: VerificationManager = VerificationManager(this)

    init {
        //SLF4JStaticBridge.setLogger(this.logger)
        Sentry.init("https://e3c0db053f984827a8618609fd024dfb@o428820.ingest.sentry.io/5374871");

        logger.info("DiscordBot starting...")
        INSTANCE = this

        this.config = initConfig()

        this.languageManager = LanguageManager(this)
        this.languageManager.loadLanguages()

        this.messageManager = MessageManager(this)
        this.messageManager.loadPacks()

        this.storage = initStorage()

        this.ticketManager = TicketManager(this)

        this.jda = initJDA()
        this.config.jdaInit()
        logger.info("DiscordBot successful started")
    }

    fun stop() {
        this.jda.shutdown()
    }

    fun getPretronicGuild() : Guild {
        return config.getPretronicGuild()
    }

    private fun initConfig(): Config {
        DocumentRegistry.getDefaultContext().registerAdapter(DiscordEmoji::class.java, DiscordEmoji.Adapter())

        DatabaseDriverConfig.registerDocumentAdapter()
        val location = File("config.yml")
        if(!location.exists()) {
            location.createNewFile()
            DocumentFileType.YAML.writer.write(location, Document.newDocument(Config()))
        }
        val document = DocumentFileType.YAML.reader.read(File("config.yml"))
        return document.getAsObject(Config::class.java).init()
    }

    private fun initJDA() : JDA {
        val commandClientBuilder = CommandClientBuilder()
                .setPrefix("!")
                .setStatus(config.botOnlineStatus)
                .setActivity(Activity.of(config.botActivityType, config.botActivityName, config.botActivityUrl))
                .addCommand(SetupCommand())
                .addCommand(CreateTicketCommand())
                .setOwnerId("246659669077131264")

        val jda = JDABuilder.create(this.config.botToken, GatewayIntent.values().toList())
                .setAutoReconnect(true)
                .addEventListeners(commandClientBuilder.build(), BotListeners(this))
                //.setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
        jda.awaitReady()
        return jda
    }

    private fun initStorage() : Storage {
        val driver = DatabaseDriverFactory.create("DiscordBot", this.config.storage)
        driver.connect()
        return Storage(driver.getDatabase(this.config.databaseName), driver.getDatabase(this.config.botDatabaseName))
    }

    fun pasteAndGetKey(data: String): String {
        val client: HttpClient = HttpClientBuilder.create().build()
        val post = HttpPost("https://paste.pretronic.net/documents")
        try {
            post.entity = StringEntity(data, StandardCharsets.UTF_8)
            val response = client.execute(post)
            val result = EntityUtils.toString(response.entity)
            return "https://paste.pretronic.net/" + DocumentFileType.JSON.reader.read(result).getString("key")
        } catch (e: IOException) {}
        return "Connection with https://paste.pretronic.net failed!"
    }
}