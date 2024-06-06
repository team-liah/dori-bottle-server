package com.liah.doribottle.service.cup

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

class CupServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var cupRepository: CupRepository

    @Autowired
    private lateinit var cupService: CupService

    @DisplayName("컵 등록")
    @Test
    fun register() {
        // when
        val id = cupService.register(RFID)
        clear()

        // then
        val findCup = cupRepository.findById(id).orElse(null)
        assertThat(findCup.rfid).isEqualTo(RFID)
        assertThat(findCup.status).isEqualTo(CupStatus.AVAILABLE)
    }

    @DisplayName("컵 등록 예외")
    @Test
    fun registerException() {
        // given
        cupRepository.save(Cup(RFID))
        clear()

        // when, then
        val exception =
            assertThrows<BusinessException> {
                cupService.register(RFID)
            }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.CUP_ALREADY_REGISTERED)
    }

    @DisplayName("컵 조회")
    @Test
    fun get() {
        // given
        val cup = cupRepository.save(Cup(RFID))
        clear()

        // when
        val result = cupService.get(cup.id)

        // then
        assertThat(result.rfid).isEqualTo(RFID)
        assertThat(result.status).isEqualTo(CupStatus.AVAILABLE)
    }

    @DisplayName("RFID 컵 조회")
    @Test
    fun getByRfid() {
        // given
        cupRepository.save(Cup(RFID))
        clear()

        // when
        val cup = cupService.getByRfid(RFID)

        // then
        assertThat(cup.rfid).isEqualTo(RFID)
        assertThat(cup.status).isEqualTo(CupStatus.AVAILABLE)
    }

    @DisplayName("컵 수정")
    @Test
    fun update() {
        // given
        val cup = cupRepository.save(Cup(RFID))
        val id = cup.id
        clear()

        // when
        cupService.update(id, "TEST", CupStatus.LOST)
        clear()

        // then
        val findCup = cupRepository.findByIdOrNull(id)
        assertThat(findCup?.rfid).isEqualTo("TEST")
        assertThat(findCup?.status).isEqualTo(CupStatus.LOST)
    }

    @DisplayName("컵 제거")
    @Test
    fun remove() {
        // given
        val cup = cupRepository.save(Cup(RFID))
        val id = cup.id
        clear()

        // when
        cupService.remove(id)
        clear()

        // then
        val findCup = cupRepository.findByIdOrNull(id)
        assertThat(findCup?.deleted).isTrue()
    }

    @DisplayName("컵 제거 예외")
    @Test
    fun removeException() {
        // given
        val cup = cupRepository.save(Cup(RFID))
        val id = cup.id
        cup.update(RFID, CupStatus.AVAILABLE)
        cup.loan()

        clear()

        // when, then
        val exception =
            assertThrows<BusinessException> {
                cupService.remove(id)
            }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.CUP_DELETE_NOT_ALLOWED)
    }

    @DisplayName("컵 목록 조회")
    @Test
    fun getAll() {
        // given
        cupRepository.save(Cup("B1:B1:B1:B1"))
        cupRepository.save(Cup("C1:C1:C1:C1"))
        cupRepository.save(Cup("D1:D1:D1:D1"))
        cupRepository.save(Cup("E1:E1:E1:E1"))
        cupRepository.save(Cup("F1:F1:F1:F1"))
        cupRepository.save(Cup("G1:G1:G1:G1"))
        clear()

        // when
        val result =
            cupService.getAll(
                status = CupStatus.AVAILABLE,
                pageable = Pageable.ofSize(3),
            )

        // then
        assertThat(result.totalElements).isEqualTo(6)
        assertThat(result.totalPages).isEqualTo(2)
        assertThat(result)
            .extracting("rfid")
            .containsExactly("B1:B1:B1:B1", "C1:C1:C1:C1", "D1:D1:D1:D1")
    }
}
