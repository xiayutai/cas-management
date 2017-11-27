package org.apereo.cas.mgmt.services.web

import org.apereo.cas.services.ServicesManager
import org.apereo.cas.util.JsonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Parent controller for all views.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
abstract class AbstractManagementController
/**
 * Instantiates a new manage registered services multi action controller.
 *
 * @param servicesManager the services manager
 */
(
        /**
         * Instance of ServicesManager.
         */
        protected val servicesManager: ServicesManager) {

    /**
     * Resolve exception.
     *
     * @param request  the request
     * @param response the response
     * @param ex       the exception
     * @return the model and view
     */
    @ExceptionHandler
    fun resolveException(request: HttpServletRequest, response: HttpServletResponse, ex: Exception): ModelAndView? {
        LOGGER.error(ex.message, ex)
        val contentType = request.getHeader(AJAX_REQUEST_HEADER_NAME)
        if (contentType != null && contentType == AJAX_REQUEST_HEADER_VALUE) {
            LOGGER.debug("Handling exception [{}] for ajax request indicated by header [{}]",
                    ex.javaClass.name, AJAX_REQUEST_HEADER_NAME)
            JsonUtils.renderException(ex, response)
            return null
        }
        LOGGER.trace("Unable to resolve exception [{}] for request. AJAX request header [{}] not found.",
                ex.javaClass.name, AJAX_REQUEST_HEADER_NAME)
        val mv = ModelAndView("error")
        mv.addObject(ex)
        return mv
    }

    companion object {
        /**
         * Ajax request header name to examine for exceptions.
         */
        private val AJAX_REQUEST_HEADER_NAME = "x-requested-with"

        /**
         * Ajax request header value to examine for exceptions.
         */
        private val AJAX_REQUEST_HEADER_VALUE = "XMLHttpRequest"

        private val LOGGER = LoggerFactory.getLogger(AbstractManagementController::class.java)
    }
}
