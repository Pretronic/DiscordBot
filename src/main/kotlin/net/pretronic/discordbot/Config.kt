package net.pretronic.discordbot

import net.pretronic.databasequery.api.driver.config.DatabaseDriverConfig
import net.pretronic.databasequery.sql.dialect.Dialect
import net.pretronic.databasequery.sql.driver.config.SQLDatabaseDriverConfigBuilder
import net.pretronic.libraries.document.annotations.DocumentIgnored
import net.pretronic.libraries.utility.duration.DurationProcessor
import net.pretronic.spigotsite.api.SpigotSite
import net.pretronic.spigotsite.api.user.User
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.TimeUnit

class Config {

    var botToken : String = "YOUR TOKEN"

    var databaseName: String = "Pretronic"
    var storage: DatabaseDriverConfig<*> = SQLDatabaseDriverConfigBuilder()
            .setAddress(InetSocketAddress.createUnresolved("127.0.0.1", 3306))
            .setDialect(Dialect.MARIADB)
            .setUsername("root")
            .setPassword("<masked>")
            .build()

    var verifiedRole: Long = 0
    var guildId: Long = 0

    private var pendingUserVerificationExpiry = DurationProcessor.getStandard().formatShort(Duration.ofDays(7))
    @DocumentIgnored var pendingUserVerificationExpiryTime : Long = 0


    private var spigotLogin = "Username"
    private var spigotPassword = "<masked>"
    private var spigotTwoFactorAuth = "<masked>"
    @DocumentIgnored lateinit var spigotUser:User


    fun init() : Config {
        this.pendingUserVerificationExpiryTime = DurationProcessor.getStandard().parse(pendingUserVerificationExpiry).toMillis()
        this.spigotUser = SpigotSite.getAPI().userManager.authenticate(spigotLogin, spigotPassword, spigotTwoFactorAuth)
        return this
    }
}