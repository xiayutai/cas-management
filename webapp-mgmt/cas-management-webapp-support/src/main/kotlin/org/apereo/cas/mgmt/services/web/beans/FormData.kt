package org.apereo.cas.mgmt.services.web.beans

import com.google.common.base.Predicate
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties
import org.apereo.cas.grouper.GrouperGroupField
import org.apereo.cas.oidc.OidcConstants
import org.apereo.cas.services.OidcSubjectTypes
import org.apereo.cas.services.RegisteredService
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy
import org.apereo.cas.services.RegisteredServiceProperty
import org.apereo.cas.ws.idp.WSFederationClaims
import org.apereo.services.persondir.util.CaseCanonicalizationMode
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers
import org.opensaml.saml.metadata.resolver.filter.impl.PredicateFilter
import org.opensaml.saml.saml2.core.Attribute
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor
import org.opensaml.saml.saml2.metadata.SPSSODescriptor
import org.reflections.ReflectionUtils
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus

import java.io.Serializable
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

/**
 * Form data passed onto the screen.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
class FormData : Serializable {

    var availableAttributes: List<String> = ArrayList()

    val remoteCodes = Arrays.stream(HttpStatus.values()).map<Int>({ it.value() }).collect(Collectors.toList())

    val samlRoles = arrayOf(SPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME, IDPSSODescriptor.DEFAULT_ELEMENT_LOCAL_NAME)

    val samlDirections = Arrays.stream<PredicateFilter.Direction>(PredicateFilter.Direction.values()).map<String> { s -> s.name.toUpperCase() }.collect(Collectors.toList())

    val samlAttributeNameFormats = arrayOf(Attribute.BASIC, Attribute.UNSPECIFIED, Attribute.URI_REFERENCE)

    val samlCredentialTypes = Arrays.stream<SamlIdPProperties.Response.SignatureCredentialTypes>(SamlIdPProperties.Response.SignatureCredentialTypes.values())
            .map<String> { s -> s.name.toUpperCase() }
            .collect(Collectors.toList())

    val oidcEncryptAlgOptions = locateKeyAlgorithmsSupported()

    val oidcEncodingAlgOptions = locateContentEncryptionAlgorithmsSupported()

    val registeredServiceProperties: Array<RegisteredServiceProperty.RegisteredServiceProperties>
        get() = RegisteredServiceProperty.RegisteredServiceProperties.values()

    val grouperFields: Array<GrouperGroupField>
        get() = GrouperGroupField.values()

    val timeUnits: Array<TimeUnit>
        get() = TimeUnit.values()

    val mergingStrategies: Array<AbstractPrincipalAttributesRepository.MergingStrategy>
        get() = AbstractPrincipalAttributesRepository.MergingStrategy.values()

    val logoutTypes: Array<RegisteredService.LogoutType>
        get() = RegisteredService.LogoutType.values()

    /**
     * Gets service types.
     *
     * @return the service types
     */
    val serviceTypes: List<Option>
        get() {
            val serviceTypes = ArrayList<Option>()
            serviceTypes.add(Option("CAS Client", "cas"))
            serviceTypes.add(Option("OAuth2 Client", "oauth"))
            serviceTypes.add(Option("SAML2 Service Provider", "saml"))
            serviceTypes.add(Option("OpenID Connect Client", "oidc"))
            serviceTypes.add(Option("WS Federation", "wsfed"))
            return serviceTypes
        }

    val wsFederationClaims: Array<WSFederationClaims>
        get() = WSFederationClaims.values()

    /**
     * Gets mfa providers.
     *
     * @return the mfa providers
     */
    val mfaProviders: List<Option>
        get() {
            val providers = ArrayList<Option>()
            providers.add(Option("Duo Security", "mfa-duo"))
            providers.add(Option("Authy Authenticator", "mfa-authy"))
            providers.add(Option("YubiKey", "mfa-yubikey"))
            providers.add(Option("RSA/RADIUS", "mfa-radius"))
            providers.add(Option("WiKID", "mfa-wikid"))
            providers.add(Option("Google Authenitcator", "mfa-gauth"))
            providers.add(Option("Microsoft Azure", "mfa-azure"))
            providers.add(Option("FIDO U2F", "mfa-u2f"))
            providers.add(Option("Swivel Secure", "mfa-swivel"))
            return providers
        }

    /**
     * Get mfa failure modes registered service multifactor policy . failure modes [ ].
     *
     * @return the registered service multifactor policy . failure modes [ ]
     */
    val mfaFailureModes: Array<RegisteredServiceMultifactorPolicy.FailureModes>
        get() = RegisteredServiceMultifactorPolicy.FailureModes.values()

    /**
     * Gets oidc scopes.
     *
     * @return the oidc scopes
     */
    val oidcScopes: List<Option>
        get() {
            val scopes = Arrays.stream<OidcConstants.StandardScopes>(OidcConstants.StandardScopes.values())
                    .map { scope -> Option(scope.getFriendlyName(), scope.getScope()) }
                    .collect(Collectors.toList())
            scopes.add(Option("User Defined", "user_defined"))
            return scopes
        }

    val oidcSubjectTypes: Array<OidcSubjectTypes>
        get() = OidcSubjectTypes.values()

    val canonicalizationModes: Array<CaseCanonicalizationMode>
        get() = CaseCanonicalizationMode.values()

    public inner class Option internal constructor(var display: String?, var value: String?)


    private fun locateKeyAlgorithmsSupported(): List<String> {
        return ReflectionUtils.getFields(KeyManagementAlgorithmIdentifiers::class.java,
                Predicate { field ->
                    (Modifier.isFinal(field!!.getModifiers()) && Modifier.isStatic(field.getModifiers())
                            && field.getType() == String::class.java)
                })
                .stream()
                .map<String>( { it.getName() })
                .sorted()
                .collect(Collectors.toList())
    }

    private fun locateContentEncryptionAlgorithmsSupported(): List<String> {
        return ReflectionUtils.getFields(ContentEncryptionAlgorithmIdentifiers::class.java,
                Predicate { field ->
                    (Modifier.isFinal(field!!.getModifiers()) && Modifier.isStatic(field.getModifiers())
                            && field.getType() == String::class.java)
                })
                .stream()
                .map<String>({ it.getName() })
                .sorted()
                .collect(Collectors.toList())
    }

    companion object {
        private const val serialVersionUID = -5201796557461644152L
    }
}
