package com.liah.doribottle.service.payment.client.kakaopay.vm

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaopayPaymentReadyResponse(
    val tid: String, // 가맹점 코드
    @JsonProperty("next_redirect_app_url")
    val nextRedirectAppUrl: String, // 요청한 클라이언트(Client)가 모바일 앱일 경우 카카오톡 결제 페이지 Redirect URL
    @JsonProperty("next_redirect_mobile_url")
    val nextRedirectMobileUrl: String, // 요청한 클라이언트가 모바일 웹일 경우 카카오톡 결제 페이지 Redirect URL
    @JsonProperty("next_redirect_pc_url")
    val nextRedirectPcUrl: String, // 요청한 클라이언트가 PC 웹일 경우 카카오톡으로 결제 요청 메시지(TMS)를 보내기 위한 사용자 정보 입력 화면 Redirect URL
    @JsonProperty("android_app_scheme")
    val androidAppScheme: String, // 카카오페이 결제 화면으로 이동하는 Android 앱 스킴(Scheme)
    @JsonProperty("ios_app_scheme")
    val iosAppScheme: String, // 카카오페이 결제 화면으로 이동하는 iOS 앱 스킴
    @JsonProperty("created_at")
    val createdAt: String // 결제 준비 요청 시간
)