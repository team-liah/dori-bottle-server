package com.liah.doribottle.repository.payment

interface PaymentStatisticDao {
    val date: String?
    val savePointAmount: Long?
    val lostCupAmount: Long?
    val unblockAccountAmount: Long?
    val cancelAmount: Long?
}