package com.liah.doribottle.constant

object DoriConstant {
    const val FIVE_PENALTIES_PRICE: Long = 30000
    const val LOST_CUP_PRICE: Long = 5000

    /**
     * K: Invitation Count
     * V: Invite Reward Amounts
     */
    val SAVE_INVITE_REWARD_AMOUNTS_MAP = mapOf(
        5 to 5L,
        10 to 10L,
        25 to 30L,
        50 to 60L,
        100 to 120L
    )
    const val SAVE_REGISTER_INVITER_REWARD_AMOUNTS: Long = 10
    const val SAVE_REGISTER_REWARD_AMOUNTS: Long = 20
}