package org.apereo.cas.mgmt.services.web

import org.apereo.cas.authentication.principal.Service
import org.apereo.cas.authentication.principal.ServiceFactory
import org.apereo.cas.authentication.principal.WebApplicationService
import org.apereo.cas.configuration.CasConfigurationProperties
import org.apereo.cas.mgmt.authentication.CasUserProfile
import org.apereo.cas.mgmt.authentication.CasUserProfileFactory
import org.apereo.cas.mgmt.services.web.beans.FormData
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceItem
import org.apereo.cas.services.RegexRegisteredService
import org.apereo.cas.services.RegisteredService
import org.apereo.cas.services.ServicesManager
import org.apereo.cas.util.DigestUtils
import org.apereo.cas.util.RegexUtils
import org.apereo.services.persondir.IPersonAttributeDao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * MultiActionController to handle the deletion of RegisteredServices as well as
 * displaying them on the Manage Services page.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller("manageRegisteredServicesMultiActionController")
class ManageRegisteredServicesMultiActionController
/**
 * Instantiates a new manage registered services multi action controller.
 *
 * @param servicesManager              the services manager
 * @param personAttributeDao           the person attribute dao
 * @param webApplicationServiceFactory the web application service factory
 * @param defaultServiceUrl            the default service url
 * @param casProperties                the cas properties
 * @param casUserProfileFactory        the cas user profile factory
 */
