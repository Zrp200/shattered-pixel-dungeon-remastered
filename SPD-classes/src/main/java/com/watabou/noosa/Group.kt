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
     * does a specified [action] on alive [children], with an optional [predicate] that allows further restriction
     *
     * [children] is wrapped after filtering, so there is no risk of concurrent modification.
     */
    private inline fun forEachAlive(action: (Gizmo)->Any?, predicate: (Gizmo)->Boolean = {true}) = synchronized(this) {
        for(child in children.filter { it.alive && predicate(it) }) action(child)
    }

    /**
     * Updates this group and all of its children.
     */
    @Synchronized
    override fun update() = forEachAlive(action = Gizmo::update, predicate = Gizmo::active)

    /**
     * Draws this group and all of its children.
     */
    @Synchronized
    override fun draw() = forEachAlive(action = Gizmo::draw, predicate = Gizmo::visible)

    /**
     * Kills this group and all of its children.
     */
    @Synchronized
    override fun kill() {
        forEachAlive(Gizmo::kill)
        super.kill()
    }

    /**
     * Destroy this group and all of its children.
     */
    @Synchronized
    override fun destroy() {
        while (children.size > 0) {
            children[0].destroy()
        }
        children.clear()
        super.destroy()
    }

    /**
     * adds the [g] to the list using the specified [add] function,
     * unless it already contains the Gizmo, in which case [onContain] is called
     *
     * this function is synchronized
     */
    private inline fun add(g: Gizmo,
                           add: (Gizmo)->Any? = children::add,
                           onContain: (Gizmo)->Unit) = synchronized(this) {
        if(g.parent == this) onContain(g)
        else {
            g.parent?.remove(g)
            add(g)
            g.parent = this
        }
    }

    /**
     * Adds [g] to [children] unless it's already there. New children are added to the front.
     */
    fun add(g: Gizmo) = add(g) {}

    /**
     * Adds [g] to the front of [children].
     *
     * Gizmos are updated and drawn from back to front.
     */
    fun addToFront(g: Gizmo) = add(g, onContain = ::bringToFront)

    /**
     * Moves [g] to the front of [children] if it is found among them.
     *
     * Gizmos are updated and drawn from back to front.
     */
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
    fun addToBack(g: Gizmo) = add(g, onContain=::sendToBack, add={ children.add(0, it)})

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