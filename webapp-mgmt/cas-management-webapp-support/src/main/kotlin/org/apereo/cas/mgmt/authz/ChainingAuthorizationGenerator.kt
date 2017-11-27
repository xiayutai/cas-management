package org.apereo.cas.mgmt.authz

import org.apereo.cas.configuration.CasConfigurationProperties
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile

import java.util.ArrayList

/**
 * This is [ChainingAuthorizationGenerator].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
class ChainingAuthorizationGenerator
/**
 * Instantiates a new Chaining authorization generator.
 *
 * @param casProperties the cas properties
 */
(private val casProperties: CasConfigurationProperties) : AuthorizationGenerator<CommonProfile> {
    private val genenerators = ArrayList<AuthorizationGenerator<CommonProfile>>()

    override fun generate(webContext: WebContext, commonProfile: CommonProfile): CommonProfile {
        var profile = commonProfile
        val it = this.genenerators.iterator()

        while (it.hasNext()) {
            val authz = it.next()
            profile = authz.generate(webContext, profile)
        }
        return profile
    }

    /**
     * Add authorization generator.
     *
     * @param g the generator.
     */
    fun addAuthorizationGenerator(g: AuthorizationGenerator<CommonProfile>) {
        this.genenerators.add(g)
    }
}
