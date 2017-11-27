package org.apereo.cas.mgmt.authentication

import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.UserProfile

/**
 * This is [CasUserProfile].
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 5.2.0
 */
class CasUserProfile(up: UserProfile, adminRoles: Collection<String>) : CommonProfile() {
    val isAdministrator: Boolean

    val department: String?
        get() = findFirstMatchingAttribute("department|ou")

    val phone: String?
        get() = findFirstMatchingAttribute("phone|phoneNumber|telephoneNumber|primaryPhone|primaryPhoneNumber")

    init {
        build(up.id, up.attributes)
        clientName = up.clientName
        linkedId = up.linkedId
        isRemembered = up.isRemembered
        addRoles(up.roles)
        addPermissions(up.permissions)

        this.isAdministrator = adminRoles.stream().anyMatch { r -> roles.contains(r) }
    }

    private fun findFirstMatchingAttribute(pattern: String): String? {
        return attributes.entries
                .stream()
                .filter { entry -> entry.key.matches(pattern.toRegex()) }
                .map { e -> e.value.toString() }
                .findFirst()
                .orElse(null)
    }

    companion object {
        private val serialVersionUID = -6308325782274816263L
    }
}
