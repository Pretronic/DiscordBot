package net.pretronic.discordbot

fun main(args: Array<String>) {

    val discordBot = DiscordBot()

    Runtime.getRuntime().addShutdownHook(Thread {
        discordBot.stop()
    })
}
