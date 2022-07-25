package com.watabou.glwrap

import com.badlogic.gdx.Gdx

abstract class Variable(protected val location: Int) {

    fun enable() {
        Gdx.gl.glEnableVertexAttribArray(location)
    }

    fun disable() {
        Gdx.gl.glDisableVertexAttribArray(location)
    }
}