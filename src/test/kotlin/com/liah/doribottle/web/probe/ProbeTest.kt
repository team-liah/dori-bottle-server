package com.liah.doribottle.web.probe

import com.liah.doribottle.web.BaseControllerTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.availability.ApplicationAvailability
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.LivenessState
import org.springframework.boot.availability.ReadinessState
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ProbeTest : BaseControllerTest() {
    @Autowired
    private lateinit var applicationAvailability: ApplicationAvailability

    @DisplayName("Liveness Test")
    @Test
    fun givenCorrectState_whenPublishingTheEvent_thenShouldTransitToBrokenState() {
        assertThat(applicationAvailability.livenessState).isEqualTo(LivenessState.CORRECT)
        mockMvc.perform(get("/actuator/health/liveness"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
        AvailabilityChangeEvent.publish(context, LivenessState.BROKEN)
        assertThat(applicationAvailability.livenessState).isEqualTo(LivenessState.BROKEN)
        mockMvc.perform(get("/actuator/health/liveness"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.status").value("DOWN"))
    }

    @DisplayName("Readiness Test")
    @Test
    fun givenAcceptingState_whenPublishingTheEvent_thenShouldTransitToRefusingState() {
        assertThat(applicationAvailability.readinessState).isEqualTo(ReadinessState.ACCEPTING_TRAFFIC)
        mockMvc.perform(get("/actuator/health/readiness"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
        AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC)
        assertThat(applicationAvailability.readinessState).isEqualTo(ReadinessState.REFUSING_TRAFFIC)
        mockMvc.perform(get("/actuator/health/readiness"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.status").value("OUT_OF_SERVICE"))
    }
}