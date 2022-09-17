package com.albertocabrera.sierpinskigasket

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private var renderSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(this)

        // Request an OpenGL ES 2.0 Context
        glSurfaceView.setEGLContextClientVersion(2)
        // Assign our renderer
        glSurfaceView.setRenderer(MainRenderer())
        renderSet = true
        setContentView(glSurfaceView)
    }

    override fun onPause() {
        super.onPause()
        if (renderSet) {
            glSurfaceView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (renderSet) {
            glSurfaceView.onResume()
        }
    }

}