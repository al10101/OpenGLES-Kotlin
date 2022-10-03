package com.al10101.customsplashscreen

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.al10101.customsplashscreen.glshenanigans.NANOSECONDS
import com.al10101.customsplashscreen.glshenanigans.Quad
import com.al10101.customsplashscreen.glshenanigans.ShaderProgram
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = GLSurfaceView(activity)
        view.setEGLContextClientVersion(2)
        view.setRenderer(activity?.let{
            SimpleRenderer(it)
        })
        return view
    }

    companion object {
        fun newInstance() = GLFragment()
    }

}

class SimpleRenderer(private val context: Context): GLSurfaceView.Renderer {

    private lateinit var resolution: FloatArray

    private lateinit var program: ShaderProgram
    private lateinit var quad: Quad
    private var globalStartTime: Long = 0

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 0f)

        globalStartTime = System.nanoTime()

        program = ShaderProgram(context)
        quad = Quad(2f, 2f)

    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        resolution = floatArrayOf(width.toFloat(), height.toFloat())
    }

    override fun onDrawFrame(p0: GL10?) {

        glClear(GL_COLOR_BUFFER_BIT)

        val currentTime = (System.nanoTime() - globalStartTime) / NANOSECONDS

        program.useProgram()
        program.setUniforms(currentTime, resolution)
        quad.bindDate(program)
        quad.draw()

    }

}