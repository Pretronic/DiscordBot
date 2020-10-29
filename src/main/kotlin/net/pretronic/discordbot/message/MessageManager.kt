package net.pretronic.discordbot.message

import net.pretronic.discordbot.DiscordBot
import net.pretronic.discordbot.message.embed.EmbedAuthorData
import net.pretronic.discordbot.message.embed.EmbedData
import net.pretronic.discordbot.message.language.Language
import net.pretronic.libraries.document.Document
import net.pretronic.libraries.document.entry.PrimitiveEntry
import net.pretronic.libraries.document.type.DocumentFileType
import net.pretronic.libraries.utility.io.FileUtil
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class MessageManager(private val discordBot: DiscordBot) {

    private val packs = ArrayList<MessagePack>()

    fun getMessage(language0: Language?, messageKey : String) : Message {
        val language : Language = language0 ?: discordBot.languageManager.defaultLanguage
        val pack : MessagePack = packs.firstOrNull { it.language == language }?:getDefaultPack()

        return pack.messages.firstOrNull { it.key == messageKey}?: getDefaultPack().messages.firstOrNull { it.key == messageKey }
            ?: throw IllegalArgumentException("No message for key: $messageKey found")
    }

    private fun getDefaultPack() : MessagePack {
        return packs.first { it.language == discordBot.languageManager.defaultLanguage }
    }

    private fun getDefaultLanguage(): Language {
        return discordBot.languageManager.defaultLanguage
    }

    fun loadPacks() {
        val messagesPath = File("messages/")
        if(!messagesPath.exists()) {
            messagesPath.mkdirs()
        }
        if(messagesPath.listFiles().isEmpty()) {
            Files.copy(this::class.java.getResourceAsStream("/messages/English_en.yml"), Paths.get("messages/English_en.yml"))
            Files.copy(this::class.java.getResourceAsStream("/messages/Deutsch_de.yml"), Paths.get("messages/Deutsch_de.yml"))
        }
        FileUtil.processFilesHierarchically(messagesPath) {
            val splitted = it.nameWithoutExtension.split("_")
            val language = discordBot.languageManager.getOrCreate(splitted[0], splitted[1])
            loadPack(language, DocumentFileType.YAML.reader.read(it))
        }
        DiscordBot.logger.info("Loaded ${this.packs.size} packs")
    }

    private fun loadPack(language: Language, document : Document) {
        val messages = ArrayList<Message>()
        document.getDocument("messages").forEach {
            /*
            it as PrimitiveEntry

             */
            if(it is PrimitiveEntry) {
                val message = Message(it.key, it.asString, null)
                messages.add(message)
            } else {
                it as Document

                var embedAuthorData: EmbedAuthorData? = null

                if(it.contains("author")) {
                    val authorData = it.getDocument("author")
                    if(authorData.contains("url") && authorData.contains("iconUrl")) {
                        embedAuthorData = EmbedAuthorData(authorData.getString("name"),
                                authorData.getString("url"),
                                authorData.getString("iconUrl"))
                    } else {
                        embedAuthorData = EmbedAuthorData(authorData.getString("name"), null, null)
                    }
                }
                val description = if(it.contains("description")) it.getString("description") else null
                val thumbnail = if(it.contains("thumbnail")) it.getString("thumbnail") else null

                messages.add(Message(it.key, null, EmbedData(embedAuthorData, description, it.getString("color"), thumbnail)))
            }
        }
        val messagePack = MessagePack(language, messages)
        this.packs.add(messagePack)
    }
}