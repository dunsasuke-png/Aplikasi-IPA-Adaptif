package com.app.manfaattumbuhan.domain.usecase

import com.app.manfaattumbuhan.domain.model.User
import com.app.manfaattumbuhan.domain.repository.UserRepository

class GetSiswaUseCase(private val userRepository: UserRepository) {
    fun execute(): List<User> = userRepository.getSiswaList()
}
