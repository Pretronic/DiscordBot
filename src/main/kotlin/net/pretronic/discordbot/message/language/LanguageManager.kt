package net.pretronic.discordbot.message.language

import net.pretronic.discordbot.DiscordBot

class LanguageManager(private val discordBot: DiscordBot) {

    private val languages: MutableCollection<Language> = ArrayList()
    val defaultLanguage: Language
        get() = this.languages.firstOrNull { it.default }?:this.languages.first()

    fun getOrCreate(name: String, localizedName: String): Language {
        var language = languages.firstOrNull { it.name.equals(name, true) && it.localizedName.equals(localizedName, true) }
        if(language == null) {
           language = Language(name, localizedName, null)
            this.languages.add(language)
        }
        return language
    }

    fun loadLanguages() {
        this.languages.addAll(discordBot.config.languages)
    }
}