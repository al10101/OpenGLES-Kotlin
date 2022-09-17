package com.albertocabrera.sierpinskigasket

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random

private const val TAG = "Renderer"

private const val BYTES_PER_FLOAT = 4
private const val COORDINATES_2D = 2

class MainRenderer: GLSurfaceView.Renderer {

    private lateinit var vertexData: FloatBuffer
    private val rand = Random(seed = System.nanoTime())

    private val numPoints = 10_000
    private var kounter = 0
    private val points = FloatArray((numPoints + 3) * COORDINATES_2D)
    private var uColorLocation: Int = 0

    private val vertexShaderCode =
                "attribute vec4 aPosition;" +
                "void main() {" +
                "   gl_Position = aPosition;" +
                "   gl_PointSize = 2.0;" +
                "}"
    private val fragmentShaderCode =
                "precision mediump float;" +
                "uniform vec4 uColor;" +
                "void main() {" +
                "   gl_FragColor = uColor;" +
                "}"

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        // Set background color
        glClearColor(1f, 1f, 1f, 1f)

        // Initialize the triangles
        Log.i(TAG, " Initializing $numPoints points...")
        initPoints()
        Log.i(TAG, " Points initialized successfully")

        // Define shaders
        val vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
        Log.i(TAG, " Shaders loaded successfully")

        // Create OpenGL ES Program
        val program = glCreateProgram()
        glAttachShader(program, vertexShader)
        glAttachShader(program, fragmentShader)
        glLinkProgram(program)
        Log.i(TAG, " OpenGL program attached")

        // Add program to OpenGL ES environment
        glUseProgram(program)

        // Send to the gpu
        Log.i(TAG, " Initializing Float Buffer")
        vertexData = ByteBuffer
            .allocateDirect(points.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexData.put(points)
        vertexData.position(0)
        Log.i(TAG, " Float Buffer initialized successfully")

        // Get position location
        val aPositionLocation = glGetAttribLocation(program, "aPosition")

        // Prepare the triangle coordinate data
        glVertexAttribPointer(aPositionLocation, COORDINATES_2D, GL_FLOAT,
            false, COORDINATES_2D * BYTES_PER_FLOAT, vertexData)

        // Enable a handle to the triangle vertices
        glEnableVertexAttribArray(aPositionLocation)

        // Get color location
        uColorLocation = glGetUniformLocation(program, "uColor")

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to fill the entire surface
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        // Clear the rendering surface
        glClear(GL_COLOR_BUFFER_BIT)

        // Draw the contour
        glUniform4f(uColorLocation, 1f, 0f, 0f, 1f)
        if (kounter < numPoints) {
            kounter += 20
            glDrawArrays(GL_LINE_LOOP, 0, 3)
        }

        // Draw the points
        glUniform4f(uColorLocation, 0f, 0f, 0f, 1f)
        glDrawArrays(GL_POINTS, 3, kounter)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, shaderCode)
        glCompileShader(shader)
        return shader
    }

    private fun initPoints() {

        // A triangle in the plane z= 0
        val vertices = floatArrayOf(
            -1f, -1f,
             0f,  1f,
             1f, -1f
        )

        // Add initial contour to the points
        points[0] = vertices[0] ; points[1] = vertices[1]
        points[2] = vertices[2] ; points[3] = vertices[3]
        points[4] = vertices[4] ; points[5] = vertices[5]

        // An arbitrary initial point inside the triangle
        points[6] = 0.25f ; points[7] = 0.50f

        // Compute and store numPoints-1 new points
        for (k in 4 until numPoints+3) {
            // Pick a vertex at random
            val j = rand.nextInt(until = 3)
            // Get indices
            val j2 = 2*j
            val k2 = 2*k
            val k2m1 = 2*(k-1)
            // Compute the point halfway between selected vertex and previous point
            val x = (points[k2m1 + 0] + vertices[j2 + 0]) / 2f
            val y = (points[k2m1 + 1] + vertices[j2 + 1]) / 2f
            points[k2 + 0] = x
            points[k2 + 1] = y
        }

    }


}