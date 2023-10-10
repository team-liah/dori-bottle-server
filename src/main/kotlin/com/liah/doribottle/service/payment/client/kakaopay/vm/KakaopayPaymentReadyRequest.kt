package com.liah.doribottle.service.payment.client.kakaopay.vm

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class KakaopayPaymentReadyRequest(
    val cid: String, // 가맹점 코드
    @JsonProperty("partner_user_id")
    val partnerUserId: String, // 가맹점 회원 id
    @JsonProperty("approval_url")
    val approvalUrl: String, // 결제 성공 시 redirect url
    @JsonProperty("cancel_url")
    val cancelUrl: String, // 결제 취소 시 redirect url
    @JsonProperty("fail_url")
    val failUrl: String // 결제 실패 시 redirect url
) : KakaopayRestApiRequest() {
    @JsonProperty("partner_order_id")
    val partnerOrderId: String = UUID.randomUUID().toString() // 가맹점 주문번호
    @JsonProperty("item_name")
    val itemName: String = "결제수단 등록" // 상품명
    val quantity: Int = 0 // 상품 수량
    @JsonProperty("total_amount")
    val totalAmount: Int = 0 // 상품 총액
    @JsonProperty("tax_free_amount")
    val taxFreeAmount: Int = 0 // 상품 비과세 금액
}