package org.apereo.cas.mgmt.services.web.beans

import java.io.Serializable

/**
 * Class used to serialize service information to be used when presenting
 * lists of services.
 *
 * @author Travis Schmidt
 * @since 5.2
 */
class RegisteredServiceItem : Serializable {

    var evalOrder = Integer.MIN_VALUE
    var assignedId: String? = null
    var serviceId: String? = null
    var name: String? = null
    var description: String? = null

    companion object {

        private const val serialVersionUID = 4882440567964605644L
    }
}
