package net.pretronic.discordbot.message

data class Language(val name : String, val localizedName : String) {

    override fun equals(other: Any?): Boolean {
        return other is Language&& other.name.equals(name, true) && other.localizedName.equals(localizedName, true)
    }
}