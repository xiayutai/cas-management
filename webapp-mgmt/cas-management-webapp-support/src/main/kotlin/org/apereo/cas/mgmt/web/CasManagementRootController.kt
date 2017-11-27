package org.apereo.cas.mgmt.web

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.ParameterizableViewController
import org.springframework.web.servlet.view.RedirectView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * This is [CasManagementRootController].
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
class CasManagementRootController : ParameterizableViewController() {

    override fun handleRequestInternal(request: HttpServletRequest,
                                       response: HttpServletResponse): ModelAndView {
        val url = request.contextPath + "/manage.html"
        LOGGER.debug("Initial url is [{}]", url)

        val encodedUrl = response.encodeURL(url)
        LOGGER.debug("Encoded url is [{}]", encodedUrl)

        return ModelAndView(RedirectView(encodedUrl))
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CasManagementRootController::class.java)
    }
}
