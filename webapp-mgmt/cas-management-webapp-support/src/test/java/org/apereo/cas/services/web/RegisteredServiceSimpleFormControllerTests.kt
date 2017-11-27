package org.apereo.cas.services.web

import org.apereo.cas.mgmt.services.web.RegisteredServiceSimpleFormController
import org.apereo.cas.services.AbstractRegisteredService
import org.apereo.cas.services.DomainServicesManager
import org.apereo.cas.services.InMemoryServiceRegistry
import org.apereo.cas.services.RegexRegisteredService
import org.apereo.cas.services.RegisteredService
import org.apereo.services.persondir.support.StubPersonAttributeDao
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.springframework.context.ApplicationEventPublisher
import org.springframework.validation.BindingResult

import java.util.Arrays
import java.util.HashMap

import org.junit.Assert.*
import org.mockito.Mockito.*

/**
 * Test cases for [RegisteredServiceSimpleFormController].
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
@RunWith(JUnit4::class)
class RegisteredServiceSimpleFormControllerTests {
    private var controller: RegisteredServiceSimpleFormController? = null
    private var manager: DomainServicesManager? = null
    private var repository: StubPersonAttributeDao? = null

    @Before
    fun setUp() {
        val attributes = HashMap<String, List<Any>>()
        attributes.put(TEST_ID, Arrays.asList(*arrayOf<Any>(TEST_ID)))

        this.repository = StubPersonAttributeDao()
        this.repository!!.backingMap = attributes

        this.manager = DomainServicesManager(InMemoryServiceRegistry(), mock(ApplicationEventPublisher::class.java))
        this.controller = RegisteredServiceSimpleFormController(this.manager!!)
    }

    @Test
    fun verifyAddRegisteredServiceNoValues() {
        val result = mock(BindingResult::class.java)
        `when`(result.model).thenReturn(HashMap())
        `when`(result.hasErrors()).thenReturn(true)
        assertTrue(result.hasErrors())
    }

    @Test
    fun verifyAddRegisteredServiceWithValues() {
        val svc = RegexRegisteredService()
        svc.description = DESCRIPTION
        svc.serviceId = SERVICE_ID
        svc.name = NAME
        svc.evaluationOrder = 123

        assertTrue(this.manager!!.allServices.isEmpty())
        this.controller!!.saveService(svc)

        val services = this.manager!!.allServices
        assertEquals(1, services.size.toLong())
        this.manager!!.allServices.forEach { rs -> assertTrue(rs is RegexRegisteredService) }
    }

    @Test
    fun verifyEditRegisteredServiceWithValues() {
        val r = RegexRegisteredService()
        r.id = 1000
        r.name = "Test Service"
        r.serviceId = TEST_ID
        r.description = DESCRIPTION

        this.manager!!.save(r)

        val svc = RegexRegisteredService()
        svc.description = DESCRIPTION
        svc.serviceId = "serviceId1"
        svc.name = NAME
        svc.id = 1000
        svc.evaluationOrder = 1000

        this.controller!!.saveService(svc)

        assertFalse(this.manager!!.allServices.isEmpty())
        val r2 = this.manager!!.findServiceBy(1000)

        assertEquals("serviceId1", r2.serviceId)
    }

    @Test
    fun verifyAddRegexRegisteredService() {
        val svc = RegexRegisteredService()
        svc.description = DESCRIPTION
        svc.serviceId = "^serviceId"
        svc.name = NAME
        svc.id = 1000
        svc.evaluationOrder = 1000

        this.controller!!.saveService(svc)

        val services = this.manager!!.allServices
        assertEquals(1, services.size.toLong())
        this.manager!!.allServices.forEach { rs -> assertTrue(rs is RegexRegisteredService) }
    }

    @Test
    fun verifyAddMultipleRegisteredServiceTypes() {
        var svc: AbstractRegisteredService = RegexRegisteredService()
        svc.description = DESCRIPTION
        svc.serviceId = "^serviceId"
        svc.name = NAME
        svc.id = 1000
        svc.evaluationOrder = 1000

        this.controller!!.saveService(svc)

        svc = RegexRegisteredService()
        svc.setDescription(DESCRIPTION)
        svc.setServiceId("^serviceId")
        svc.setName(NAME)
        svc.setId(100)
        svc.setEvaluationOrder(100)

        this.controller!!.saveService(svc)

        val services = this.manager!!.allServices
        assertEquals(2, services.size.toLong())
    }

    @Test
    fun verifyAddMockRegisteredService() {
        this.controller = RegisteredServiceSimpleFormController(this.manager!!)

        val svc = RegexRegisteredService()
        svc.description = DESCRIPTION
        svc.serviceId = "^serviceId"
        svc.name = NAME
        svc.id = 1000
        svc.evaluationOrder = 1000

        this.controller!!.saveService(svc)

        val services = this.manager!!.allServices
        assertEquals(1, services.size.toLong())
        this.manager!!.allServices.forEach { rs -> assertTrue(rs is RegexRegisteredService) }
    }

    @Test
    fun verifyEditMockRegisteredService() {
        this.controller = RegisteredServiceSimpleFormController(this.manager!!)

        val r = RegexRegisteredService()
        r.id = 1000
        r.name = "Test Service"
        r.serviceId = TEST_ID
        r.description = DESCRIPTION

        this.manager!!.save(r)

        r.serviceId = "serviceId1"
        this.controller!!.saveService(r)

        assertFalse(this.manager!!.allServices.isEmpty())
        val r2 = this.manager!!.findServiceBy(1000)

        assertEquals("serviceId1", r2.serviceId)
        assertTrue(r2 is RegexRegisteredService)
    }

    companion object {

        private val NAME = "name"
        private val SERVICE_ID = "serviceId"
        private val DESCRIPTION = "description"
        private val TEST_ID = "test"
    }
}
