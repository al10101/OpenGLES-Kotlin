package com.albertocabrera.bouncysquare

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11

private const val TAG = "Square"

class Square {

    // THe vertices of the square
    private val vertices: FloatArray = floatArrayOf(
            -1.0f, -1.0f,
             1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f,  1.0f
    )

    // Colors are converted to bytes internally anyway
    private val maxColor: Byte = 255.toByte()

    // Colors are defined similarly, but in this case there are four colors: red, green, blue,
    // alpha(transparency). These map directly to the four vertices shown earlier, so the first
    // color goes with the first vertex, and so on. Since we're using bytes, the color values
    // go from 0 to 255
    private val colors = byteArrayOf(
            maxColor, maxColor, 0, maxColor,
            0, maxColor, maxColor, maxColor,
            0,        0,        0, maxColor,
            maxColor, 0, maxColor, maxColor
    )

    // The Square is made of two triangles: one from the vertices 0, 3, and 1 and the other from
    // the indices 0, 2 and 3
    private val indices = byteArrayOf(
            0, 3, 1,
            0, 2, 3
    )

    // Texture coordinates
    private var textureCoords = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    )
    private val texIncrease = 1.0f

    // The texture information is saved in integer array
    private val textures = IntArray(1)

    private var vbb = ByteBuffer.allocateDirect(vertices.size * 4)
    private var tbb = ByteBuffer.allocateDirect(textureCoords.size * 4)
    private var mFVertexBuffer: FloatBuffer
    private var mColorBuffer: ByteBuffer
    private var mIndexBuffer: ByteBuffer
    private var mTextureBuffer: FloatBuffer

    init {

        vbb.order(ByteOrder.nativeOrder())
        mFVertexBuffer = vbb.asFloatBuffer()
        mFVertexBuffer.put(vertices)
        mFVertexBuffer.position(0)

        mColorBuffer = ByteBuffer.allocateDirect(colors.size)
        mColorBuffer.put(colors)
        mColorBuffer.position(0)

        mIndexBuffer = ByteBuffer.allocateDirect(indices.size)
        mIndexBuffer.put(indices)
        mIndexBuffer.position(0)

        tbb.order(ByteOrder.nativeOrder())
        mTextureBuffer = tbb.asFloatBuffer()
        mTextureBuffer.put(textureCoords)
        mTextureBuffer.position(0)

    }

    // Used by SquareRenderer.drawFrame()
    fun draw(gl: GL10) {

        // Indicate that the vertices are ordering their faces
        //gl.glFrontFace(GL11.GL_CW)

        // Specify the number of elements per vertex (two), that the data is floating point and
        // that the stride is 0 bytes.
        // Expect vertex and color data
        gl.glVertexPointer(2, GL11.GL_FLOAT, 0, mFVertexBuffer)
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)

        // The size is four elements, with the RGBA quadruplets using unsigned bytes and stride
        // also 0
        //gl.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 0, mColorBuffer)
        //gl.glEnableClientState(GL10.GL_COLOR_ARRAY)

        // Enable textures. ES only supports 2D.
        // The blending is where color and destination color are mixed.
        //gl.glEnable(GL10.GL_TEXTURE_2D)
        //gl.glEnable(GL10.GL_BLEND)

        // The blend function determines how the source and destination pixels/fragments are mixed
        // together.
        //gl.glBlendFunc(GL10.GL_ONE, GL10.GL_SRC_COLOR)
        //  Ensure that we want the current texture
        //gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])

        // Texture coordinates are handed off to the hardware
        //gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextureBuffer)
        //gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY)

        // The actual "draw" command. The format of the geometry with the number of vertices
        //gl.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_BYTE, mIndexBuffer)
        // It takes the information from vertex, color and position
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)

        // Disable the client state for texture in the same way it was disabled for colors and
        // vertices
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY)
        //gl.glDisableClientState(GL10.GL_COLOR_ARRAY)
        //gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY)

        // Resets the front face ordering back
        //gl.glFrontFace(GL11.GL_CCW)

    }

    fun createTexture(gl: GL10, contextRegF: Context, resource: Int): Int {

        // Load the bitmap, letting the loader handle any image format that Android can read in
        val image = BitmapFactory.decodeResource(contextRegF.resources, resource)

        // Unused texture "name", which in reality is just a number. This ensures that each texture
        // use has a unique identifier. If you want to reuse identifiers, then you'd call glDeleteTextures()
        gl.glGenTextures(1, textures, 0)

        // Bound the current 2D texture
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])

        // This utility class binds OpenGL ES and the Android API is used. This utility specifies
        // the 2D image for the bitmap that we created
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, image, 0)

        // Set the params that are required on the Android. Without them, the texture has a default
        // "filter" value that is unnecessary at this time
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())

        // Allow OpenGL to compute automatically the mipmapping
        gl.glHint(GL11.GL_GENERATE_MIPMAP,GL10.GL_NICEST)
        gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL11.GL_GENERATE_MIPMAP,GL10.GL_TRUE.toFloat())

        // Explicitly say to recycle the bitmap
        image.recycle()

        return resource

    }

}