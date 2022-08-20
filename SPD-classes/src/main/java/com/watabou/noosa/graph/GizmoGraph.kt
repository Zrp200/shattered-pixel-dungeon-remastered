package com.watabou.noosa.graph

import com.watabou.noosa.Group
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph

private val graph: Graph = SingleGraph("GizmoGraph")

var graphOn = false
    private set

fun initGraph() {
    System.setProperty("org.graphstream.ui", "swing")
    graph.isStrict = false
    graph.setAutoCreate(true)
    graph.display()
    graphOn = true
}

@Suppress("NewApi")
fun updateGraph(group: Group) {
    with (graph) {
        addNode("$group").setAttribute("retain", true)

        updateNode(group)

        forEach { if (it != null && !it.hasAttribute("retain")) graph.removeNode(it) }
        forEach {
            it.removeAttribute("retain")
            it.setAttribute("ui.label", it.id.substringAfterLast(".").substringBefore("@"))
        }
    }
}

private fun updateNode(group: Group) {
    synchronized(group) {
        (Group::class.java.getDeclaredField("children")
            .apply { isAccessible = true }
            .get(group) as ArrayList<*>)
            .forEach { child ->
                graph.addEdge("$group->$child", "$group", "$child")
                graph.getNode("$child").setAttribute("retain")
                if (child is Group) updateNode(child)
            }
    }
}
