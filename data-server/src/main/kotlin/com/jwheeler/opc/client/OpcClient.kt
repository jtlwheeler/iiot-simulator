package com.jwheeler.opc.client

import com.google.common.collect.Lists.newArrayList
import com.jwheeler.data.server.ValveInfo
import org.eclipse.milo.opcua.sdk.client.OpcUaClient
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem
import org.eclipse.milo.opcua.stack.client.UaTcpStackClient
import org.eclipse.milo.opcua.stack.core.AttributeId
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicLong

class OpcClient {

    private val endpointUrl = System.getenv("DEVICE_URL") ?: "opc.tcp://localhost:12686/example"
    private val logger = LoggerFactory.getLogger(javaClass)
    private val clientHandles = AtomicLong(1L)
    private lateinit var opcClient: OpcUaClient

    init {
        createClient()
    }

    @Throws(Exception::class)
    private fun createClient() {
        val securityTempDir = File(System.getProperty("java.io.tmpdir"), "security")
        if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
            throw Exception("unable to create security dir: $securityTempDir")
        }
        LoggerFactory.getLogger(javaClass)
                .info("security temp dir: {}", securityTempDir.absolutePath)

        val endpoints = try {
            logger.info("***********************************************************")
            logger.info("endpoint URL {} |{}}", endpointUrl, System.getenv("DEVICE_URL"))
            logger.info("***********************************************************")
            UaTcpStackClient
                    .getEndpoints(endpointUrl)
                    .get()
        } catch (ex: Throwable) {
            val discoveryUrl = "$endpointUrl/discovery"
            logger.info("Trying explicit discovery URL: {}", discoveryUrl)
            UaTcpStackClient
                    .getEndpoints(discoveryUrl)
                    .get()
        }

        val endpoint = Arrays.stream(endpoints)
                .findFirst().orElseThrow { Exception("no desired endpoints returned") }

        logger.info("Using endpoint: {}", endpoint.endpointUrl)

        val config = OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
                .setApplicationUri("urn:eclipse:milo:examples:client")
                .setEndpoint(endpoint)
                .setRequestTimeout(uint(5000))
                .build()

        opcClient = OpcUaClient(config)
    }

    fun subscribe() {
        logger.info("subscribe {}", endpointUrl)
        opcClient.connect().get()

        val subscription = opcClient.subscriptionManager.createSubscription(1000.0).get()

        val readValueId = ReadValueId(
                NodeId(2, "Valve/State"),
                AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE)

        val clientHandle = uint(clientHandles.getAndIncrement())

        val parameters = MonitoringParameters(
                clientHandle,
                1000.0, // sampling interval
                null, // filter, null means use default
                uint(10), // queue size
                true        // discard oldest
        )

        val request = MonitoredItemCreateRequest(
                readValueId, MonitoringMode.Reporting, parameters)

        val onItemCreated = { item: UaMonitoredItem, _: Int ->
            item.setValueConsumer { item2, value ->
                this.onSubscriptionValue(item2, value)
            }
        }

        val items = subscription.createMonitoredItems(
                TimestampsToReturn.Both,
                newArrayList<MonitoredItemCreateRequest>(request),
                onItemCreated
        ).get()

        for (item in items) {
            if (item.statusCode.isGood) {
                logger.info("item created for nodeId={}", item.readValueId.nodeId)
            } else {
                logger.warn("failed to create item for nodeId={} (status={})",
                        item.readValueId.nodeId, item.statusCode)
            }
        }
    }

    var valveStatus = 0
    var valveInfo = ValveInfo(0)
    private fun onSubscriptionValue(item: UaMonitoredItem, value: DataValue) {
        logger.info("subscription value received: item={}, value={}",
                item.readValueId.nodeId, value.value)

        valveStatus = value.value.value as Int
        valveInfo = ValveInfo(valveStatus)
    }
}