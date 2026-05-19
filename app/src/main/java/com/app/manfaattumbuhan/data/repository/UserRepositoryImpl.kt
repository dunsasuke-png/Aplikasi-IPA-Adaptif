package com.app.manfaattumbuhan.data.repository

import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.domain.model.User
import com.app.manfaattumbuhan.domain.model.UserRole
import com.app.manfaattumbuhan.domain.repository.UserRepository

class UserRepositoryImpl : UserRepository {

    override fun login(username: String, password: String): User? {
        val expectedPassword = StaticData.passwords[username] ?: return null
        if (expectedPassword != password) return null
        val user = StaticData.users.find { it.username == username }
        if (user != null) {
            StaticData.currentUser = user
        }
        return user
    }

    override fun getSiswaList(): List<User> {
        return StaticData.users.filter { it.role == UserRole.SISWA }
    }

    override fun getCurrentUser(): User? {
        return StaticData.currentUser
    }
}
