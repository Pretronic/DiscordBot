package net.pretronic.discordbot.user

import net.pretronic.databasequery.api.dsl.insert
import net.pretronic.databasequery.api.query.result.QueryResult
import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.extensions.replyKey
import net.pretronic.discordbot.message.Messages
import net.pretronic.discordbot.extensions.sendMessageKey
import net.pretronic.libraries.caching.ArrayCache
import net.pretronic.libraries.caching.CacheQuery
import net.pretronic.libraries.utility.interfaces.ObjectOwner
import net.pretronic.spigotsite.api.SpigotSite
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class PretronicUserManager(private val discordBot: DiscordBot) {

    private val users = ArrayCache<PretronicUser>().setRefresh(6, TimeUnit.HOURS)
    private val pendingUserVerifications = ArrayList<PendingUserVerification>()

    val SPIGOT_MC_ID: CacheQuery<PretronicUser> = object : CacheQuery<PretronicUser> {
        override fun check(user: PretronicUser, objects: Array<Any>): Boolean {
            return user.spigotMcId === objects[0] as Int
        }

        override fun load(identifiers: Array<Any>): PretronicUser? {
            val result: QueryResult = DiscordBot.INSTANCE.storage.userTable.find().where("SpigotMcId", identifiers[0]).execute()
            if (!result.isEmpty) {
                val entry = result.first()
                return PretronicUser(entry.getInt("Id"), entry.getInt("SpigotMcId"), entry.getString("SpigotMcName")
                        , entry.getLong("Discord"), null, entry.getString("Key"), null, entry.getObject("Created") as Timestamp)
            }
            return null
        }
    }

    val Discord_ID: CacheQuery<PretronicUser> = object : CacheQuery<PretronicUser> {
        override fun check(user: PretronicUser, objects: Array<Any>): Boolean {
            return user.discordId === objects[0] as Long
        }

        override fun load(identifiers: Array<Any>): PretronicUser? {
            val result: QueryResult = DiscordBot.INSTANCE.storage.userTable.find().where("Discord", identifiers[0]).execute()
            if (!result.isEmpty) {
                val entry = result.first()
                return PretronicUser(entry.getInt("Id"), entry.getInt("SpigotMcId"), entry.getString("SpigotMcName")//@Todo add language in storage
                        , entry.getLong("Discord"), null, entry.getString("Key"), null
                        , entry.getObject("Created") as Timestamp)
            }
            return null
        }
    }

    fun getUserBySpigotMc(spigotMcId: Int): PretronicUser? {
        return users[SPIGOT_MC_ID, spigotMcId]
    }

    fun getUserByDiscord(discordId: Long): PretronicUser? {
        return users[Discord_ID, discordId]
    }

    fun createUser(spigotMcId: Int, spigotMcName: String): PretronicUser {
        val key = generateKey()
        val id: Int = DiscordBot.INSTANCE.storage.userTable.insert()
                .set("SpigotMcId", spigotMcId)
                .set("SpigotMcName", spigotMcName)
                .set("Key", key)
                .set("Created", Date.valueOf(LocalDate.now())).executeAndGetGeneratedKeyAsInt("Id")
        return PretronicUser(id, spigotMcId, spigotMcName, 0, null, key, null, Timestamp(System.currentTimeMillis()))
    }

    fun createPendingVerificationUser(discordId : Long) : PendingUserVerification {
        val secret = UUID.randomUUID().toString().replace("-", "")
        val expiry = System.currentTimeMillis() + discordBot.config.pendingUserVerificationExpiryTime
        this.discordBot.storage.pendingVerification.insert {
            set("Secret", secret)
            set("Discord", discordId)
            set("Expiry", expiry)
        }.execute()
        val verification = PendingUserVerification(secret, discordId, expiry)
        this.pendingUserVerifications.add(verification)
        return verification
    }

    private fun generateKey(): String {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis()
    }

    fun startConversationProcessor() {
        discordBot.scheduler.createTask(ObjectOwner.SYSTEM)
                .interval(30, TimeUnit.SECONDS)
                .delay(30, TimeUnit.SECONDS)
                .async()
                .execute{ checkConversations() }
    }

    fun checkConversations() {
        println("check")
        val conversations = SpigotSite.getAPI().conversationManager.getConversations(discordBot.config.spigotUser, 5)
        println(conversations.size)
        for (conversation in conversations) {
            if (conversation.isUnread) {
                if (conversation.title.startsWith("Authentication ")) {
                    println("auth")
                    val secret: String = conversation.title.replace("Authentication ", "")
                    val pendingUser = pendingUserVerifications.firstOrNull { it.secret == secret }
                    if(pendingUser == null) {
                        conversation.replyKey(discordBot.config.spigotUser, Messages.ACCOUNT_VERIFIED_FAILED)
                    } else {
                        val pretronicUser = pendingUser.complete(conversation.lastReplier.userId, conversation.lastReplier.username)
                        if(pretronicUser != null) {
                            pretronicUser.asUser().openPrivateChannel().queue {
                                it.sendMessageKey(Messages.ACCOUNT_VERIFIED_DISCORD, mapOf(Pair("spigotMcUser", conversation.lastReplier.username))).queue()
                            }
                            conversation.replyKey(discordBot.config.spigotUser, Messages.ACCOUNT_VERIFIED_SPIGOTMC,
                                    mapOf(Pair("discordUser", pretronicUser.asUser().name)))
                        } else {
                            //Expired
                        }

                    }
                    conversation.leave(discordBot.config.spigotUser)
                    Thread.sleep(12000)
                } else if (conversation.getTitle().toLowerCase().startsWith("getuserid")) {
                    val pretronicUser = getUserBySpigotMc(conversation.author.userId)
                    if (pretronicUser != null) {
                        conversation.replyKey(discordBot.config.spigotUser, Messages.SPIGOTMC_GETUSERID_SEND, mapOf(Pair("key", pretronicUser.key)))
                    } else {
                        conversation.replyKey(discordBot.config.spigotUser, Messages.SPIGOTMC_GETUSERID_NOT_EXIST)
                    }
                    conversation.leave(discordBot.config.spigotUser)
                    Thread.sleep(12000) //Wait because of spam
                } else {
                    //@Todo ticket to discord
                    // We can´t get the message with the api
                    // createTicket(conversation, Ticket.TicketType.SPIGOT);
                }
            }
        }
        println("end")
    }
}