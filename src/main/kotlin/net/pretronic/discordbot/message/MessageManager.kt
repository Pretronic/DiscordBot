package net.pretronic.discordbot.message

import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.user.PretronicUser
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.entry.PrimitiveEntry
import net.pretronic.libraries.document.type.DocumentFileType
import net.pretronic.libraries.utility.io.FileUtil
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class MessageManager(private val discordBot: DiscordBot) {

    private val defaultLanguage : Language = Language("English", "en")
    private val packs = ArrayList<MessagePack>()

    fun getMessage(pretronicUser: PretronicUser, messageKey: String) : Message {
        return getMessage(pretronicUser.language, messageKey)
    }

    fun getMessage(language0: Language?, messageKey : String) : Message {
        val language : Language = language0 ?: this.defaultLanguage
        val pack : MessagePack = packs.firstOrNull { it.language == language }?:getDefaultPack()

        return pack.messages.firstOrNull { it.messageKey == messageKey}?: getDefaultPack().messages.first { it.messageKey == messageKey }
    }

    private fun getDefaultPack() : MessagePack {
        return packs.first { it.language == this.defaultLanguage }
    }

    fun loadPacks() {
        val messagesPath = File("messages/")
        if(!messagesPath.exists()) {
            messagesPath.mkdirs()
        }
        if(messagesPath.listFiles().isEmpty()) {
            Files.copy(this::class.java.getResourceAsStream("/messages/English_en.yml"), Paths.get("messages/English_en.yml"))
        }
        FileUtil.processFilesHierarchically(messagesPath) {
            val splitted = it.nameWithoutExtension.split("_")
            val language = Language(splitted[0], splitted[1])
            loadPack(language, DocumentFileType.YAML.reader.read(it))
        }
        discordBot.logger.info("Loaded ${this.packs.size} packs")
    }

    private fun loadPack(language: Language, document : Document) {
        val messages = ArrayList<Message>()
        document.getDocument("messages").forEach {
            it as PrimitiveEntry
            val message = Message(it.key, it.asString)
            messages.add(message)
        }
        val messagePack = MessagePack(language, messages)
        this.packs.add(messagePack)
    }
}