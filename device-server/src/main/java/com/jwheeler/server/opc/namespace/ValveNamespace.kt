package com.jwheeler.server.opc.namespace

import org.eclipse.milo.opcua.sdk.core.AccessLevel
import org.eclipse.milo.opcua.sdk.core.Reference
import org.eclipse.milo.opcua.sdk.server.OpcUaServer
import org.eclipse.milo.opcua.sdk.server.api.*
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode
import org.eclipse.milo.opcua.stack.core.Identifiers
import org.eclipse.milo.opcua.stack.core.UaException
import org.eclipse.milo.opcua.stack.core.types.builtin.*
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue
import java.util.concurrent.CompletableFuture

class ValveNamespace(private val server: OpcUaServer, private var namespaceIndex: UShort) : Namespace {

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

        node.value = DataValue(Variant(0))

        server.nodeMap.addNode(node)
        dynamicFolder.addOrganizes(node)
    }

    override fun getNamespaceIndex(): UShort {
        return namespaceIndex
    }

    override fun getNamespaceUri(): String? {
        return null
    }

    override fun read(context: AttributeManager.ReadContext, maxAge: Double?, timestamps: TimestampsToReturn, readValueIds: List<ReadValueId>) {

    }

    override fun write(context: AttributeManager.WriteContext, writeValues: List<WriteValue>) {

    }

    override fun onDataItemsCreated(dataItems: List<DataItem>) {

    }

    override fun onDataItemsModified(dataItems: List<DataItem>) {

    }

    override fun onDataItemsDeleted(dataItems: List<DataItem>) {

    }

    override fun onMonitoringModeChanged(monitoredItems: List<MonitoredItem>) {

    }

    override fun browse(context: AccessContext, nodeId: NodeId): CompletableFuture<List<Reference>>? {
        return null
    }

    companion object {

        val NAMESPACE_URI = "urn:valve"
    }
}
