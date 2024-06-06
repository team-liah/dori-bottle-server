package com.liah.doribottle.envers.cup

import com.liah.doribottle.config.TestcontainersConfig
import com.liah.doribottle.constant.AuthorityConstant
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.service.cup.CupService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.history.RevisionMetadata
import org.springframework.data.history.RevisionSort
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@ActiveProfiles("test")
@Import(TestcontainersConfig::class)
@SpringBootTest
class CupEnversTest {
    @Autowired
    private lateinit var cupRepository: CupRepository

    @Autowired
    private lateinit var cupService: CupService

    @DisplayName("컵 리비전 조회")
    @Test
    fun findRevisions() {
        // given
        val cup = cupRepository.save(Cup("A1:A1:A1:A1"))

        cup.update("B1:B1:B1:B1", CupStatus.LOST)
        cupRepository.save(cup)

        cupRepository.delete(cup)

        // when
        val revisions = cupRepository.findRevisions(cup.id)

        // then
        assertThat(revisions.content)
            .extracting("metadata.revisionType")
            .containsExactly(
                RevisionMetadata.RevisionType.INSERT,
                RevisionMetadata.RevisionType.UPDATE,
                RevisionMetadata.RevisionType.DELETE,
            )
    }

    @DisplayName("컵 리비전 조회")
    @Test
    fun getAllRevisions() {
        // given
        val cup = cupRepository.save(Cup("A1:A1:A1:A1"))

        cup.update("B1:B1:B1:B1", CupStatus.LOST)
        cupRepository.save(cup)

        cup.update("B1:B1:B1:B1", CupStatus.RETURNED)
        cupRepository.save(cup)

        cup.delete()
        cupRepository.save(cup)

        cupRepository.delete(cup)

        // when
        val cupRevisions = cupService.getAllRevisions(cup.id, PageRequest.of(0, 5, RevisionSort.desc()))

        // then
        assertThat(cupRevisions)
            .extracting("rfid")
            .containsExactly(
                null,
                "Deleted ${cup.id}",
                "B1:B1:B1:B1",
                "B1:B1:B1:B1",
                "A1:A1:A1:A1",
            )
        assertThat(cupRevisions)
            .extracting("status")
            .containsExactly(
                null,
                CupStatus.RETURNED,
                CupStatus.RETURNED,
                CupStatus.LOST,
                CupStatus.AVAILABLE,
            )
        assertThat(cupRevisions)
            .extracting("deleted")
            .containsExactly(
                false,
                true,
                false,
                false,
                false,
            )
        assertThat(cupRevisions)
            .extracting("lastModifiedBy")
            .containsExactly(
                null,
                UUID.fromString(AuthorityConstant.SYSTEM_ID),
                UUID.fromString(AuthorityConstant.SYSTEM_ID),
                UUID.fromString(AuthorityConstant.SYSTEM_ID),
                UUID.fromString(AuthorityConstant.SYSTEM_ID),
            )
    }
}
