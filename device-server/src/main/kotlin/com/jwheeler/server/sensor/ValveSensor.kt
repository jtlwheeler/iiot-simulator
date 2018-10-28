package com.jwheeler.server.sensor

import java.util.concurrent.ThreadLocalRandom

class ValveSensor {

    var state: ValveState = ValveState.CLOSED
        private set
        get() {
            return generateFakeValveState()
        }

    private fun generateFakeValveState(): ValveState {
        val randomNumber = ThreadLocalRandom.current().nextInt(0, 2)
        return when (randomNumber) {
            0 -> ValveState.CLOSED
            else -> ValveState.OPENED
        }
    }
}