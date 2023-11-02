package com.liah.doribottle.config.probe

import org.springframework.boot.actuate.availability.AvailabilityStateHealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.boot.availability.ApplicationAvailability
import org.springframework.boot.availability.AvailabilityState
import org.springframework.boot.availability.ReadinessState
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class ReadinessStateHealthIndicator(
    availability: ApplicationAvailability
) : AvailabilityStateHealthIndicator(
    availability,
    ReadinessState::class.java,
    Consumer { statusMappings: StatusMappings<ReadinessState> ->
        statusMappings.add(ReadinessState.ACCEPTING_TRAFFIC, Status.UP)
        statusMappings.add(ReadinessState.REFUSING_TRAFFIC, Status.OUT_OF_SERVICE)
    }
) {
    override fun getState(applicationAvailability: ApplicationAvailability): AvailabilityState {
        return applicationAvailability.readinessState
    }
}