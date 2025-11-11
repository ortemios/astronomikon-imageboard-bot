package ru.ortemios.imagebot.db.datasource

import java.sql.Connection


class UserDataSource(private val connection: Connection) {

    fun setGroup(userId: String, group: String) {
        val stmt = connection.prepareStatement("insert or replace into user_groups (user_id, group_id) values (?, ?)")
        stmt.setString(1, userId)
        stmt.setString(2, group)
        stmt.executeUpdate()
        stmt.close()
    }

    fun getGroup(userId: String): String? {
        val stmt = connection.prepareStatement("select group_id from user_groups where user_id = ?")
        stmt.setString(1, userId)
        val result = stmt.executeQuery()
        val groupId = if (result.next()) {
            result.getString("group_id")
        } else {
            null
        }
        stmt.close()
        return groupId
    }
}