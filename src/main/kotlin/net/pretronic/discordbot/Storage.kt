package net.pretronic.discordbot

import net.pretronic.databasequery.api.Database
import net.pretronic.databasequery.api.collection.DatabaseCollection

class Storage(database : Database) {

    val userTable: DatabaseCollection = database.getCollection("pretronic_user");
    val licenseTable: DatabaseCollection = database.getCollection("pretronic_license");
    val pendingVerification: DatabaseCollection = database.getCollection("pretronic_user_pending_verification");
    val resourceTable: DatabaseCollection = database.getCollection("pretronic_resource");
}