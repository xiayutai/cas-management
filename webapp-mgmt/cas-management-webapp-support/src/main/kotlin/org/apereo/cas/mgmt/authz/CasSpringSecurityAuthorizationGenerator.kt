package org.apereo.cas.mgmt.authz

import org.apereo.cas.util.ResourceUtils
import org.apereo.cas.util.io.FileWatcherService
import org.jooq.lambda.Unchecked
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.authorization.generator.SpringSecurityPropertiesAuthorizationGenerator
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import java.io.File

import java.io.FileInputStream
import java.util.Properties

/**
 * This is [CasSpringSecurityAuthorizationGenerator].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
class CasSpringSecurityAuthorizationGenerator(usersFile: Resource) : AuthorizationGenerator<CommonProfile> {

    private var generator: SpringSecurityPropertiesAuthorizationGenerator<CommonProfile>? = null

    init {
        val properties = Properties()
        try {
            if (ResourceUtils.doesResourceExist(usersFile)) {
                properties.load(usersFile.inputStream)
            }
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }

        this.generator = SpringSecurityPropertiesAuthorizationGenerator<CommonProfile>(properties)
        watchResource(usersFile)
    }

    private fun watchResource(usersFile: Resource) {
        try {
            val watcher = FileWatcherService(usersFile.file,
                    Unchecked.consumer<File> { file ->
                        val newProps = Properties()
                        newProps.load(FileInputStream(file))
                        this.generator = SpringSecurityPropertiesAuthorizationGenerator<CommonProfile>(newProps)
                    })
            watcher.start(javaClass.simpleName)
        } catch (e: Exception) {
            LOGGER.debug(e.message, e)
        }

    }

    override fun generate(context: WebContext, profile: CommonProfile): CommonProfile {
        return this.generator!!.generate(context, profile)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CasSpringSecurityAuthorizationGenerator::class.java)
    }
}
