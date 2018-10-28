package com.jwheeler.server.opc.namespace

import com.jwheeler.server.sensor.ValveState
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode
import org.eclipse.milo.opcua.stack.core.application.CertificateManager
import org.eclipse.milo.opcua.stack.core.application.CertificateValidator
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.junit.Test
import java.security.KeyPair
import java.security.cert.X509Certificate
import java.util.*

class ValveNamespaceTest {

    @Test
    fun valveStatusNode_shouldBeToNamespace() {
        val serverConfig = OpcUaServerConfig.builder()
                .setCertificateManager(StubCertificateManager())
                .setCertificateValidator(StubCertificateValidator())
                .build()
        val server = OpcUaServer(serverConfig)
        val valveNamespace = server.namespaceManager.registerAndAdd(
                ValveNamespace.NAMESPACE_URI
        ) { idx -> ValveNamespace(server, idx) }

        val nodeId = NodeId(valveNamespace.namespaceIndex, "Valve/State")
        val valveStatusNode = server.nodeMap.getNode(nodeId).get()
        assertThat(valveStatusNode).isNotNull

        assertThat(valveStatusNode.browseName.name).isEqualTo("Valve State")

        val uaVariableNode = valveStatusNode as UaVariableNode
        assertThat(uaVariableNode.value.value.value).isEqualTo(ValveState.CLOSED)
    }

    private inner class StubCertificateManager : CertificateManager {

        override fun getKeyPair(thumbprint: ByteString): Optional<KeyPair> {
            return Optional.empty()
        }

        override fun getCertificate(thumbprint: ByteString): Optional<X509Certificate> {
            return Optional.empty()
        }

        override fun getCertificateChain(thumbprint: ByteString): Optional<Array<X509Certificate>> {
            return Optional.empty()
        }

        override fun getKeyPairs(): Set<KeyPair>? {
            return null
        }

        override fun getCertificates(): Set<X509Certificate> {
            return emptySet()
        }
    }

    private inner class StubCertificateValidator : CertificateValidator {

        override fun validate(certificate: X509Certificate) {

        }

        override fun verifyTrustChain(certificateChain: List<X509Certificate>) {

        }
    }
}