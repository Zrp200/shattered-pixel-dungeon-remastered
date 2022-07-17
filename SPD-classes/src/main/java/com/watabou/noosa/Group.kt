/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.noosa

import com.watabou.utils.Reflection

/**
 * Gizmo-container for other gizmos.
 */
open class Group : Gizmo() {

    /**
     * Gizmos whose [parent] is this group.
     */
    protected var children: ArrayList<Gizmo> = ArrayList()

    /**
     * Apply an action to all [children] of this group.
     */
    @Synchronized
    private fun applyToChildren(action: Gizmo.() -> Unit, condition: (Gizmo) -> Boolean = { _ -> true }) {
        for (i in 0 until children.size) {
            if (i >= children.size) break
            val child = children[i]
            if (condition(child)) {
                child.action()
            }
        }
    }

    /**
     * Updates this group and all of its children.
     */
    override fun update() = applyToChildren(Gizmo::update) { g: Gizmo -> g.alive && g.active }

    /**
     * Draws this group and all of its children.
     */
    override fun draw() = applyToChildren(Gizmo::draw) { g: Gizmo -> g.alive && g.visible }

    /**
     * Kills this group and all of its children.
     */
    override fun kill() = applyToChildren(Gizmo::kill) { g: Gizmo -> g.alive }.also { super.kill() }

    /**
     * Destroy this group and all of its children.
     */
    override fun destroy() = applyToChildren(Gizmo::destroy).also { children.clear(); super.destroy() }

    /**
     * Adds a gizmo to the [children] unless it's already there. New children are added to the front.
     */
    @Synchronized
    fun add(g: Gizmo) {
        if (g.parent === this) return
        g.parent?.remove(g)
        children.add(g)
        g.parent = this
    }

    /**
     * Adds a gizmo to the front of [children].
     *
     * Gizmos are updated and drawn from back to front.
     */
    @Synchronized
    fun addToFront(g: Gizmo) {
        if (g.parent === this) return bringToFront(g)
        g.parent?.remove(g)
        children.add(g)
        g.parent = this
    }

    /**
     * Moves a gizmo to the front of [children] if it is found among them.
     *
     * Gizmos are updated and drawn from back to front.
     */
    @Synchronized
    fun bringToFront(g: Gizmo) {
        g.takeIf(children::contains)?.also {
            children.remove(it)
            children.add(it)
        }
    }

    /**
     * Adds a gizmo to the back of [children].
     *
     * Gizmos are updated and drawn from back to front.
     */
    @Synchronized
    fun addToBack(g: Gizmo) {
        if (g.parent === this) return sendToBack(g)
        g.parent?.remove(g)
        children.add(0, g)
        g.parent = this
    }

    /**
     * Moves a gizmo to the back of [children] if it is found among them.
     *
     * Gizmos are updated and drawn from back to front.
     */
    @Synchronized
    fun sendToBack(g: Gizmo) {
        g.takeIf(children::contains)?.also {
            children.remove(it)
            children.add(0, it)
        }
    }

    /**
     * Gets an [available][firstAvailableOrNull] [child][children] for reuse or creates a new one if none are found.
     *
     * @param c class of which an instance is to be obtained
     * @return obtained gizmo
     */
    @Synchronized
    fun <T : Gizmo> recycle(c: Class<T>): T = firstAvailableOrNull(c) ?: Reflection.newInstance(c).also { add(it) }

    /**
     * Finds a [dead][alive] gizmo of specified class among [children].
     *
     * @param c class of which an instance is to be found
     * @return found gizmo or null if none are found
     */
    @Synchronized
    fun <T : Gizmo> firstAvailableOrNull(c: Class<T>): T? = children.filterIsInstance(c).firstOrNull { !it.alive }

    /**
     * Detaches a [child][children] from this group.
     *
     * @param g child to detach
     * @return detached child
     */
    @Synchronized
    open fun <T : Gizmo> remove(g: T): T? = g.takeIf(children::remove)?.apply { parent = null }

    /**
     * Detaches all [children] from this group.
     */
    @Synchronized
    open fun clear() {
        children.apply {
            forEach { it.parent = null }
            clear()
        }
    }
}