package com.liah.doribottle

import com.liah.doribottle.config.TestcontainersConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Import(TestcontainersConfig::class)
@SpringBootTest
class DoriBottleApplicationTests {
    @Test
    fun contextLoads() {
    }
}
