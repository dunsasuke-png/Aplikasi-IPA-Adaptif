package com.app.manfaattumbuhan.domain.repository

import com.app.manfaattumbuhan.domain.model.User

interface UserRepository {
    fun login(username: String, password: String): User?
    fun getSiswaList(): List<User>
    fun getCurrentUser(): User?
}
