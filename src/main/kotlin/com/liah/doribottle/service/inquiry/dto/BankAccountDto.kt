package com.liah.doribottle.service.inquiry.dto

import com.liah.doribottle.domain.inquiry.BankAccount

data class BankAccountDto(
    val bank: String?,
    val accountNumber: String?,
    val accountHolder: String?
) {
    fun toEmbeddable() = BankAccount(bank, accountNumber, accountHolder)
}
