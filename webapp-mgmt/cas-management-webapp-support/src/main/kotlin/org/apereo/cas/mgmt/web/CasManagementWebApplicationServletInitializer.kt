package org.apereo.cas.mgmt.web

import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.support.SpringBootServletInitializer


/**
 * This is [CasManagementWebApplicationServletInitializer].
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
class CasManagementWebApplicationServletInitializer : SpringBootServletInitializer() {

    override fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder {
        return builder.sources(CasManagementWebApplication::class.java).banner(CasManagementBanner())
    }
}
