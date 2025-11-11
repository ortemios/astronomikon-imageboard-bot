package ru.ortemios.imagebot.db.setup

import org.flywaydb.core.Flyway

class DatabaseMigrator {
    fun migrate(dbUrl: String) {
        val flyway = Flyway.configure()
            .dataSource(dbUrl, null, null)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
        flyway.migrate()
    }
}