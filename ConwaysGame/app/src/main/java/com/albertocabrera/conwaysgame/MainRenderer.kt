package com.albertocabrera.conwaysgame

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.random.Random

private const val TAG = "Renderer"

private const val BYTES_PER_FLOAT = 4
private const val BYTES_PER_INT = 4
private const val COORDINATES_2D = 2

private const val DEATH = 0
private const val ALIVE = 1
private val COLORS_MAP = mapOf(
    DEATH to floatArrayOf(0f, 0f, 0f, 1f),
    ALIVE to floatArrayOf(1f, 1f, 1f, 1f)
)

class MainRenderer: GLSurfaceView.Renderer {

    private lateinit var vertexBuffer: FloatBuffer
    private val rand = Random(seed = System.nanoTime())

    // Set the grid's size (initial grid's measures: x={-1, 1}, y={-1, 1} -> total size = 2)
    private val size = 100                                                           //   |
    private val step = 2f / size.toFloat() // <--------------------------------------------
    private val nSquares = size * size
    private val nVertex = nSquares * 6

    // Define the points and colors
    private val vertexData = FloatArray(nVertex * COORDINATES_2D)
    private val states = IntArray(nSquares)

    // Attributes location
    private var aPositionLocation = 0
    private var uColorLocation = 0

    // Shaders programs
    private var graphicsProgram = 0
    private var computeProgram = 0

    private lateinit var latticeIn: IntBuffer
    private lateinit var latticeOut: IntBuffer

    private val vertexShaderCode = """
        attribute vec4 aPosition;
        void main() {
            gl_Position = aPosition;
        }
    """.trimIndent()
    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;
        void main() {
            gl_FragColor = uColor;
        }
    """.trimIndent()

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        // Set background color
        glClearColor(1f, 1f, 1f, 1f)

        // Compute all the initial data
        initStates()

        Log.i(TAG, " SIZE= $size")
        Log.i(TAG, " SQUARES= $nSquares")
        Log.i(TAG, " VERTEX= $nVertex")

        // Define shaders
        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)

        // Create OpenGL ES graphics program and add
        graphicsProgram = glCreateProgram()
        glAttachShader(graphicsProgram, vertexShader)
        glAttachShader(graphicsProgram, fragmentShader)
        glLinkProgram(graphicsProgram)
        glUseProgram(graphicsProgram)

        // Set buffers
        vertexBuffer = makeFloatBuffer(vertexData)

        // Prepare and send position attributes
        aPositionLocation = glGetAttribLocation(graphicsProgram, "aPosition")
        glEnableVertexAttribArray(aPositionLocation)
        glVertexAttribPointer(aPositionLocation, COORDINATES_2D, GL_FLOAT,
            false, COORDINATES_2D * BYTES_PER_FLOAT, vertexBuffer)

        // Set color location
        uColorLocation = glGetUniformLocation(graphicsProgram, "uColor")
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to fill the entire surface
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        // Clear the rendering surface
        glClear(GL_COLOR_BUFFER_BIT)

        // Update grid
        updateCpu()
        //updateGpu()

        // Draw the squares
        for (i in 0 until nSquares) {
            val rgba = COLORS_MAP[ states[i] ]!!
            glUniform4f(uColorLocation, rgba[0], rgba[1], rgba[2], rgba[3])
            glDrawArrays(GL_TRIANGLES, i*6, 6)
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, shaderCode)
        glCompileShader(shader)
        return shader
    }

    private fun makeFloatBuffer(arr: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(arr.size * BYTES_PER_FLOAT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(arr)
        fb.position(0)
        return fb
    }

    private fun makeIntBuffer(arr: IntArray): IntBuffer {
        val bb = ByteBuffer.allocateDirect(arr.size * BYTES_PER_INT)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asIntBuffer()
        fb.put(arr)
        fb.position(0)
        return fb
    }

    private fun initStates() {

        // Fill initial random states
        for (i in 0 until size) {
            for (j in 0 until size) {
                val ij = j * size + i
                val vIndex = 12 * ij

                // To define a square (cell), four xy cartesian coordinates are needed. Since the
                // normalized device coordiantes range from -1 to +1 in both axis, the step is defined
                // as 2 units divided by the number of squares per axis
                val x0 = -1f + (j+0).toFloat() * step ; val y0 = -1f + (i+0).toFloat() * step
                val x1 = -1f + (j+1).toFloat() * step ; val y1 = -1f + (i+0).toFloat() * step
                val x2 = -1f + (j+0).toFloat() * step ; val y2 = -1f + (i+1).toFloat() * step
                val x3 = -1f + (j+1).toFloat() * step ; val y3 = -1f + (i+1).toFloat() * step

                // The first triangle
                vertexData[vIndex+ 0] = x0 ; vertexData[vIndex+ 1] = y0
                vertexData[vIndex+ 2] = x1 ; vertexData[vIndex+ 3] = y1
                vertexData[vIndex+ 4] = x2 ; vertexData[vIndex+ 5] = y2

                // The second triangle, two xy coordinates are repeated from the first triangle
                vertexData[vIndex+ 6] = x2 ; vertexData[vIndex+ 7] = y2
                vertexData[vIndex+ 8] = x3 ; vertexData[vIndex+ 9] = y3
                vertexData[vIndex+10] = x1 ; vertexData[vIndex+11] = y1

                // Fill the colors/states
                val state = rand.nextInt(until = 2)
                states[ij] = state
            }
        }

    }

    private fun updateCpu() {

        val oldStates = states.copyOf()

        for (i in 0 until size) {

            for (j in 0 until size) {

                // Compute 8 neighbors
                val ij0 = ( abs(j-1) %size ) * size + i
                val ij1 = ( (j+1)%size ) * size + i
                val ij2 = j * size + ( abs(i-1)%size )
                val ij3 = j * size + ( (i+1)%size )
                val ij4 = ( abs(j-1)%size ) * size + ( abs(i-1)%size )
                val ij5 = ( abs(j-1)%size ) * size + ( (i+1)%size )
                val ij6 = ( (j+1)%size ) * size + ( abs(i-1)%size )
                val ij7 = ( (j+1)%size ) * size + ( (i+1)%size )

                // Sum all (alive) neighbors
                val total = oldStates[ij0] + oldStates[ij1] + oldStates[ij2] + oldStates[ij3] +
                        oldStates[ij4] + oldStates[ij5] + oldStates[ij6] + oldStates[ij7]

                // Check current state and modify if necessary
                val ij = j * size + i
                if (oldStates[ij] == ALIVE) {
                    if (total < 2 || total > 3) {
                        states[ij] = DEATH
                    }
                } else {
                    if (total == 3) {
                        states[ij] = ALIVE
                    }
                }

            }

        }

    }

}