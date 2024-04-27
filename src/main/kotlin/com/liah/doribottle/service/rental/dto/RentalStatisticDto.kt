package com.liah.doribottle.service.rental.dto

import com.liah.doribottle.repository.rental.RentalStatisticDao

data class RentalStatisticDto(
    val date: String,
    val totalPointAmount: Long,
    val confirmedCount: Long,
    val succeededCount: Long,
    val failedCount: Long,
    val canceledCount: Long,
) {
    companion object {
        fun fromDao(dao: RentalStatisticDao): RentalStatisticDto {
            return RentalStatisticDto(
                date = dao.date!!,
                totalPointAmount = dao.totalPointAmount!!,
                confirmedCount = dao.confirmedCount!!,
                succeededCount = dao.succeededCount!!,
                failedCount = dao.failedCount!!,
                canceledCount = dao.canceledCount!!,
            )
        }
    }
}
