package net.pretronic.discordbot.config

import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig
import net.pretronic.databasequery.sql.dialect.Dialect
import net.pretronic.databasequery.sql.driver.config.SQLDatabaseDriverConfigBuilder
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.DiscordEmoji
import net.pretronic.discordbot.message.language.Language
import net.pretronic.discordbot.ticket.topic.TicketTopic
import net.pretronic.discordbot.ticket.topic.TicketTopicContent
import net.pretronic.libraries.document.annotations.DocumentIgnored
import net.pretronic.libraries.utility.duration.DurationProcessor
import net.pretronic.spigotsite.api.user.User
import java.net.InetSocketAddress
import java.time.Duration

class Config {

    var botToken : String = "YOUR TOKEN"

    var databaseName: String = "Pretronic"
    var botDatabaseName: String = "pretronic-bot"
    var storage: DatabaseDriverConfig<*> = SQLDatabaseDriverConfigBuilder()
            .setAddress(InetSocketAddress.createUnresolved("127.0.0.1", 3306))
            .setDialect(Dialect.MARIADB)
            .setUsername("root")
            .setPassword("<masked>")
            .build()

    var botOnlineStatus = OnlineStatus.ONLINE
    var botActivityType = Activity.ActivityType.WATCHING
    var botActivityName = "https://pretronic.net"
    var botActivityUrl: String? = "https://paste.pretronic.net"


    var verifiedRoleId: Long = 0
    val verifiedRole: Role?
        get() = getPretronicGuild().getRoleById(verifiedRoleId)
    var teamRoleId: Long = 0
    @DocumentIgnored lateinit var teamRole: Role
    var guildId: Long = 0

    var ticketLogChannelId: Long = 0

    var ticketCreateMessageId: Long = 0
    var ticketTopics: List<TicketTopic> = listOf()

    private var ticketCategoryId: Long = 0
    @DocumentIgnored lateinit var ticketCategory: Category
    var ticketCloseEmoji = DiscordEmoji("\uD83D\uDD12")

    var ticketProvideInformationFinishEmoji = DiscordEmoji("✅")
    var ticketProvideInformationNextTopicEmoji = DiscordEmoji("➡️")


    var languages: Collection<Language> = listOf(Language("English", "en", true, DiscordEmoji("\t\uD83C\uDDEC\uD83C\uDDE7"))
            , Language("Deutsch", "de", false, DiscordEmoji("\uD83C\uDDE9\uD83C\uDDEA")))

    val channelAutoEmojis: Collection<ChannelAutoEmoji> = listOf()


    val subscribeGroupEmoji: Collection<SubscribeGroupEmoji> = listOf()


    val resourceRoleIds: Map<String, Long> = mapOf()

    fun init() : Config {
        subscribeGroupEmoji.forEach {
            it.init()
        }
        channelAutoEmojis.forEach {
            it.init()
        }
        return this
    }

    fun jdaInit() {
        this.ticketCategory = DiscordBot.INSTANCE.jda.getCategoryById(this.ticketCategoryId)!!
        this.teamRole = DiscordBot.INSTANCE.getPretronicGuild().getRoleById(this.teamRoleId)!!

        println("Custom emotes:")
        getPretronicGuild().emotes.forEach {
            println("${it.idLong} | ${it.name}")
        }
    }

    fun ticketTopicByName(name: String): TicketTopic {
        return ticketTopics.first { it.name == name }
    }

    fun getAccessAbleTicketTopics(discordId: Long, topics: Collection<TicketTopicContent>): List<TicketTopic> {
        val accessAble = ArrayList<TicketTopic>()
        val member = getPretronicGuild().getMemberById(discordId)
        ticketTopics.forEach {
            if(topics.firstOrNull { content -> content.topic == it } == null) {
                it.roleIds?.let { roleIds ->
                    member?.let { member ->
                        var added = false
                        roleIds.forEach { roleId ->
                            if(member.roles.any { role -> role.idLong == roleId }) {
                                if(!added) {
                                    accessAble.add(it)
                                    added = true
                                }
                            }
                        }
                    }
                }?:accessAble.add(it)
            }
        }
        return accessAble
    }

    fun getPretronicGuild() : Guild {
        return DiscordBot.INSTANCE.jda.getGuildById(guildId)!!
    }

    fun getResourceRole(resourceId: String): Role? {
        return this.resourceRoleIds[resourceId]?.let {
            getPretronicGuild().getRoleById(it)
        }
    }
}