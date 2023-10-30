package com.liah.doribottle.config.probe

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.availability.ApplicationAvailability
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
class ProbeTest {
    protected lateinit var mockMvc: MockMvc

    @Autowired private lateinit var applicationAvailability: ApplicationAvailability
}