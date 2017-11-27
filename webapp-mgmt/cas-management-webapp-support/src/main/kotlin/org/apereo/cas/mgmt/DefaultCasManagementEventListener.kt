package org.apereo.cas.mgmt

import org.apache.commons.lang3.StringUtils
import org.apereo.cas.util.AsciiArtUtils
import org.apereo.cas.util.DateTimeUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

/**
 * This is [DefaultCasManagementEventListener].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
class DefaultCasManagementEventListener {

    /**
     * Handle application ready event.
     *
     * @param event the event
     */
    @EventListener
    fun handleApplicationReadyEvent(event: ApplicationReadyEvent) {
        AsciiArtUtils.printAsciiArtInfo(LOGGER, "READY", StringUtils.EMPTY)
        LOGGER.info("Ready to process requests @ [{}]", DateTimeUtils.zonedDateTimeOf(event.timestamp))
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultCasManagementEventListener::class.java)
    }
}
