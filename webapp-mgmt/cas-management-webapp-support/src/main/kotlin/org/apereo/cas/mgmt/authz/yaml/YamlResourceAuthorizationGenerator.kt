package org.apereo.cas.mgmt.authz.yaml

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apereo.cas.mgmt.authz.json.JsonResourceAuthorizationGenerator
import org.springframework.core.io.Resource

/**
 * This is [YamlResourceAuthorizationGenerator].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
class YamlResourceAuthorizationGenerator(resource: Resource) : JsonResourceAuthorizationGenerator(resource) {

    override val jsonFactory: JsonFactory?
        get() = YAMLFactory()
}
