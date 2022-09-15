package com.albertocabrera.bouncycube

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sin
import kotlin.math.tan

class CubeRenderer: GLSurfaceView.Renderer {

    private val mCube = Cube()
    private var mTransY = 0.0f
    private var mAngle = 0.0f

    override fun onDrawFrame(gl: GL10) {

        val z = -7.0f

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl.glClearColor(0.0f, 0.5f, 0.5f, 1.0f)

        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glLoadIdentity()

        gl.glTranslatef(0.0f, sin(mTransY) / 2.0f, z)

        gl.glRotatef(mAngle, 0.0f, 1.0f, 0.0f)
        gl.glRotatef(mAngle, 1.0f, 0.0f, 0.0f)

        //gl.glScalef(1.0f, 2.0f, 1.0f)

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY)

        mCube.draw(gl)

        mTransY += 0.075f
        mAngle += 0.4f

    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {

        gl.glViewport(0, 0, width, height)

        val zNear = 0.1f
        val zFar = 1000.0f
        val fieldOfView = 30.0f / 57.3f

        gl.glEnable(GL10.GL_NORMALIZE)

        val aspectRatio = width.toFloat() / height.toFloat()

        gl.glMatrixMode(GL10.GL_PROJECTION)

        val size = zNear * tan(fieldOfView / 2.0f)

        gl.glFrustumf(-size, size, -size / aspectRatio,
            size / aspectRatio, zNear, zFar)

        gl.glMatrixMode(GL10.GL_MODELVIEW)

    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {

        // Turning off any dithering. Dithering default is ON because it makes limited palettes
        // look nicer but at the expense of performance
        gl.glDisable(GL10.GL_DITHER)

        // Nudge OpenGL ES to do what it thinks best by accepting certain trade offs
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)

        // "de-select" faces that are aimed away from us
        gl.glEnable(GL10.GL_CULL_FACE)
        //gl.glCullFace(GL10.GL_FRONT) // to delete front faces and only show back faces

        // Smooth colors so the colors blend along the surface
        gl.glShadeModel(GL10.GL_SMOOTH)

        // z-Buffering
        gl.glEnable(GL10.GL_DEPTH_TEST)

    }

}