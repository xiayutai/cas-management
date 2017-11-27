package org.apereo.cas.mgmt.config

import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.mgmt.services.audit.Pac4jAuditablePrincipalResolver
import org.apereo.cas.mgmt.services.audit.ServiceManagementResourceResolver
import org.apereo.cas.util.CollectionUtils
import org.apereo.inspektr.audit.AuditTrailManagementAspect
import org.apereo.inspektr.audit.AuditTrailManager
import org.apereo.inspektr.audit.spi.AuditActionResolver
import org.apereo.inspektr.audit.spi.AuditResourceResolver
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver
import org.apereo.inspektr.audit.spi.support.ObjectCreationAuditActionResolver
import org.apereo.inspektr.audit.spi.support.ParametersAsStringResourceResolver
import org.apereo.inspektr.audit.support.Slf4jLoggingAuditTrailManager
import org.apereo.inspektr.common.spi.PrincipalResolver
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.HashMap

/**
 * This is [CasManagementAuditConfiguration].
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casManagementAuditConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties::class)
class CasManagementAuditConfiguration {

    @Bean
    fun saveServiceResourceResolver(): AuditResourceResolver {
        return ParametersAsStringResourceResolver()
    }

    @Bean
    fun deleteServiceResourceResolver(): AuditResourceResolver {
        return ServiceManagementResourceResolver()
    }

    @Bean
    fun saveServiceActionResolver(): AuditActionResolver {
        return DefaultAuditActionResolver(AUDIT_ACTION_SUFFIX_SUCCESS, AUDIT_ACTION_SUFFIX_FAILED)
    }

    @Bean
    fun deleteServiceActionResolver(): AuditActionResolver {
        return ObjectCreationAuditActionResolver(AUDIT_ACTION_SUFFIX_SUCCESS, AUDIT_ACTION_SUFFIX_FAILED)
    }

    @Bean
    fun auditablePrincipalResolver(): PrincipalResolver {
        return Pac4jAuditablePrincipalResolver()
    }

    @Bean
    fun auditTrailManagementAspect(): AuditTrailManagementAspect {
        return AuditTrailManagementAspect("CAS_Management",
                auditablePrincipalResolver(), CollectionUtils.wrap(auditTrailManager()),
                auditActionResolverMap(),
                auditResourceResolverMap())
    }

    @Bean
    @RefreshScope
    fun auditTrailManager(): AuditTrailManager {
        return Slf4jLoggingAuditTrailManager()
    }

    @Bean
    fun auditResourceResolverMap(): Map<String, AuditResourceResolver> {
        val map = HashMap<String, AuditResourceResolver>(2)
        map.put("DELETE_SERVICE_RESOURCE_RESOLVER", deleteServiceResourceResolver())
        map.put("SAVE_SERVICE_RESOURCE_RESOLVER", saveServiceResourceResolver())
        return map
    }

    @Bean
    fun auditActionResolverMap(): Map<String, AuditActionResolver> {
        val map = HashMap<String, AuditActionResolver>(2)
        map.put("DELETE_SERVICE_ACTION_RESOLVER", deleteServiceActionResolver())
        map.put("SAVE_SERVICE_ACTION_RESOLVER", saveServiceActionResolver())
        return map
    }

    @Bean
    fun casClientInfoLoggingFilter(): FilterRegistrationBean {
        val bean = FilterRegistrationBean()
        bean.filter = ClientInfoThreadLocalFilter()
        bean.urlPatterns = CollectionUtils.wrap("/*")
        bean.setName("CAS Client Info Logging Filter")
        bean.isAsyncSupported = true
        return bean
    }

    companion object {
        private val AUDIT_ACTION_SUFFIX_FAILED = "_FAILED"
        private val AUDIT_ACTION_SUFFIX_SUCCESS = "_SUCCESS"
    }
}
