package org.apereo.cas.mgmt.services.audit

import org.apache.commons.lang3.StringUtils
import org.apereo.inspektr.audit.spi.AuditResourceResolver

import org.aspectj.lang.JoinPoint
import org.apereo.cas.util.AopUtils


/**
 * Resolves a service id to the service.
 *
 *
 * The expectation is that args[0] is a Long.
 *
 * @author Scott Battaglia
 * @since 3.4.6
 */
class ServiceManagementResourceResolver : AuditResourceResolver {

    override fun resolveFrom(target: JoinPoint, returnValue: Any): Array<String> {
        return findService(target)
    }

    override fun resolveFrom(target: JoinPoint, exception: Exception): Array<String> {
        return findService(target)
    }

    /**
     * Find service.
     *
     * @param joinPoint the join point
     * @return the string[]
     */
    private fun findService(joinPoint: JoinPoint): Array<String> {
        val j = AopUtils.unWrapJoinPoint(joinPoint)

        val id = j.args[0] as Long ?: return arrayOf(StringUtils.EMPTY)

        return arrayOf("id=" + id)
    }
}
