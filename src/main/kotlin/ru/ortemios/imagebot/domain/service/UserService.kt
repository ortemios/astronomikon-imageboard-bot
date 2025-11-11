package ru.ortemios.imagebot.domain.service

import ru.ortemios.imagebot.db.datasource.UserDataSource
import ru.ortemios.imagebot.domain.entity.User

class UserService(private val userDataSource: UserDataSource) {

    private val allowedUsers = System.getenv("ALLOWED_USERS").split(",")

    fun hasAccess(user: User): Boolean {
        return allowedUsers.contains(user.id) || allowedUsers.contains(user.username)
    }

    fun setGroup(userId: String, groupId: String) {
        userDataSource.setGroup(userId, groupId)
    }

    fun getGroup(userId: String): String? {
        return userDataSource.getGroup(userId)
    }
}