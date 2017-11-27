package org.apereo.cas.mgmt.services.web

import org.apereo.cas.services.RegexRegisteredService
import org.apereo.cas.services.RegisteredService
import org.apereo.cas.services.ServicesManager
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

/**
 * Handle adding/editing of RegisteredServices.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Controller("registeredServiceSimpleFormController")
class RegisteredServiceSimpleFormController
/**
 * Instantiates a new registered service simple form controller.
 *
 * @param servicesManager          the services manager
 */
(servicesManager: ServicesManager) : AbstractManagementController(servicesManager) {

    /**
     * Adds the service to the Service Registry.
     *
     * @param service the edit bean
     * @return the response entity
     */
    @PostMapping(value = "saveService", consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun saveService(@RequestBody service: RegisteredService): ResponseEntity<String> {
        val newSvc = this.servicesManager.save(service)
        LOGGER.info("Saved changes to service [{}]", service.id)
        return ResponseEntity(newSvc.id.toString(), HttpStatus.OK)
    }

    /**
     * Gets service by id.
     *
     * @param id the id
     * @return the service by id
     */
    @GetMapping(value = "getService")
    fun getServiceById(@RequestParam(value = "id", required = false) id: Long?): ResponseEntity<RegisteredService> {
        val service: RegisteredService?
        if (id == -1L) {
            service = RegexRegisteredService()
        } else {
            service = this.servicesManager.findServiceBy(id!!)
            if (service == null) {
                LOGGER.warn("Invalid service id specified [{}]. Cannot find service in the registry", id)
                throw IllegalArgumentException("Service id $id cannot be found")
            }
        }
        return ResponseEntity(service, HttpStatus.OK)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RegisteredServiceSimpleFormController::class.java)
    }
}
