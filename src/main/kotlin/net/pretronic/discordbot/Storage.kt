package net.pretronic.discordbot

import net.pretronic.databasequery.api.Database
import net.pretronic.databasequery.api.collection.DatabaseCollection
import net.pretronic.databasequery.api.collection.field.FieldOption
import net.pretronic.databasequery.api.datatype.DataType
import net.pretronic.databasequery.api.dsl.createCollection
import net.pretronic.databasequery.api.query.ForeignKey

class Storage(oldDatabase : Database, botDatabase: Database) {

    val ticket: DatabaseCollection = oldDatabase.createCollection("pretronic_ticket") {
        field { it.name("Id").type(DataType.INTEGER).options(FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT) }
        field { it.name("State").type(DataType.STRING).options(FieldOption.NOT_NULL) }
        field { it.name("ChannelId").type(DataType.LONG).options(FieldOption.NOT_NULL) }
        field { it.name("Language").type(DataType.STRING).options(FieldOption.NOT_NULL) }
        field { it.name("DiscordControlMessageId").type(DataType.LONG).options(FieldOption.NOT_NULL) }
        field { it.name("TopicChooseMessageId").type(DataType.LONG) }
    }.create()
    val ticketParticipants = oldDatabase.createCollection("pretronic_ticket_participants") {
        field { it.name("TicketId").type(DataType.INTEGER).foreignKey(ticket, "Id", ForeignKey.Option.CASCADE)
                .options(FieldOption.NOT_NULL) }
        field { it.name("DiscordUserId").type(DataType.LONG).options(FieldOption.NOT_NULL) }
        field { it.name("Role").type(DataType.STRING).options(FieldOption.NOT_NULL) }
    }.create()


    val pendingVerifications: DatabaseCollection = botDatabase.getCollection("pretronic_bot_pending_verifications")

    val mcnativeResourceOwners: DatabaseCollection = botDatabase.getCollection("pretronic_bot_mcnative_resource_owners")
}