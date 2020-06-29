package net.pretronic.discordbot.ticket

enum class TicketParticipantRole {

    CREATOR,
    TEAM;

    companion object {

        fun parse(value: String): TicketParticipantRole {
            return when(value.toLowerCase()) {
                "creator" -> CREATOR
                else -> TEAM
            }
        }
    }
}