package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.repository.payment.PaymentStatisticDao

data class PaymentStatisticDto(
    val date: String,
    val totalAmount: Long,
    val savePointAmount: Long,
    val lostCupAmount: Long,
    val unblockAccountAmount: Long
) {
    companion object {
        fun fromDao(dao: PaymentStatisticDao): PaymentStatisticDto {
            return PaymentStatisticDto(
                date = dao.date!!,
                totalAmount = dao.totalAmount!!,
                savePointAmount = dao.savePointAmount!!,
                lostCupAmount = dao.lostCupAmount!!,
                unblockAccountAmount = dao.unblockAccountAmount!!
            )
        }
    }
}