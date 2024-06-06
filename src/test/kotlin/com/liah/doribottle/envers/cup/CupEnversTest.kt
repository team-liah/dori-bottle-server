package com.liah.doribottle.envers.cup

import com.liah.doribottle.config.TestcontainersConfig
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus
import com.liah.doribottle.repository.cup.CupRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.history.RevisionMetadata
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Import(TestcontainersConfig::class)
@SpringBootTest
class CupEnversTest {
    @Autowired
    private lateinit var cupRepository: CupRepository

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
}
