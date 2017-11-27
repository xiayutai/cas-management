package org.apereo.cas.mgmt.config

import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.configuration.model.webapp.mgmt.ManagementWebappProperties
import org.apereo.cas.mgmt.authz.CasRoleBasedAuthorizer
import org.apereo.cas.mgmt.authz.CasSpringSecurityAuthorizationGenerator
import org.apereo.cas.mgmt.authz.json.JsonResourceAuthorizationGenerator
import org.apereo.cas.mgmt.authz.yaml.YamlResourceAuthorizationGenerator
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.authorization.generator.FromAttributesAuthorizationGenerator
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

/**
 * This is [CasManagementAuthorizationConfiguration].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casManagementAuthorizationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties::class)
class CasManagementAuthorizationConfiguration {

    @Autowired
    private val casProperties: CasConfigurationProperties? = null

    @ConditionalOnMissingBean(name = arrayOf("authorizationGenerator"))
    @Bean
    @RefreshScope
    fun authorizationGenerator(): AuthorizationGenerator<CommonProfile> {
        val authzAttributes = casProperties!!.mgmt.authzAttributes
        return if (!authzAttributes.isEmpty()) {
            if (authzAttributes.stream().anyMatch { a -> a == "*" }) {
                staticAdminRolesAuthorizationGenerator()
            } else FromAttributesAuthorizationGenerator(authzAttributes.toTypedArray(), arrayOf())
        } else springSecurityPropertiesAuthorizationGenerator()

    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = arrayOf("staticAdminRolesAuthorizationGenerator"))
    fun staticAdminRolesAuthorizationGenerator(): AuthorizationGenerator<CommonProfile> {
        return Generator(casProperties!!)
    }

    class Generator(var casProperties: CasConfigurationProperties): AuthorizationGenerator<CommonProfile> {

        override fun generate(p0: WebContext?, p1: CommonProfile): CommonProfile {
            p1.addRoles(casProperties.mgmt.adminRoles)
            return p1
        }
    }

    @ConditionalOnMissingBean(name = arrayOf("managementWebappAuthorizer"))
    @Bean
    @RefreshScope
    fun managementWebappAuthorizer(): Authorizer<*> {
        return CasRoleBasedAuthorizer(casProperties!!.mgmt.adminRoles)
    }

    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = arrayOf("springSecurityPropertiesAuthorizationGenerator"))
    fun springSecurityPropertiesAuthorizationGenerator(): AuthorizationGenerator<CommonProfile> {
        try {
            val mgmt = casProperties!!.mgmt
            val userPropertiesFile = mgmt.userPropertiesFile
            if (userPropertiesFile.filename.endsWith("json")) {
                return JsonResourceAuthorizationGenerator(userPropertiesFile)
            }
            return if (userPropertiesFile.filename.endsWith("yml")) {
                YamlResourceAuthorizationGenerator(userPropertiesFile)
            } else CasSpringSecurityAuthorizationGenerator(userPropertiesFile)
        } catch (e: Exception) {
            throw RuntimeException(e.message, e)
        }

    }
}
