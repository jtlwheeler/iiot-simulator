package com.jwheeler.server

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists.newArrayList
import com.jwheeler.server.opc.namespace.ValveNamespace
import com.jwheeler.server.security.KeyStoreLoader
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.api.Namespace
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.*
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator
import org.eclipse.milo.opcua.sdk.server.identity.X509IdentityValidator
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil
import org.eclipse.milo.opcua.stack.core.application.DefaultCertificateManager
import org.eclipse.milo.opcua.stack.core.application.DirectoryCertificateValidator
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil
import org.eclipse.milo.opcua.stack.core.util.CryptoRestrictions
import org.slf4j.LoggerFactory
import java.io.File
import java.security.Security
import java.util.*
import java.util.concurrent.CompletableFuture

class Application {
    private var server: OpcUaServer

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        init {
            CryptoRestrictions.remove()
            Security.addProvider(BouncyCastleProvider())
        }
    }

    init {
        val securityTempDir = getSecurityTempDirectory()

        val loader = KeyStoreLoader().load(securityTempDir)

        val certificateManager = DefaultCertificateManager(
                loader.serverKeyPair,
                loader.serverCertificateChain
        )

        val certificateValidator = configureCertificateValidator(securityTempDir)

        val x509IdentityValidator = X509IdentityValidator { true }

        val bindAddresses = newArrayList<String>()
        bindAddresses.add("0.0.0.0")

        val endpointAddresses = newArrayList<String>()
        endpointAddresses.add(HostnameUtil.getHostname())
        endpointAddresses.addAll(HostnameUtil.getHostnames("0.0.0.0"))

        val identityValidator = UsernameIdentityValidator(
                true
        ) { authChallenge ->
            val username = authChallenge.username
            val password = authChallenge.password

            val userOk = "user" == username && "password1" == password
            val adminOk = "admin" == username && "password2" == password

            userOk || adminOk
        }

        val applicationUri = certificateManager.certificates.stream()
                .findFirst()
                .map { certificate ->
                    CertificateUtil.getSubjectAltNameField(certificate, CertificateUtil.SUBJECT_ALT_NAME_URI)
                            .map<String> { it.toString() }
                            .orElseThrow { RuntimeException("certificate is missing the application URI") }
                }
                .orElse("urn:eclipse:milo:examples:server:" + UUID.randomUUID())

        val serverConfig = OpcUaServerConfig.builder()
                .setApplicationUri(applicationUri)
                .setApplicationName(LocalizedText.english("Eclipse Milo OPC UA Example Server"))
                .setBindPort(12686)
                .setBindAddresses(bindAddresses)
                .setEndpointAddresses(endpointAddresses)
                .setBuildInfo(
                        BuildInfo(
                                "urn:eclipse:milo:example-server",
                                "eclipse",
                                "eclipse milo example server",
                                OpcUaServer.SDK_VERSION,
                                "", DateTime.now()))
                .setCertificateManager(certificateManager)
                .setCertificateValidator(certificateValidator)
                .setIdentityValidator(CompositeValidator(identityValidator, x509IdentityValidator))
                .setProductUri("urn:eclipse:milo:example-server")
                .setServerName("example")
                .setSecurityPolicies(
                        EnumSet.of(
                                SecurityPolicy.None,
                                SecurityPolicy.Basic128Rsa15,
                                SecurityPolicy.Basic256,
                                SecurityPolicy.Basic256Sha256,
                                SecurityPolicy.Aes128_Sha256_RsaOaep,
                                SecurityPolicy.Aes256_Sha256_RsaPss))
                .setUserTokenPolicies(
                        ImmutableList.of<UserTokenPolicy>(
                                USER_TOKEN_POLICY_ANONYMOUS,
                                USER_TOKEN_POLICY_USERNAME,
                                USER_TOKEN_POLICY_X509))
                .build()

        server = OpcUaServer(serverConfig)

        server.namespaceManager.registerAndAdd<Namespace>(
                ValveNamespace.NAMESPACE_URI
        ) { idx -> ValveNamespace(server, idx) }

        logger.info("Config done")
    }

    private fun configureCertificateValidator(securityTempDir: File): DirectoryCertificateValidator {
        val pkiDir = securityTempDir.toPath().resolve("pki").toFile()
        val certificateValidator = DirectoryCertificateValidator(pkiDir)
        logger.info("pki dir: {}", pkiDir.absolutePath)
        return certificateValidator
    }

    private fun getSecurityTempDirectory(): File {
        val securityTempDir = File(System.getProperty("java.io.tmpdir"), "security")
        if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
            throw Exception("unable to create security temp dir: $securityTempDir")
        }
        LoggerFactory.getLogger(javaClass).info("security temp dir: {}", securityTempDir.absolutePath)
        return securityTempDir
    }

    fun startup(): CompletableFuture<OpcUaServer> {
        return server.startup()
    }
}

fun main(args: Array<String>) {
    val application = Application()
    application.startup().get()

    val future = CompletableFuture<Void>()

    Runtime.getRuntime().addShutdownHook(Thread { future.complete(null) })

    future.get()
}