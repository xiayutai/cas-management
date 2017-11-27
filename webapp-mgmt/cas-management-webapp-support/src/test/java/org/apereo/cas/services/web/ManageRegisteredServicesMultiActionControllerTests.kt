package org.apereo.cas.services.web

import org.apereo.cas.config.CasCoreServicesConfiguration
import org.apereo.cas.config.CasCoreUtilConfiguration
import org.apereo.cas.config.CasCoreWebConfiguration
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration
import org.apereo.cas.mgmt.config.CasManagementAuditConfiguration
import org.apereo.cas.mgmt.config.CasManagementAuthenticationConfiguration
import org.apereo.cas.mgmt.config.CasManagementAuthorizationConfiguration
import org.apereo.cas.mgmt.config.CasManagementWebAppConfiguration
import org.apereo.cas.mgmt.services.web.ManageRegisteredServicesMultiActionController
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceItem
import org.apereo.cas.services.RegexRegisteredService
import org.apereo.cas.services.ServicesManager
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.servlet.ModelAndView

import org.junit.Assert.*

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(AopAutoConfiguration::class, RefreshAutoConfiguration::class, CasManagementAuditConfiguration::class, CasManagementWebAppConfiguration::class, ServerPropertiesAutoConfiguration::class, CasCoreUtilConfiguration::class, CasCoreServicesConfiguration::class, CasManagementAuthenticationConfiguration::class, CasWebApplicationServiceFactoryConfiguration::class, CasManagementAuthorizationConfiguration::class, CasCoreWebConfiguration::class))
@DirtiesContext
@TestPropertySource(locations = arrayOf("classpath:/mgmt.properties"))
class ManageRegisteredServicesMultiActionControllerTests {

    @Rule
    var thrown = ExpectedException.none()

    @Autowired
    @Qualifier("manageRegisteredServicesMultiActionController")
    private val controller: ManageRegisteredServicesMultiActionController? = null

    @Autowired
    @Qualifier("servicesManager")
    private val servicesManager: ServicesManager? = null

    @Test
    fun verifyDeleteService() {
        val r = RegexRegisteredService()
        r.id = 1200
        r.name = NAME
        r.serviceId = "serviceId"
        r.evaluationOrder = 1

        this.servicesManager!!.save(r)

        val response = MockHttpServletResponse()
        this.controller!!.manage(response)
        this.controller!!.deleteRegisteredService(1200)

        assertNull(this.servicesManager!!.findServiceBy(1200))
    }

    @Test
    @Throws(Exception::class)
    fun verifyDeleteServiceNoService() {
        val response = MockHttpServletResponse()
        val entity = this.controller!!.deleteRegisteredService(5000)
        assertNull(this.servicesManager!!.findServiceBy(5000))
        assertFalse(response.contentAsString.contains("serviceName"))
        assertFalse(entity.statusCode.is2xxSuccessful)
    }

    @Test
    fun updateEvaluationOrderInvalidServiceId() {
        val r = RegexRegisteredService()
        r.id = 1200
        r.name = NAME
        r.serviceId = "test"
        r.evaluationOrder = 2

        this.thrown.expect(IllegalArgumentException::class.java)

        this.servicesManager!!.save(r)
        val svcs = arrayOf<RegisteredServiceItem>()
        var rsb = RegisteredServiceItem()
        rsb.assignedId = "5000"
        svcs[0] = rsb
        rsb = RegisteredServiceItem()
        rsb.assignedId = "1200"
        svcs[1] = rsb
        this.controller!!.updateOrder(MockHttpServletRequest(), MockHttpServletResponse(), svcs)
    }

    @Test
    fun verifyManage() {
        val r = RegexRegisteredService()
        r.id = 1200
        r.name = NAME
        r.description = UNIQUE_DESCRIPTION
        r.serviceId = "test"
        r.evaluationOrder = 2

        this.servicesManager!!.save(r)

        val response = MockHttpServletResponse()
        val mv = this.controller!!.manage(response)

        assertTrue(mv.model.containsKey("defaultServiceUrl"))
        assertTrue(mv.model.containsKey("status"))
    }

    companion object {

        private val NAME = "name"
        private val UNIQUE_DESCRIPTION = "uniqueDescription"
    }
}
