package org.apereo.cas.mgmt.authentication

import org.apereo.cas.CasProtocolConstants
import org.pac4j.core.client.Client
import org.pac4j.core.config.Config
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.core.exception.HttpAction
import org.pac4j.core.profile.CommonProfile
import org.pac4j.springframework.web.SecurityInterceptor
import org.springframework.util.StringUtils
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.stream.Collectors

/**
 * This is [CasManagementSecurityInterceptor].
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
class CasManagementSecurityInterceptor
/**
 * Instantiates a new Cas management security interceptor.
 *
 * @param config the config
 */
(config: Config) : SecurityInterceptor(config) {

    init {
        super.setClients(getClientNames(config))
        super.setAuthorizers(getAuthorizerNames(config))
        val logic = CasManagementSecurityLogic()
        logic.isSaveProfileInSession = true
        super.setSecurityLogic(logic)
    }

    override fun postHandle(request: HttpServletRequest?, response: HttpServletResponse?,
                            handler: Any?, modelAndView: ModelAndView?) {
        if (!StringUtils.isEmpty(request!!.queryString) && request.queryString.contains(CasProtocolConstants.PARAMETER_TICKET)) {
            val v = RedirectView(request.requestURL.toString())
            v.setExposeModelAttributes(false)
            v.isExposePathVariables = false
            modelAndView!!.view = v
        }
    }

    private fun getClientNames(config: Config): String {
        return config.clients.clients.stream().map<String>( { it.getName() }).collect(Collectors.joining(Pac4jConstants.ELEMENT_SEPRATOR))
    }

    private fun getAuthorizerNames(config: Config): String {
        return config.authorizers.keys.stream().collect(Collectors.joining(Pac4jConstants.ELEMENT_SEPRATOR))
    }

    /**
     * The Cas management security logic.
     */
    class CasManagementSecurityLogic : DefaultSecurityLogic<Boolean,J2EContext>() {
        protected override fun forbidden(context: J2EContext, currentClients: List<Client<Credentials,CommonProfile>>?, list: List<CommonProfile>?, authorizers: String?): HttpAction {
            return HttpAction.redirect("Authorization failed", context, "authorizationFailure")
        }

        protected override fun loadProfilesFromSession(context: J2EContext?, currentClients: List<Client<Credentials,CommonProfile>>): Boolean {
            return true
        }
    }
}
