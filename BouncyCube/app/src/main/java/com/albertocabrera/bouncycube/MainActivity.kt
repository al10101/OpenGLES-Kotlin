package com.albertocabrera.bouncycube

import android.app.Activity
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : Activity() {

    private lateinit var view: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = GLSurfaceView(this)

        view.setRenderer(CubeRenderer())

        setContentView(view)

    }
}