package com.watabou.noosa.graph

import com.watabou.noosa.Group
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph

private val graph: Graph = SingleGraph("GizmoGraph")

var on = false
    private set

fun initGraph() {
    System.setProperty("org.graphstream.ui", "swing")
    graph.isStrict = false
    graph.setAutoCreate(true)
    graph.display()
    on = true
}

@Suppress("NewApi")
fun updateGraph(group: Group) {
    graph.addNode("$group").setAttribute("retain", true)

    updateNode(group)

    graph.nodes().forEach { if (it != null && !it.hasAttribute("retain")) graph.removeNode(it) }
    graph.forEach { it.removeAttribute("retain") }

    for (node in graph) {
        node.setAttribute("ui.label", node.id.substringAfterLast(".").substringBefore("@"))
    }
}

private fun updateNode(group: Group) {
    synchronized(group) {
        val childrenField = Group::class.java.getDeclaredField("children")
        childrenField.isAccessible = true
        val children = childrenField.get(group) as ArrayList<*>
        for (child in children) {
            graph.addEdge("$group->$child", "$group", "$child")
            graph.getNode("$child").setAttribute("retain")
            if (child is Group) updateNode(child)
        }
    }
}
