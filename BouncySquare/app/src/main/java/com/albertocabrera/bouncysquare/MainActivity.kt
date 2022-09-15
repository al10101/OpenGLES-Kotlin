package com.albertocabrera.bouncysquare

import android.app.Activity
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager

class MainActivity : Activity() {

    private lateinit var view: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Create a GLSurfaceView instance
        view = GLSurfaceView(this)
        view.setRenderer(SquareRenderer(true, this.applicationContext))
        setContentView(view)

    }
}