package com.liah.doribottle.domain.inquiry

import com.liah.doribottle.service.inquiry.dto.BankAccountDto
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class BankAccount(
    @Column
    val bank: String?,

    @Column
    val accountNumber: String?,

    @Column
    val accountHolder: String?
) {
    fun toDto() = BankAccountDto(bank, accountNumber, accountHolder)
}