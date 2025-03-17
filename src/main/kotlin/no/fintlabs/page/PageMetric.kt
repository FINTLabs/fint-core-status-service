package no.fintlabs.page

import io.micrometer.core.instrument.MeterRegistry

class PageMetric(
    private val meterRegistry: MeterRegistry
) {

    fun incrementCompletedSyncs(domain: String, pkg: String, resource: String) {
        meterRegistry.counter(
            "core.completed.syncs",
            "component", "$domain $pkg",
            "resource", resource
        ).increment()
    }

}