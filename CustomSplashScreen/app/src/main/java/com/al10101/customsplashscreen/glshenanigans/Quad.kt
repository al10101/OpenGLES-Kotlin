package com.al10101.customsplashscreen.glshenanigans

import android.opengl.GLES20.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private const val POSITION_COMPONENT_COUNT = 2
private const val STRIDE = POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT

class Quad(width: Float, height: Float) {

    private val vertexBuffer: FloatBuffer
    private val fanOrder: ByteBuffer = ByteBuffer.allocateDirect(6)

    init {

        // Since it will be drawn with the fan mode, we add positions and fan
        val positions = floatArrayOf(
            -width/2f, -height/2f,
            -width/2f,  height/2f,
            width/2f,  height/2f,
            width/2f, -height/2f
        )

        vertexBuffer = ByteBuffer
            .allocateDirect(positions.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(positions)

        val fan = byteArrayOf(0, 1, 2, 0, 2, 3)
        fanOrder.put(fan)
        fanOrder.position(0)

    }

    fun bindDate(program: ShaderProgram) {
        vertexBuffer.position(0)
        glVertexAttribPointer(program.aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexBuffer)
        glEnableVertexAttribArray(program.aPositionLocation)
        vertexBuffer.position(0)
    }

    fun draw() {
        glDrawElements(GL_TRIANGLE_FAN, 6, GL_UNSIGNED_BYTE, fanOrder)
    }

}