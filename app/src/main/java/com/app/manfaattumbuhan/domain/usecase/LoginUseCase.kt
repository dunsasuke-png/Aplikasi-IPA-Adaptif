package com.app.manfaattumbuhan.domain.usecase

import com.app.manfaattumbuhan.domain.model.User
import com.app.manfaattumbuhan.domain.repository.UserRepository

class LoginUseCase(private val userRepository: UserRepository) {
    fun execute(username: String, password: String): User? {
        return userRepository.login(username, password)
    }
}
