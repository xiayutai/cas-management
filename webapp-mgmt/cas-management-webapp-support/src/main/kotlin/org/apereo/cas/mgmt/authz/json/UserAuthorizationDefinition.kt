package org.apereo.cas.mgmt.authz.json

import com.fasterxml.jackson.annotation.JsonTypeInfo

import java.io.Serializable
import java.util.LinkedHashSet

/**
 * This is [UserAuthorizationDefinition].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
class UserAuthorizationDefinition : Serializable {

    var roles: Set<String> = LinkedHashSet()
    var permissions: Set<String> = LinkedHashSet()

    companion object {
        private const val serialVersionUID = 5612860879960019695L
    }

}
