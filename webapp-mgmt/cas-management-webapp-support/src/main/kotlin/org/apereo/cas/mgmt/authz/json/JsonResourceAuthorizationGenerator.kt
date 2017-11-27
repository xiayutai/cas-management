package org.apereo.cas.mgmt.authz.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apereo.cas.util.io.FileWatcherService
import org.hjson.JsonValue
import org.jooq.lambda.Unchecked
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import java.io.File

import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.util.LinkedHashMap

/**
 * This is [JsonResourceAuthorizationGenerator].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
open class JsonResourceAuthorizationGenerator(resource: Resource) : AuthorizationGenerator<CommonProfile> {

    private val objectMapper: ObjectMapper

    private var rules: Map<String, UserAuthorizationDefinition> = LinkedHashMap()

    protected open val jsonFactory: JsonFactory?
        get() = null

    init {
        this.objectMapper = ObjectMapper(jsonFactory).findAndRegisterModules()

        loadResource(resource)
        watchResource(resource)
    }

    private fun watchResource(usersFile: Resource) {
        try {
            val watcher = FileWatcherService(usersFile.file,
                    Unchecked.consumer<File> { file -> loadResource(usersFile) })
            watcher.start(javaClass.simpleName)
        } catch (e: Exception) {
            LOGGER.debug(e.message, e)
        }

    }

    private fun loadResource(res: Resource) {
        try {
            InputStreamReader(res.inputStream, StandardCharsets.UTF_8).use { reader ->
                val personList = object : TypeReference<Map<String, UserAuthorizationDefinition>>() {

                }
                this.rules = this.objectMapper.readValue(JsonValue.readHjson(reader).toString(), personList)
            }
        } catch (e: Exception) {
            throw RuntimeException(e.message, e)
        }

    }

    override fun generate(context: WebContext, profile: CommonProfile): CommonProfile {
        val id = profile.id
        if (rules.containsKey(id)) {
            val defn = rules[id]
            profile.addRoles(defn?.roles)
            profile.addPermissions(defn?.permissions)
        }
        return profile
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(JsonResourceAuthorizationGenerator::class.java)
    }
}
