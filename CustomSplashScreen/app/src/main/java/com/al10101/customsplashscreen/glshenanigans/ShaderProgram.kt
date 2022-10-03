package com.al10101.customsplashscreen.glshenanigans

import android.content.Context
import android.opengl.GLES20.*
import com.al10101.customsplashscreen.R

const val U_TIME = "u_Time"
const val U_RESOLUTION = "u_Resolution"

const val A_POSITION = "a_Position"

class ShaderProgram(
    context: Context
) {

    val program by lazy {
        ShaderUtils.buildProgram(
            context.readTextFileFromResource(R.raw.vertex_shader),
            context.readTextFileFromResource(R.raw.fragment_shader)
        )
    }

    // Uniforms
    private val uTimeLocation by lazy {
        glGetUniformLocation(program, U_TIME)
    }
    private val uResolutionLocation by lazy {
        glGetUniformLocation(program, U_RESOLUTION)
    }

    // Attributes
    val aPositionLocation by lazy {
        glGetAttribLocation(program, A_POSITION)
    }

    fun useProgram() {
        glUseProgram(program)
    }

    fun setUniforms(time: Float, resolution: FloatArray) {
        glUniform1f(uTimeLocation, time)
        glUniform2fv(uResolutionLocation, 1, resolution, 0)
    }

}