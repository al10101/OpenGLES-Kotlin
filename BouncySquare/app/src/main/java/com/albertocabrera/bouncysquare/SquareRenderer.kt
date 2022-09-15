package com.albertocabrera.bouncysquare

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11
import kotlin.math.sin

private const val TAG = "SquareRenderer"

class SquareRenderer(useTranslucentBackground: Boolean,
                     private val context: Context): GLSurfaceView.Renderer {

    private val mTranslucentBackground = useTranslucentBackground
    private val mSquare = Square()
    private var mTransY = 0.0f
    private var mAngle = 0.0f

    // Root refresh method. This constructs the image each time through, many times a second
    override fun onDrawFrame(gl: GL10) {

        // Clear the screen. The color buffer holds all of the RGBA color data, while
        // the depth buffer is used to ensure that the closer items properly obscure the further
        // items
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)

        // Geometry params
        gl.glMatrixMode(GL10.GL_MODELVIEW)
        gl.glEnableClientState(GL11.GL_VERTEX_ARRAY)

        // Turn blending on
        gl.glEnable(GL10.GL_BLEND)
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)

        // Square 1
        gl.glLoadIdentity()
        // Translate the box up and down. To get a smooth motion, the actual translation value
        // is based on a sine wave.
        gl.glTranslatef(0.0f, sin(mTransY), -3.0f)
        gl.glColor4f(0.0f, 0.0f, 1.0f, 0.5f)
        // Actual drawing routine
        mSquare.draw(gl)

        // Square 2
        gl.glLoadIdentity()
        gl.glTranslatef(sin(mTransY)*0.5f, 0.0f, -2.9f)
        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.5f)
        mSquare.draw(gl)


        mTransY += 0.075f // 0.075 or 0.3f

    }

    // Called whenever the screen changes size or is created at startup
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {

        // Dimensions and placement of your OpenGL window. Typically, the size of the screen with
        // a location 0
        gl.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        // Set up the matrix that will be called when making any general-purpose matrix
        // management calls. GL_PROJECTION projects the 3D scene to the 2D screen
        gl.glMatrixMode(GL10.GL_PROJECTION)

        // Resets value of matrix to its initial values
        gl.glLoadIdentity()

        // The frustum is the volume of space that defines what you can actually see
        gl.glFrustumf(-ratio, ratio, -1.0f, 1.0f, 1.0f, 10.0f)

    }

    // Upon surface creation
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {

        // Turning off any dithering. Dithering default is ON because it makes limited palettes
        // look nicer but at the expense of performance
        gl.glDisable(GL10.GL_DITHER)

        // Nudge OpenGL ES to do what it thinks best by accepting certain trade offs
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST)

        /*if (mTranslucentBackground) {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        } else {
            gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        }*/

        // "de-select" faces that are aimed away from us
        gl.glEnable(GL10.GL_CULL_FACE)

        // Smooth colors so the colors blend along the surface
        gl.glShadeModel(GL10.GL_SMOOTH)

        // z-Buffering
        gl.glEnable(GL10.GL_DEPTH_TEST)

        val resid = R.drawable.frame_3
        mSquare.createTexture(gl, this.context, resid)

    }

}