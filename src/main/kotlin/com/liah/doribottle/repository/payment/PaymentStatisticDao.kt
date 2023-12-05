package com.liah.doribottle.repository.payment

interface PaymentStatisticDao {
    val date: String?
    val totalAmount: Long?
    val savePointAmount: Long?
    val lostCupAmount: Long?
    val unblockAccountAmount: Long?
}