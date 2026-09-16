package com.washapp.data.scheduling

import com.washapp.data.model.Machine

object MachineScheduler {
    fun findAvailableMachine(machines: List<Machine>): Machine? {
        return machines.firstOrNull { it.isAvailable }
    }
}
