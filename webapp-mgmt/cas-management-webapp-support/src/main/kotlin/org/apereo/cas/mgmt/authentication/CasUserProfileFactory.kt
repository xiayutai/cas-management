package org.apereo.cas.mgmt.authentication

import org.apereo.cas.configuration.CasConfigurationProperties
import org.pac4j.core.context.J2EContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.core.profile.UserProfile
import org.springframework.stereotype.Component

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.Optional

/**
 * This is [CasUserProfileFactory].
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 5.2.0
 */
class CasUserProfileFactory(private val casProperties: CasConfigurationProperties) {

    /**
     * create user profile for views.
     *
     * @param request  the request
     * @param response the response
     * @return the cas user profile
     */
    fun from(request: HttpServletRequest, response: HttpServletResponse): CasUserProfile {
        val manager = ProfileManager<CommonProfile>(J2EContext(request, response))
        val profile = manager.get(true)
        if (profile.isPresent()) {
            val up = profile.get()
            return CasUserProfile(up, this.casProperties.mgmt.adminRoles)
        }
        throw IllegalArgumentException("Could not determine authenticated profile")
    }
}
