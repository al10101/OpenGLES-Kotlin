package com.albertocabrera.controlstester.utils

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.Matrix
import com.albertocabrera.controlstester.R
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class Sphere(context: Context, resolution: Int, val radius: Float, rgb: FloatArray, alpha: Float) {

    val modelMatrix = FloatArray(16)

    private val stacks = resolution
    private val slices = resolution

    private var vIdx = 0
    private var cIdx = 0
    private var tIdx = 0

    private var colorIncrement = 0f

    // slices * 2 takes into account the fact that two triangles are needed for each face bound by
    // the slice and stack borders. The +2 handles the fact that the first two vertices are also the
    // last vertices, so are duplicated
    private val vertices = FloatArray(POSITION_COUNT_3D * (slices * 2 + 2) * stacks)
    private val colors = FloatArray(COLOR_COUNT * (slices * 2 + 2) * stacks)

    // Buffers to contain data
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer

    // Default shader variables
    private var program = 0
    private var uProjectionMatrixLocation = -1
    private var uViewMatrixLocation = -1
    private var uModelMatrixLocation = -1
    private var aPositionLocation = -1
    private var aColorLocation = -1

    init {

        // Precompute all circumference positions
        val circle = FloatArray(slices * POSITION_COUNT_2D)
        generateCircumference(circle, slices)

        // The outer loop, going from south to north
        for (phiIdx in 0 until stacks) {

            // The first circle
            val phi0 = PI_F * ( (phiIdx + 0) * (1f / stacks) - 0.5f )

            // The second circle
            val phi1 = PI_F * ( (phiIdx + 1) * (1f / stacks) - 0.5f )

            // Pre-calculated values
            val c0 = cos(phi0)
            val s0 = sin(phi0)
            val c1 = cos(phi1)
            val s1 = sin(phi1)

            // The inner loop, going through the whole circumference
            tIdx = 0
            for (thetaIdx in 0 until slices) {

                // First point, xyz coordinates
                vertices[vIdx + 0] = radius * c0 * circle[tIdx + 0]
                vertices[vIdx + 1] = radius * s0
                vertices[vIdx + 2] = radius * c0 * circle[tIdx + 1]

                // Second point, xyz coordinates
                vertices[vIdx + 3] = radius * c1 * circle[tIdx + 0]
                vertices[vIdx + 4] = radius * s1
                vertices[vIdx + 5] = radius * c1 * circle[tIdx + 1]

                // Bot points are the same color
                colors[cIdx + 0] = rgb[0] * colorIncrement
                colors[cIdx + 1] = rgb[1] * colorIncrement
                colors[cIdx + 2] = rgb[2] * colorIncrement
                colors[cIdx + 3] = alpha
                colors[cIdx + 4] = rgb[0] * colorIncrement
                colors[cIdx + 5] = rgb[1] * colorIncrement
                colors[cIdx + 6] = rgb[2] * colorIncrement
                colors[cIdx + 7] = alpha

                // Increments proportional to the number of components
                vIdx += 2 * POSITION_COUNT_3D
                cIdx += 2 * COLOR_COUNT
                tIdx += POSITION_COUNT_2D

            }

            // Increment in the color
            colorIncrement += 1f / stacks

            // Create a degenerate triangle to connect stacks
            vertices[vIdx + 3] = vertices[vIdx - 3]
            vertices[vIdx + 0] = vertices[vIdx + 3]
            vertices[vIdx + 4] = vertices[vIdx - 2]
            vertices[vIdx + 1] = vertices[vIdx + 4]
            vertices[vIdx + 5] = vertices[vIdx - 1]
            vertices[vIdx + 2] = vertices[vIdx + 5]

        }

        // The vertex, color and normal data are converted to byte arrays that OpenGL can use
        vertexBuffer = makeFloatBuffer(vertices)
        colorBuffer = makeFloatBuffer(colors)

        // Initialize graphic program
        val vertexResource = R.raw.vertex_shader
        val vertexCode = readTextFileFromResource(context, vertexResource)
        val fragmentResource = R.raw.fragment_shader
        val fragmentCode = readTextFileFromResource(context, fragmentResource)
        program = createProgram(vertexCode, fragmentCode)
        validateProgram(program)

        // Set initial position
        Matrix.setIdentityM(modelMatrix, 0)

    }

    fun useProgram() {
        glUseProgram(program)
    }

    private fun getLocations() {

        // Get location of the uniforms
        uProjectionMatrixLocation = glGetUniformLocation(program, U_PROJECTION_MATRIX)
        uViewMatrixLocation = glGetUniformLocation(program, U_VIEW_MATRIX)
        uModelMatrixLocation = glGetUniformLocation(program, U_MODEL_MATRIX)

        // Get location of the attributes
        aPositionLocation = glGetAttribLocation(program, A_POSITION)
        aColorLocation = glGetAttribLocation(program, A_COLOR)

    }

    fun bindData() {

        // Get locations
        getLocations()

        // Positions
        glVertexAttribPointer(
            aPositionLocation, POSITION_COUNT_3D,
            GL_FLOAT, false, POSITION_STRIDE_3D, vertexBuffer
        )
        glEnableVertexAttribArray(aPositionLocation)

        // Colors
        glVertexAttribPointer(
            aColorLocation, COLOR_COUNT,
            GL_FLOAT, false, COLOR_STRIDE, colorBuffer
        )
        glEnableVertexAttribArray(aColorLocation)

    }

    // Set fragment matrices
    fun fragmentUniforms(projectionMatrix: FloatArray, viewMatrix:FloatArray, modelMatrix: FloatArray) {
        glUniformMatrix4fv(uProjectionMatrixLocation, 1, false, projectionMatrix, 0)
        glUniformMatrix4fv(uViewMatrixLocation, 1, false, viewMatrix, 0)
        glUniformMatrix4fv(uModelMatrixLocation, 1, false, modelMatrix, 0)
    }

    // Do the actual drawing
    fun drawPrimitives(modelMatrix: FloatArray) {
        // Do the actual drawing
        glDrawArrays(GL_TRIANGLE_STRIP, 0, (slices + 1) * 2 * (stacks - 1) + 2)
    }

}