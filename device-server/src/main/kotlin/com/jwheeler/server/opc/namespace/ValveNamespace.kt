package com.jwheeler.server.opc.namespace

import com.google.common.collect.Lists
import com.jwheeler.server.sensor.ValveSensor
import org.eclipse.milo.opcua.sdk.core.AccessLevel
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.api.*
import org.eclipse.milo.opcua.sdk.server.api.nodes.VariableNode
import org.eclipse.milo.opcua.sdk.server.nodes.AttributeContext
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode
import org.eclipse.milo.opcua.sdk.server.nodes.delegates.AttributeDelegate
import org.eclipse.milo.opcua.sdk.server.nodes.delegates.AttributeDelegateChain
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.StatusCodes
import org.eclipse.milo.opcua.stack.core.UaException
import org.eclipse.milo.opcua.stack.core.types.builtin.*
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class ValveNamespace(private val server: OpcUaServer,
                     private var namespaceIndex: UShort)
    : Namespace {

    private val logger = LoggerFactory.getLogger(javaClass)
    private var subscriptionModel: SubscriptionModel = SubscriptionModel(server, this)
    private val valveSensor = ValveSensor()

    init {

        val folderNodeId = NodeId(namespaceIndex, "Valve")

        val folderNode = UaFolderNode(
                server.nodeMap,
                folderNodeId,
                QualifiedName(namespaceIndex, "Valve"),
                LocalizedText.english("Valve")
        )

        server.nodeMap.addNode(folderNode)

        try {
            server.uaNamespace.addReference(
                    Identifiers.ObjectsFolder,
                    Identifiers.Organizes,
                    true,
                    folderNodeId.expanded(),
                    NodeClass.Object
            )
        } catch (e: UaException) {
            e.printStackTrace()
        }

        addValveSensorNodes(folderNode)
    }

    private fun addValveSensorNodes(rootNode: UaFolderNode) {
        val dynamicFolder = UaFolderNode(
                server.nodeMap,
                NodeId(namespaceIndex, "Valve"),
                QualifiedName(namespaceIndex, "Valve"),
                LocalizedText.english("Valve")
        )

        server.nodeMap.addNode(dynamicFolder)
        rootNode.addOrganizes(dynamicFolder)

        val name = "Valve State"
        val node = UaVariableNode.UaVariableNodeBuilder(server.nodeMap)
                .setNodeId(NodeId(namespaceIndex, "Valve/State"))
                .setAccessLevel(ubyte(AccessLevel.getMask(AccessLevel.READ_ONLY)))
                .setBrowseName(QualifiedName(namespaceIndex, name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(Identifiers.Integer)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .build()

        node.value = DataValue(Variant(valveSensor.state))

        val delegate = AttributeDelegateChain.create(
                object : AttributeDelegate {
                    @Throws(UaException::class)
                    override fun getValue(context: AttributeContext?, node: VariableNode): DataValue {
                        return DataValue(Variant(valveSensor.state.value))
                    }
                }
        )

        node.setAttributeDelegate(delegate)

        server.nodeMap.addNode(node)
        dynamicFolder.addOrganizes(node)
    }

    override fun getNamespaceIndex(): UShort {
        return namespaceIndex
    }

    override fun getNamespaceUri(): String? {
        return null
    }

    override fun read(context: AttributeManager.ReadContext,
                      maxAge: Double?,
                      timestamps: TimestampsToReturn,
                      readValueIds: List<ReadValueId>) {

        val results = Lists.newArrayListWithCapacity<DataValue>(readValueIds.size)

        for (readValueId in readValueIds) {
            val node = server.nodeMap[readValueId.nodeId]

            if (node != null) {
                val value = node.readAttribute(
                        AttributeContext(context),
                        readValueId.attributeId,
                        timestamps,
                        readValueId.indexRange,
                        readValueId.dataEncoding
                )

                results.add(value)
            } else {
                results.add(DataValue(StatusCodes.Bad_NodeIdUnknown))
            }
        }

        context.complete(results)
    }

    override fun write(context: AttributeManager.WriteContext, writeValues: List<WriteValue>) {

    }

    override fun onDataItemsCreated(dataItems: List<DataItem>) {
        subscriptionModel.onDataItemsCreated(dataItems)
    }

    override fun onDataItemsModified(dataItems: List<DataItem>) {
        subscriptionModel.onDataItemsModified(dataItems)
    }

    override fun onDataItemsDeleted(dataItems: List<DataItem>) {
        subscriptionModel.onDataItemsDeleted(dataItems)
    }

    override fun onMonitoringModeChanged(monitoredItems: List<MonitoredItem>) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems)
    }

    override fun browse(context: AccessContext, nodeId: NodeId): CompletableFuture<List<Reference>>? {
        return null
    }

    companion object {
        val NAMESPACE_URI = "urn:valve"
    }
}
