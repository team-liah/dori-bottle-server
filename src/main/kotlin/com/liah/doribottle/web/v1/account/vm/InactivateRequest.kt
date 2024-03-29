package com.liah.doribottle.web.v1.account.vm

import com.liah.doribottle.service.inquiry.dto.BankAccountDto

data class InactivateRequest(
    val bankAccount: BankAccountDto?,
    val reason: String?
)