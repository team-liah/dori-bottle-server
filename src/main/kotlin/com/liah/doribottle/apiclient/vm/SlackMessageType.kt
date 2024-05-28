package com.liah.doribottle.apiclient.vm

enum class SlackMessageType(
    val domain: SlackMessageDomain,
) {
    MACHINE_LACK_OF_CUP(SlackMessageDomain.MACHINE),
    MACHINE_FULL_OF_CUP(SlackMessageDomain.MACHINE),
    MACHINE_STATE_CHANGE(SlackMessageDomain.MACHINE),
}

enum class SlackMessageDomain {
    MACHINE,
}
