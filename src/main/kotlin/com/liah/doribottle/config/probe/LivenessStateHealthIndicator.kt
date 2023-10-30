package com.liah.doribottle.config.probe

import org.springframework.boot.actuate.availability.AvailabilityStateHealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.boot.availability.ApplicationAvailability
import org.springframework.boot.availability.AvailabilityState
import org.springframework.boot.availability.LivenessState
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class LivenessStateHealthIndicator(
    availability: ApplicationAvailability
) : AvailabilityStateHealthIndicator(
    availability,
    LivenessState::class.java,
    Consumer { statusMappings: StatusMappings<LivenessState> ->
        statusMappings.add(LivenessState.CORRECT, Status.UP)
        statusMappings.add(LivenessState.BROKEN, Status.DOWN)
    }
) {
    override fun getState(applicationAvailability: ApplicationAvailability): AvailabilityState {
        return applicationAvailability.livenessState
    }
}