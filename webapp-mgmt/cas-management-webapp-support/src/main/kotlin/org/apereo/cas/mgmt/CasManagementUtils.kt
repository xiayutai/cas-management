package org.apereo.cas.mgmt

import org.apereo.cas.configuration.CasConfigurationProperties
import org.springframework.beans.factory.BeanCreationException
import org.springframework.boot.autoconfigure.web.ServerProperties

/**
 * This is [CasManagementUtils].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
object CasManagementUtils {

    /**
     * Gets default callback url.
     *
     * @param casProperties    the cas properties
     * @param serverProperties the server properties
     * @return the default callback url
     */
    fun getDefaultCallbackUrl(casProperties: CasConfigurationProperties, serverProperties: ServerProperties): String {
        try {
            return casProperties.mgmt.serverName + serverProperties.contextPath + "/manage.html"
        } catch (e: Exception) {
            throw BeanCreationException(e.message, e)
        }

    }
}
