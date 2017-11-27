package org.apereo.cas.mgmt.config

import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.mgmt.CasManagementUtils
import org.apereo.cas.mgmt.authentication.CasUserProfileFactory
import org.pac4j.cas.client.direct.DirectCasClient
import org.pac4j.cas.config.CasConfiguration
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.client.Client
import org.pac4j.core.client.direct.AnonymousClient
import org.pac4j.core.config.Config
import org.pac4j.core.profile.AnonymousProfile
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.IpClient
import org.pac4j.http.credentials.authenticator.IpRegexpAuthenticator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.StringUtils

import java.util.ArrayList

/**
 * This is [CasManagementAuthenticationConfiguration].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casManagementAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties::class)
class CasManagementAuthenticationConfiguration {

    @Autowired
    private val serverProperties: ServerProperties? = null

    @Autowired
    private val casProperties: CasConfigurationProperties? = null

    @Autowired
    @Qualifier("managementWebappAuthorizer")
    private val managementWebappAuthorizer: Authorizer<CommonProfile>? = null

    @Autowired
    @Qualifier("authorizationGenerator")
    private val authorizationGenerator: AuthorizationGenerator<CommonProfile>? = null

    @Autowired
    @Qualifier("staticAdminRolesAuthorizationGenerator")
    private val staticAdminRolesAuthorizationGenerator: AuthorizationGenerator<CommonProfile>? = null

    @ConditionalOnMissingBean(name = arrayOf("authenticationClients"))
    @Bean
    @RefreshScope
    fun authenticationClients(): List<Client<*, *>> {
        val clients = ArrayList<Client<*, *>>()

        if (StringUtils.hasText(casProperties!!.server.name)) {
            LOGGER.debug("Configuring an authentication strategy based on CAS running at [{}]", casProperties?.server?.name)
            val cfg = CasConfiguration(casProperties?.server?.loginUrl)
            val client = DirectCasClient(cfg)
            client.setAuthorizationGenerator(authorizationGenerator)
            client.name = "CasClient"
            clients.add(client)
        } else {
            LOGGER.debug("Skipping CAS authentication strategy configuration; no CAS server name is defined")
        }

        if (StringUtils.hasText(casProperties?.mgmt?.authzIpRegex)) {
            LOGGER.info("Configuring an authentication strategy based on authorized IP addresses matching [{}]", casProperties?.mgmt?.authzIpRegex)
            val ipClient = IpClient(IpRegexpAuthenticator(casProperties?.mgmt?.authzIpRegex))
            ipClient.name = "IpClient"
            ipClient.setAuthorizationGenerator(staticAdminRolesAuthorizationGenerator)
            clients.add(ipClient)
        } else {
            LOGGER.debug("Skipping IP address authentication strategy configuration; no pattern is defined")
        }

        if (clients.isEmpty()) {
            LOGGER.warn("No authentication strategy is defined, CAS will establish an anonymous authentication mode whereby access is immediately granted. " + "This may NOT be relevant for production purposes. Consider configuring alternative authentication strategies for maximum security.")
            val anon = AnonymousClient.INSTANCE
            anon.setAuthorizationGenerator(staticAdminRolesAuthorizationGenerator as AuthorizationGenerator<AnonymousProfile>)
            clients.add(anon)
        }
        return clients
    }

    @ConditionalOnMissingBean(name = arrayOf("casManagementSecurityConfiguration"))
    @Bean
    @RefreshScope
    fun casManagementSecurityConfiguration(): Config {
        val cfg = Config(CasManagementUtils.getDefaultCallbackUrl(casProperties!!, serverProperties!!), authenticationClients())
        cfg.setAuthorizer(this.managementWebappAuthorizer!!)
        return cfg
    }


    /*
    @ConditionalOnMissingBean(name = arrayOf("casUserProfileFactory"))
    @Bean
    @RefreshScope
    fun casUserProfileFactory(): CasUserProfileFactory {
        return CasUserProfileFactory(casProperties!!)
    }
    */


    companion object {
        private val LOGGER = LoggerFactory.getLogger(CasManagementAuthenticationConfiguration::class.java)
    }
}
