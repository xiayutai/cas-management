package org.apereo.cas.mgmt.web

import org.apereo.cas.util.spring.boot.AbstractCasBanner

/**
 * This is [CasManagementBanner].
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
class CasManagementBanner : AbstractCasBanner() {
    override fun getTitle(): String {
        return "CAS Management"
    }
}
