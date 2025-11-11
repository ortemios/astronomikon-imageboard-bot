package ru.ortemios.imagebot.db.setup

import java.sql.Connection
import java.sql.DriverManager

class ConnectionFactory {

    fun createConnection(dbUrl: String): Connection {
        DatabaseMigrator().migrate(dbUrl)
        val conn = DriverManager.getConnection(dbUrl)
        return conn
    }
}