package com.grigroviska.passwordia.viewModel

import com.grigroviska.passwordia.dao.loginRepo

class GetTOTPTimerUseCase(private val repository: loginRepo) {
    fun execute(totpKey: String): String {
        return repository.generateTOTP(totpKey)
    }
}
