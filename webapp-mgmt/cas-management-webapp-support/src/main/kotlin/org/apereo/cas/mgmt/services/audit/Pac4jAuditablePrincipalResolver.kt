package org.apereo.cas.mgmt.services.audit

import org.apereo.cas.util.Pac4jUtils
import org.aspectj.lang.JoinPoint
import org.apereo.inspektr.common.spi.PrincipalResolver

/**
 * Principal resolver for inspektr based on pac4j.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
class Pac4jAuditablePrincipalResolver : PrincipalResolver {

    private val fromSecurityContext: String
        get() = Pac4jUtils.getPac4jAuthenticatedUsername()

    override fun resolveFrom(auditableTarget: JoinPoint, retval: Any): String {
        return fromSecurityContext
    }

    override fun resolveFrom(auditableTarget: JoinPoint, exception: Exception): String {
        return fromSecurityContext
    }

    override fun resolve(): String {
        return fromSecurityContext
    }

}