(
        servicesManager: ServicesManager,
        private val personAttributeDao: IPersonAttributeDao,
        webApplicationServiceFactory: ServiceFactory<WebApplicationService>,
        defaultServiceUrl: String,
        private val casProperties: CasConfigurationProperties,
        private val casUserProfileFactory: CasUserProfileFactory) : AbstractManagementController(servicesManager) {
    private val defaultService: Service

    val managerType: ResponseEntity<String>
        @GetMapping(value = "managerType")
        get() = ResponseEntity(casProperties.serviceRegistry.managementType.toString(), HttpStatus.OK)

    /**
     * Gets domains.
     *
     * @return the domains
     */
    val domains: ResponseEntity<Collection<String>>
        @GetMapping(value = "/domainList")
        get() {
            ensureDefaultServiceExists()
            val data = this.servicesManager.domains
            return ResponseEntity(data, HttpStatus.OK)
        }

    /**
     * Gets form data.
     *
     * @return the form data
     */
    val formData: ResponseEntity<FormData>
        @GetMapping(value = "formData")
        get() {
            ensureDefaultServiceExists()
            val formData = FormData()
            val possibleUserAttributeNames = this.personAttributeDao.possibleUserAttributeNames
            val possibleAttributeNames = ArrayList<String>()
            if (possibleUserAttributeNames != null) {
                possibleAttributeNames.addAll(possibleUserAttributeNames)
                Collections.sort(possibleAttributeNames)
            }
            formData.availableAttributes = possibleAttributeNames
            return ResponseEntity(formData, HttpStatus.OK)
        }

    init {
        this.defaultService = webApplicationServiceFactory.createService(defaultServiceUrl)
    }

    /**
     * Mapped method to return the manage.html.
     *
     * @param response - HttpServletResponse
     * @return - ModelAndView
     */
    @GetMapping("/manage.html")
    fun manage(response: HttpServletResponse): ModelAndView {
        ensureDefaultServiceExists()
        val model = HashMap<String, Any>()
        model.put(STATUS, HttpServletResponse.SC_OK)
        model.put("defaultServiceUrl", this.defaultService.id)
        return ModelAndView("manage", model)
    }

    /**
     * Ensure default service exists.
     */
    private fun ensureDefaultServiceExists() {
        this.servicesManager.load()
        val c = this.servicesManager.allServices ?: throw IllegalStateException("Services cannot be empty")

        if (!this.servicesManager.matchesExistingService(this.defaultService)) {
            val svc = RegexRegisteredService()
            svc.serviceId = '^' + this.defaultService.id
            svc.name = "Services Management Web Application"
            svc.description = svc.name
            this.servicesManager.save(svc)
            this.servicesManager.load()
        }
    }

    /**
     * Authorization failure handling. Simply returns the view name.
     *
     * @return the view name.
     */
    @GetMapping(value = "/authorizationFailure")
    fun authorizationFailureView(): String {
        return "authorizationFailure"
    }

    /**
     * Logout handling. Simply returns the view name.
     *
     * @param request the request
     * @param session the session
     * @return the view name.
     */
    @GetMapping(value = "/logout.html")
    fun logoutView(request: HttpServletRequest, session: HttpSession): String {
        LOGGER.debug("Invalidating application session...")
        session.invalidate()
        return "logout"
    }

    /**
     * Method to delete the RegisteredService by its ID. Will make sure
     * the default service that is the management app itself cannot be deleted
     * or the user will be locked out.
     *
     * @param idAsLong the id
     * @return the response entity
     */
    @GetMapping(value = "/deleteRegisteredService")
    fun deleteRegisteredService(@RequestParam("id") idAsLong: Long): ResponseEntity<String> {
        ensureDefaultServiceExists()
        val svc = this.servicesManager.findServiceBy(this.defaultService) ?: return ResponseEntity("The default service " + this.defaultService.id + " cannot be found. ", HttpStatus.BAD_REQUEST)
        if (svc.id == idAsLong) {
            return ResponseEntity("The default service " + this.defaultService.id + " cannot be deleted. "
                    + "The definition is required for accessing the application.", HttpStatus.BAD_REQUEST)
        }

        val r = this.servicesManager.delete(idAsLong) ?: return ResponseEntity("Service id $idAsLong cannot be found.", HttpStatus.BAD_REQUEST)
        return ResponseEntity(r.name, HttpStatus.OK)
    }


    /**
     * Gets user.
     *
     * @param request  the request
     * @param response the response
     * @return the user
     */
    @GetMapping(value = "/user")
    fun getUser(request: HttpServletRequest,
                response: HttpServletResponse): ResponseEntity<CasUserProfile> {
        val data = casUserProfileFactory.from(request, response)
        return ResponseEntity(data, HttpStatus.OK)
    }

    /**
     * Gets services.
     *
     * @param domain the domain for which services will be retrieved
     * @return the services
     */
    @GetMapping(value = "/getServices")
    fun getServices(@RequestParam domain: String): ResponseEntity<List<RegisteredServiceItem>> {
        ensureDefaultServiceExists()
        val serviceItems = ArrayList<RegisteredServiceItem>()
        val services = ArrayList(this.servicesManager.getServicesForDomain(domain))
        serviceItems.addAll(services.stream().map<RegisteredServiceItem>({ this.createServiceItem(it) }).collect(Collectors.toList()))
        return ResponseEntity(serviceItems, HttpStatus.OK)
    }

    /**
     * Method will filter all services in the register using the passed string a regular expression against the
     * service name, service id, and service description.
     *
     * @param query - a string representing text to search for
     * @return - the resulting services
     */
    @GetMapping(value = "/search")
    fun search(@RequestParam query: String): ResponseEntity<List<RegisteredServiceItem>> {
        ensureDefaultServiceExists()
        val pattern = RegexUtils.createPattern("^.*$query.*$")
        val serviceBeans = ArrayList<RegisteredServiceItem>()
        val services = this.servicesManager.allServices
                .stream()
                .filter { service ->
                    (pattern.matcher(service.serviceId).lookingAt()
                            || pattern.matcher(service.name).lookingAt()
                            || pattern.matcher(service.description).lookingAt())
                }
                .collect(Collectors.toList())
        serviceBeans.addAll(services.stream().map<RegisteredServiceItem>({ this.createServiceItem(it) }).collect(Collectors.toList()))
        return ResponseEntity(serviceBeans, HttpStatus.OK)
    }

    /**
     * Method will update the order of two services passed in.
     *
     * @param request  the request
     * @param response the response
     * @param svcs     the services to be updated
     */
    @PostMapping(value = "/updateOrder", consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    @ResponseStatus(HttpStatus.OK)
    fun updateOrder(request: HttpServletRequest, response: HttpServletResponse,
                    @RequestBody svcs: Array<RegisteredServiceItem>) {
        ensureDefaultServiceExists()
        val id = svcs[0].assignedId
        val svcA = this.servicesManager.findServiceBy(java.lang.Long.parseLong(id!!)) ?: throw IllegalArgumentException("Service $id cannot be found")
        val id2 = svcs[1].assignedId
        val svcB = this.servicesManager.findServiceBy(java.lang.Long.parseLong(id2!!)) ?: throw IllegalArgumentException("Service $id2 cannot be found")
        svcA.evaluationOrder = svcs[0].evalOrder
        svcB.evaluationOrder = svcs[1].evalOrder
        this.servicesManager.save(svcA)
        this.servicesManager.save(svcB)
    }

    private fun createServiceItem(service: RegisteredService): RegisteredServiceItem {
        val serviceItem = RegisteredServiceItem()
        serviceItem.assignedId = service.id.toString()
        serviceItem.evalOrder = service.evaluationOrder
        serviceItem.name = service.name
        serviceItem.serviceId = service.serviceId
        serviceItem.description = DigestUtils.abbreviate(service.description)
        return serviceItem
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ManageRegisteredServicesMultiActionController::class.java)

        private val STATUS = "status"
    }

}

