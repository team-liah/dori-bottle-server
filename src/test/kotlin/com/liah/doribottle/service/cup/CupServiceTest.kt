package com.liah.doribottle.service.cup

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus.*
import com.liah.doribottle.repository.cup.CupRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class CupServiceTest {
    companion object {
        const val RFID = "RFID"
    }

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    @Autowired
    private lateinit var cupRepository: CupRepository
    @Autowired
    private lateinit var cupService: CupService

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @DisplayName("컵 등록")
    @Test
    fun registerTest() {
        //when
        val id = cupService.register(RFID)
        clear()

        //then
        val findCup = cupRepository.findById(id).orElse(null)
        assertThat(findCup.rfid).isEqualTo(RFID)
        assertThat(findCup.status).isEqualTo(AVAILABLE)
        assertThat(findCup.deleted).isFalse
    }

    @DisplayName("RFID 컵 조회")
    @Test
    fun getCupByRfidTest() {
        //given
        cupRepository.save(Cup(RFID))
        clear()

        //when
        val cup = cupService.getByRfid(RFID)

        //then
        assertThat(cup.rfid).isEqualTo(RFID)
        assertThat(cup.status).isEqualTo(AVAILABLE)
    }

    @DisplayName("컵 제거")
    @Test
    fun removeTest() {
        //given
        val cup = cupRepository.save(Cup(RFID))
        val id = cup.id
        clear()

        //when
        cupService.remove(id)
        clear()

        //then
        val findCup = cupRepository.findById(id).orElse(null)
        assertThat(findCup).isNull()
    }

    @DisplayName("컵 제거 예외")
    @Test
    fun removeExceptionTest() {
        //given
        val cup = cupRepository.save(Cup(RFID))
        val id = cup.id
        cup.changeState(AVAILABLE)
        cup.loan()

        clear()

        //when, then
        val exception = assertThrows<BusinessException> {
            cupService.remove(id)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.CUP_DELETE_NOT_ALLOWED)
    }
}