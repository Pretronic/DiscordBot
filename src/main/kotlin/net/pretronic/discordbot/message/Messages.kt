package net.pretronic.discordbot.message

import java.awt.Color
import java.util.*

class Messages {
    companion object  {

        private val RANDOM = Random()
        private val MIN_BRIGHTNESS = 0.7f


        val COMMAND_VERIFY_START = "command.verify.start"
        val COMMAND_VERIFY_ALREADY = "command.verify.already"
        val ACCOUNT_VERIFIED_FAILED = "account.verified.failed"
        val ACCOUNT_VERIFIED_DISCORD = "account.verified.discord"
        val ACCOUNT_VERIFIED_SPIGOTMC = "account.verified.spigotmc"
        val SPIGOTMC_GETUSERID_SEND = "spigotmc.getuserid.send"
        val SPIGOTMC_GETUSERID_NOT_EXIST = "spigotmc.getuserid.notExist"
        val DISCORD_GETUSERID_SEND = "discord.getuserid.send"
        val DISCORD_GETUSERID_NOT_EXIST = "discord.getuserid.notExist"
        val DISCORD_TICKET_CONTROL_MESSAGE = "discord.ticket.controlMessage"
        val DISCORD_TICKET_TOPIC_CHOOSE = "discord.ticket.topicChoose"
        val DISCORD_TICKET_ALREADY_CREATED = "discord.ticket.alreadyCreated"
        val DISCORD_TICKET_CLOSED_SELF = "discord.ticket.closed.self"
        val DISCORD_TICKET_PROVIDE_INFORMATION = "discord.ticket.provideInformation"
        val DISCORD_TICKET_PROVIDE_INFORMATION_NEED = "discord.ticket.provideInformation.need"
        val DISCORD_TICKET_PROVIDE_INFORMATION_FINISH = "discord.ticket.provideInformation.finish"
        val DISCORD_TICKET_DISPLAY_INFORMATION = "discord.ticket.displayInformation"
        val DISCORD_TICKET_NOT_OPENED_NOTIFY = "discord.ticket.notOpenedNotify"


        fun getRandomBrightColor(): Color {
            val h: Float = RANDOM.nextFloat()
            val s: Float = RANDOM.nextFloat()
            val b: Float = MIN_BRIGHTNESS + (1f - MIN_BRIGHTNESS) * RANDOM.nextFloat()
            return Color.getHSBColor(h, s, b)
        }
    }
}
