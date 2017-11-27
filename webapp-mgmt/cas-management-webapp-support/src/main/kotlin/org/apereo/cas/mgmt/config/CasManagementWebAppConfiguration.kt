package org.apereo.cas.mgmt.config

import org.apereo.cas.authentication.principal.ServiceFactory
import org.apereo.cas.authentication.principal.WebApplicationService
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.configuration.model.support.oidc.OidcProperties
import org.apereo.cas.configuration.support.Beans
import org.apereo.cas.mgmt.CasManagementUtils
import org.apereo.cas.mgmt.DefaultCasManagementEventListener
import org.apereo.cas.mgmt.authentication.CasManagementSecurityInterceptor
import org.apereo.cas.mgmt.authentication.CasUserProfileFactory
import org.apereo.cas.mgmt.services.web.ForwardingController
import org.apereo.cas.mgmt.services.web.ManageRegisteredServicesMultiActionController
import org.apereo.cas.mgmt.services.web.RegisteredServiceSimpleFormController
import org.apereo.cas.mgmt.web.CasManagementRootController
import org.apereo.cas.oidc.claims.BaseOidcScopeAttributeReleasePolicy
import org.apereo.cas.oidc.claims.OidcCustomScopeAttributeReleasePolicy
import org.apereo.cas.services.ServicesManager
import org.apereo.cas.util.CollectionUtils
import org.apereo.services.persondir.IPersonAttributeDao
import org.pac4j.core.config.Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.StringUtils
import org.springframework.web.filter.CharacterEncodingFilter
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping
import org.springframework.web.servlet.i18n.CookieLocaleResolver
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.mvc.Controller
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter
import org.springframework.web.servlet.mvc.UrlFilenameViewController
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver

import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.Properties
import java.util.stream.Collectors

/**
 * This is [CasManagementWebAppConfiguration].
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casManagementWebAppConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties::class)
class CasManagementWebAppConfiguration : WebMvcConfigurerAdapter() {

    @Autowired
    private val serverProperties: ServerProperties? = null


    @Autowired
    private val context: ApplicationContext? = null

    @Autowired
    @Qualifier("casManagementSecurityConfiguration")
    private val casManagementSecurityConfiguration: Config? = null

    @Autowired
    lateinit private var casProperties: CasConfigurationProperties

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private val webApplicationServiceFactory: ServiceFactory<WebApplicationService>? = null

    @Bean
    fun casUserProfileFactory(): CasUserProfileFactory {
        return CasUserProfileFactory(casProperties)
    }


    @Bean
    fun characterEncodingFilter(): Filter {
        return CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true)
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = arrayOf("attributeRepository"))
    @Bean
    fun attributeRepository(): IPersonAttributeDao {
        return Beans.newStubAttributeRepository(casProperties!!.authn.attributeRepository)
    }

    @Bean
    fun rootController(): Controller {
        return CasManagementRootController()
    }

    @Bean
    fun handlerMappingC(): SimpleUrlHandlerMapping {
        val mapping = SimpleUrlHandlerMapping()
        mapping.order = 1
        mapping.setAlwaysUseFullPath(true)
        mapping.rootHandler = rootController()

        val properties = Properties()
        properties.put("/*.html", UrlFilenameViewController())
        mapping.setMappings(properties)
        return mapping
    }

    @Bean
    fun casManagementSecurityInterceptor(): HandlerInterceptorAdapter {
        return CasManagementSecurityInterceptor(casManagementSecurityConfiguration!!)
    }

    @ConditionalOnMissingBean(name = arrayOf("localeResolver"))
    @Bean
    fun localeResolver(): LocaleResolver {
        return object : CookieLocaleResolver() {
            override fun determineDefaultLocale(request: HttpServletRequest): Locale {
                val locale = request.locale
                return if (StringUtils.isEmpty(casProperties!!.mgmt.defaultLocale) || locale.language != casProperties?.mgmt!!.defaultLocale) {
                    locale
                } else Locale(casProperties?.mgmt!!.defaultLocale)
            }
        }
    }

    @RefreshScope
    @Bean
    fun localeChangeInterceptor(): HandlerInterceptor {
        val bean = LocaleChangeInterceptor()
        bean.paramName = this.casProperties!!.locale.paramName
        return bean
    }

    override fun addInterceptors(registry: InterceptorRegistry?) {
        registry!!.addInterceptor(localeChangeInterceptor())
        registry.addInterceptor(casManagementSecurityInterceptor())
                .addPathPatterns("/**").excludePathPatterns("/callback*", "/logout*", "/authorizationFailure")
    }

    @Bean
    fun simpleControllerHandlerAdapter(): SimpleControllerHandlerAdapter {
        return SimpleControllerHandlerAdapter()
    }

    @Bean
    fun forwardingController(): ForwardingController {
        return ForwardingController()
    }

    @Bean
    fun manageRegisteredServicesMultiActionController(
            @Qualifier("servicesManager") servicesManager: ServicesManager): ManageRegisteredServicesMultiActionController {
        val defaultCallbackUrl = CasManagementUtils.getDefaultCallbackUrl(casProperties!!, serverProperties!!)
        return ManageRegisteredServicesMultiActionController(servicesManager, attributeRepository(),
                webApplicationServiceFactory!!, defaultCallbackUrl, casProperties!!, casUserProfileFactory())
    }

    @Bean
    fun registeredServiceSimpleFormController(@Qualifier("servicesManager") servicesManager: ServicesManager): RegisteredServiceSimpleFormController {
        return RegisteredServiceSimpleFormController(servicesManager)
    }

    @RefreshScope
    @Bean
    fun userDefinedScopeBasedAttributeReleasePolicies(): Collection<BaseOidcScopeAttributeReleasePolicy> {
        val oidc = casProperties!!.authn.oidc
        return oidc.userDefinedScopes.entries
                .stream()
                .map { k -> OidcCustomScopeAttributeReleasePolicy(k.key, CollectionUtils.wrapList(*k.value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())) }
                .collect(Collectors.toSet())
    }

    @Bean
    fun defaultCasManagementEventListener(): DefaultCasManagementEventListener {
        return DefaultCasManagementEventListener()
    }

    @Bean
    fun staticTemplateResolver(): SpringResourceTemplateResolver {
        val resolver = SpringResourceTemplateResolver()
        resolver.setApplicationContext(this.context)
        resolver.prefix = "classpath:/dist/"
        resolver.suffix = ".html"
        resolver.setTemplateMode("HTML")
        resolver.characterEncoding = Charset.forName("UTF-8").name()
        resolver.isCacheable = false
        resolver.order = 0
        resolver.checkExistence = true
        return resolver
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry?) {
        registry!!.addResourceHandler("/**").addResourceLocations("classpath:/dist/", "classpath:/static/")
    }
}
