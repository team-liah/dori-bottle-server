package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.repository.payment.PaymentStatisticDao

data class PaymentStatisticDto(
    val date: String,
    val savePointAmount: Long,
    val lostCupAmount: Long,
    val unblockAccountAmount: Long,
    val cancelAmount: Long
) {
    companion object {
        fun fromDao(dao: PaymentStatisticDao): PaymentStatisticDto {
            return PaymentStatisticDto(
                date = dao.date!!,
                savePointAmount = dao.savePointAmount!!,
                lostCupAmount = dao.lostCupAmount!!,
                unblockAccountAmount = dao.unblockAccountAmount!!,
                cancelAmount = dao.cancelAmount!!
            )
        }
    }
}