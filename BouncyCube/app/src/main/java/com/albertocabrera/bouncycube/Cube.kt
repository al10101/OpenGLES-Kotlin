package com.albertocabrera.bouncycube

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11

class Cube {

    private val vertices = floatArrayOf(
        -1.0f,  1.0f,  1.0f,
         1.0f,  1.0f,  1.0f,
         1.0f, -1.0f,  1.0f,
        -1.0f, -1.0f,  1.0f,

        -1.0f,  1.0f, -1.0f,
         1.0f,  1.0f, -1.0f,
         1.0f, -1.0f, -1.0f,
        -1.0f, -1.0f, -1.0f
    )

    private val maxColor = 255.toByte()

    private val colors = byteArrayOf(
        maxColor, maxColor, 0, maxColor,
        0, maxColor, maxColor, maxColor,
        0,        0,        0, maxColor,
        maxColor, 0, maxColor, maxColor,

        maxColor, 0,        0, maxColor,
        0, maxColor,        0, maxColor,
        0,        0, maxColor, maxColor,
        0,        0,        0, maxColor
    )

    private val tFan1 = byteArrayOf(
        1, 0, 3,
        1, 3, 2,
        1, 2, 6,
        1, 6, 5,
        1, 5, 4,
        1, 4, 0
    )

    private val tFan2 = byteArrayOf(
        7, 4, 5,
        7, 5, 6,
        7, 6, 2,
        7, 2, 3,
        7, 3, 0,
        7, 0, 4
    )

    var vbb: ByteBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
    private var mFVertexBuffer: FloatBuffer
    private var mColorBuffer: ByteBuffer
    private var mTFan1: ByteBuffer
    private var mTFan2: ByteBuffer

    init {

        vbb.order(ByteOrder.nativeOrder())
        mFVertexBuffer = vbb.asFloatBuffer()
        mFVertexBuffer.put(vertices)
        mFVertexBuffer.position(0)

        mColorBuffer = ByteBuffer.allocateDirect(colors.size)
        mColorBuffer.put(colors)
        mColorBuffer.position(0)

        mTFan1 = ByteBuffer.allocateDirect(tFan1.size)
        mTFan1.put(tFan1)
        mTFan1.position(0)

        mTFan2 = ByteBuffer.allocateDirect(tFan2.size)
        mTFan2.put(tFan2)
        mTFan2.position(0)

    }

    fun draw (gl: GL10) {

        gl.glVertexPointer(3, GL11.GL_FLOAT, 0, mFVertexBuffer)
        gl.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 0, mColorBuffer)

        gl.glDrawElements(GL11.GL_TRIANGLE_FAN, 6 * 3, GL11.GL_UNSIGNED_BYTE, mTFan1)
        gl.glDrawElements(GL11.GL_TRIANGLE_FAN, 6 * 3, GL11.GL_UNSIGNED_BYTE, mTFan2)

    }

}