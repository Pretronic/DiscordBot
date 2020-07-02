package net.pretronic.discordbot

import net.pretronic.databasequery.api.Database
import net.pretronic.databasequery.api.collection.DatabaseCollection
import net.pretronic.databasequery.api.collection.field.FieldOption
import net.pretronic.databasequery.api.datatype.DataType
import net.pretronic.databasequery.api.dsl.createCollection
import net.pretronic.databasequery.api.query.ForeignKey
import net.pretronic.discordbot.ticket.topic.TicketTopic
import net.pretronic.libraries.document.DocumentContext

class Storage(database : Database) {

    val topicContext = DocumentContext.newPreparedContext().apply {
        this.registerAdapter(TicketTopic::class.java, TicketTopic.Adapter())//@Todo fix write and read
    }

    val user: DatabaseCollection = database.getCollection("pretronic_user")
    val license: DatabaseCollection = database.getCollection("pretronic_license")
    val pendingVerification: DatabaseCollection = database.getCollection("pretronic_user_pending_verification")
    val resource: DatabaseCollection = database.getCollection("pretronic_resource")

    val ticket: DatabaseCollection = database.createCollection("pretronic_ticket") {
        field { it.name("Id").type(DataType.INTEGER).options(FieldOption.PRIMARY_KEY, FieldOption.AUTO_INCREMENT) }
        field { it.name("State").type(DataType.STRING).options(FieldOption.NOT_NULL) }
        field { it.name("ChannelId").type(DataType.LONG).options(FieldOption.NOT_NULL) }
        field { it.name("Language").type(DataType.STRING).options(FieldOption.NOT_NULL) }
        field { it.name("DiscordControlMessageId").type(DataType.LONG).options(FieldOption.NOT_NULL) }
        field { it.name("Topics").type(DataType.STRING).defaultValue("{}") }
        field { it.name("TopicChooseMessageId").type(DataType.LONG) }
    }.create()
    val ticketParticipants = database.createCollection("pretronic_ticket_participants") {
        field { it.name("TicketId").type(DataType.INTEGER).foreignKey(ticket, "Id", ForeignKey.Option.CASCADE)
                .options(FieldOption.NOT_NULL) }
        field { it.name("DiscordUserId").type(DataType.LONG).options(FieldOption.NOT_NULL) }
        field { it.name("Role").type(DataType.STRING).options(FieldOption.NOT_NULL) }
    }.create()

}