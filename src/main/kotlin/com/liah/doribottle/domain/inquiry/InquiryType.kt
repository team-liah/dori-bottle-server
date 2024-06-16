package com.liah.doribottle.domain.inquiry

enum class InquiryType(
    val title: String,
) {
    REFUND("환불접수"),
    SALES("도입문의"),
    ETC("기타"),
}
