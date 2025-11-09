package ru.ortemios.imagebot.domain.service

import ru.ortemios.imagebot.domain.entity.User

class UserService {

    private val userGroups = mutableMapOf<String, String>() // TODO: use permanent storage
    private val authorizedUsers = listOf("ortemios") // TODO: use permanent storage

    fun hasAccess(user: User): Boolean {
        return authorizedUsers.contains(user.id) || authorizedUsers.contains(user.username)
    }

    fun setGroup(userId: String, groupId: String) {
        userGroups[userId] = groupId
    }

    fun getGroup(userId: String): String? {
        return userGroups[userId]
    }
}