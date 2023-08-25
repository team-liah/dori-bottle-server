package com.liah.doribottle.service.payment.dto

import java.time.Instant

data class TossPaymentResponse(
    val mId: String?, // 상점아이디
    val version: String?, // Payment 객체의 응답 버전
    val paymentKey: String, // 결제의 키 값
    val status: TossPaymentStatus?, // 결제 처리 상태
    val lastTransactionKey: String?, // 마지막 거래의 키 값
    val orderId: String?, // 주문 ID
    val orderName: String?, // 주문명
    val requestedAt: Instant?, // 결제가 일어난 날짜와 시간 정보
    val approvedAt: Instant, // 결제 승인이 일어난 날짜와 시간 정보
    val useEscrow: Boolean?, // 에스크로 사용 여부
    val cultureExpense: Boolean?, // 문화비(도서, 공연 티켓, 박물관·미술관 입장권 등) 지출 여부 (계좌이체, 가상계좌 결제)
    val card: TossPaymentCardResponse?,
    val virtualAccount: Any?, // 가상계좌로 결제하면 제공되는 가상계좌 관련 정보
    val transfer: Any?, // 계좌이체로 결제했을 때 이체 정보
    val mobilePhone: Any?, // 휴대폰으로 결제하면 제공되는 휴대폰 결제 관련 정보
    val giftCertificate: Any?, // 상품권으로 결제하면 제공되는 상품권 결제 관련 정보
    val cashReceipt: Any?, // 현금영수증 정보
    val cashReceipts: Any?, // 현금영수증 발행 및 취소 이력
    val discount: Any?, // 카드사의 즉시 할인 프로모션
    val cancels: List<TossCancelResponse>?, // 결제 취소 이력
    val secret: String?, // 가상계좌 웹훅이 정상적인 요청인지 검증하는 값
    val type: TossPaymentType?,
    val easyPay: Any?, // 간편결제 정보
    val country: String?, // 결제한 국가
    val failure: TossFailureResponse?, // 결제 실패 정보
    val isPartialCancelable: Boolean?, // 부분 취소 가능 여부
    val receipt: TossReceiptResponse?, // 발행된 영수증 정보
    val checkout: Any?, // 결제창 정보
    val currency: String?, // 결제 통화
    val totalAmount: Int?, // 총 결제 금액
    val balanceAmount: Int?, // 취소할 수 있는 금액
    val suppliedAmount: Int?, // 공급가액
    val vat: Int?, // 부가세
    val taxFreeAmount: Int?, // 결제 금액 중 면세 금액
    val taxExemptionAmount: Int?, // 과세를 제외한 결제 금액
    val method: String? // 결제수단
) {
    fun toPaymentResultDto() = PaymentResultDto(paymentKey, approvedAt, receipt?.url, cancels?.firstOrNull()?.transactionKey)
}

data class TossPaymentCardResponse(
    val issuerCode: String?,
    val acquirerCode: String?,
    val number: String?,
    val installmentPlanMonths: Int?,
    val isInterestFree: Boolean?,
    val interestPayer: String?,
    val approveNo: String?,
    val useCardPoint: Boolean?,
    val cardType: String?,
    val ownerType: String?,
    val acquireStatus: String?,
    val amount: Int?
)

data class TossReceiptResponse(
    val url: String?
)

data class TossCancelResponse(
    val cancelAmount: Int?, // 결제를 취소한 금액
    val cancelReason: String?, // 결제를 취소한 이유
    val taxFreeAmount: Int?, // 취소된 금액 중 면세 금액
    val taxExemptionAmount: Int?, // 취소된 금액 중 과세 제외 금액
    val refundableAmount: Int?, // 결제 취소 후 환불 가능한 잔액
    val easyPayDiscountAmount: Int?, // 간편결제 서비스의 포인트, 쿠폰, 즉시할인과 같은 적립식 결제수단에서 취소된 금액
    val canceledAt: Instant?, // 결제 취소가 일어난 날짜와 시간 정보
    val transactionKey: String?, // 취소 건의 키 값
    val receiptKey: String? // 취소 건의 현금영수증 키 값
)

data class TossFailureResponse(
    val code: String?,
    val message: String?
)

enum class TossPaymentType {
    NORMAL, BILLING, BRANDPAY
}

enum class TossPaymentStatus {
    READY, // 결제를 생성하면 가지게 되는 초기 상태
    IN_PROGRESS, // 결제수단 정보와 해당 결제수단의 소유자가 맞는지 인증을 마친 상태
    WAITING_FOR_DEPOSIT, // 가상계좌 결제 흐름에만 있는 상태
    DONE, // 인증된 결제수단 정보, 고객 정보로 요청한 결제가 승인된 상태
    CANCELED, // 승인된 결제가 취소된 상태
    PARTIAL_CANCELED, // 승인된 결제가 부분 취소된 상태
    ABORTED, // 결제 승인이 실패한 상태
    EXPIRED // 결제 유효 시간 30분이 지나 거래가 취소된 상태
}