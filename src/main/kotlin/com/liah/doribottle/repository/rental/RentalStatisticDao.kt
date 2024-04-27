package com.liah.doribottle.repository.rental

interface RentalStatisticDao {
    val date: String?
    val totalPointAmount: Long?
    val confirmedCount: Long?
    val succeededCount: Long?
    val failedCount: Long?
    val canceledCount: Long?
}
