package org.apereo.cas.mgmt.authz

import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.exception.HttpAction
import org.pac4j.core.profile.CommonProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is [CasRoleBasedAuthorizer].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
class CasRoleBasedAuthorizer(roles: List<String>) : RequireAnyRoleAuthorizer<CommonProfile>(roles) {

    @Throws(HttpAction::class)
    override fun check(context: WebContext?, profile: CommonProfile, element: String): Boolean {
        LOGGER.debug("Evaluating [{}] against profile [{}]", element, profile)
        val result = super.check(context, profile, element)
        if (!result) {
            LOGGER.warn("Unable to authorize access, since the authenticated profile [{}] does not contain the required role [{}]", profile, element)
        } else {
            LOGGER.debug("Successfully authorized access for profile [{}] having matched required role [{}]", profile, element)
        }
        return result
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CasRoleBasedAuthorizer::class.java)
    }
}
