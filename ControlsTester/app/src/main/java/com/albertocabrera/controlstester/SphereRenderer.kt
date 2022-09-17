package com.albertocabrera.controlstester

import android.content.Context
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import com.albertocabrera.controlstester.utils.*
import com.albertocabrera.controlstester.utils.Geometry.crossProduct
import com.albertocabrera.controlstester.utils.Geometry.intersects
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import com.albertocabrera.controlstester.utils.Geometry.vectorBetween
import kotlin.math.acos
import kotlin.math.sqrt

private const val TAG = RENDERER_TAG

class SphereRenderer(private val context: Context): GLSurfaceView.Renderer {

    // Controls
    var oldDist = 0f
    var midTouch = Geometry.Point(0f, 0f, 0f)
    var startFov = 0f
    var currentFov = 0f
    private var fov = 0f
    private var theta = 0f
    private var ux = 1f
    private var uy = 1f
    private var uz = 1f
    private val temp1 = FloatArray(16)
    private var spherePressed = false
    private var spherePosition = Geometry.Point(0f, 0f, 0f)
    private var previousTouch = Geometry.Point(0f, 0f, 0f)
    private var currentTouch = Geometry.Point(0f, 0f, 0f)

    // Renderer variables
    private val bgColor = WHITE
    private var origWidth = 0
    private var origHeight = 0

    // Uniform matrices
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val invertedViewProjectionMatrix = FloatArray(16)

    // Models to render
    private var maxNorm = 0f
    private lateinit var sphere: Sphere
    private lateinit var boundary: Sphere

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {

        // Turning off any dithering
        glDisable(GL_DITHER)

        // "De-select" faces that are aimed away from us
        glEnable(GL_CULL_FACE)
        glCullFace(GL_FRONT)

        // z-Buffering
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)

        // Enable alpha blending
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Set background color
        glClearColor(bgColor[0], bgColor[1], bgColor[2], 1f)

        // Set up camera
        maxNorm = 5f
        Matrix.setLookAtM(viewMatrix, 0, 0f,0f, maxNorm,
            0f, 0f, 0f, 0f, 1f, 0f)
        maxNorm *= 0.1f

        // Create and add spheres
        sphere = Sphere(context, 15, 1f, RED, 1f)
        spherePosition = Geometry.Point(0f, 0f, 0f)

        // Define boundary
        boundary = Sphere(context, 50, 1.3f, BLUE, 0.3f)

    }

    private fun setClipping(width: Int, height: Int) {
        // Use a perspective projection
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, fov, ratio, 0.01f, 50f)
        // Once the projection is defined, get view-projection and invert
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        origWidth = width
        origHeight = height
        fov = 45f
        setClipping(width, height)
    }

    override fun onDrawFrame(p0: GL10?) {

        // Clear the rendering surface
        glEnable(GL_DEPTH_TEST)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Update movement
        Matrix.setIdentityM(temp1, 0)
        Matrix.rotateM(temp1, 0, theta, ux, uy, uz)

        // Move and draw
        Matrix.multiplyMM(modelMatrix, 0, temp1, 0, sphere.modelMatrix, 0)
        sphere.useProgram()
        sphere.fragmentUniforms(projectionMatrix, viewMatrix, modelMatrix)
        sphere.bindData()
        sphere.drawPrimitives(modelMatrix)

        if (spherePressed) {
            boundary.useProgram()
            boundary.fragmentUniforms(projectionMatrix, viewMatrix, modelMatrix)
            boundary.bindData()
            boundary.drawPrimitives(modelMatrix)
        }

    }

    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {

        val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)

        // Now test if this ray intersects with the mallet by creating a bounding sphere that
        // wraps the mallet.
        val atomBoundingSphere = Geometry.Sphere(Geometry.Point(spherePosition.x, spherePosition.y, spherePosition.z), sphere.radius)

        // If the ray intersects (if the user touched a part of the screen that intersects the
        // mallet's bounding sphere), then set malletPressed = true
        spherePressed = intersects(atomBoundingSphere, ray)

        // If pressed, select. Else, move
        if (spherePressed) {
            Log.i(TAG, "SpherePressed!")
        }

        // Define a plane representing the background
        val plane = Geometry.Plane(Geometry.Point(0f, 0f, maxNorm), Geometry.Vector(0f, maxNorm, maxNorm))
        // Find out where the touched point intersects the plane
        val touchedPoint = Geometry.intersectionPoint(ray, plane)
        previousTouch = Geometry.Point(touchedPoint.x, touchedPoint.y, maxNorm)
        Log.i(TAG, "PREV VECTOR: X= %.4f  Y= %.4f  Z= %.4f".format(previousTouch.x, previousTouch.y, previousTouch.z))

    }

    fun handleTouchDragToRotate(normalizedX: Float, normalizedY: Float) {
        val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        // Define a plane representing the background
        val plane = Geometry.Plane(Geometry.Point(0f, 0f, maxNorm), Geometry.Vector(0f, maxNorm, maxNorm))
        // Find out where the touched point intersects the plane
        val touchedPoint = Geometry.intersectionPoint(ray, plane)
        // Define new vector
        currentTouch = Geometry.Point(touchedPoint.x, touchedPoint.y, maxNorm)
        computeRotation()
        Log.i(TAG, "NEW VECTOR : X= %.4f  Y= %.4f  Z= %.4f".format(currentTouch.x, currentTouch.y, currentTouch.z))
        Log.i(TAG, "UX= %.4f  UY= %.4f  UZ= %.4f  THETA= %.4f".format(ux, uy, uz, theta))
    }

    private fun computeRotation() {
        val cross = crossProduct(previousTouch, currentTouch)
        val norm = cross.length()
        val unitary = cross.scale(1f/norm)
        val num = previousTouch.dotProduct(currentTouch)
        val den = previousTouch.length() * currentTouch.length()
        ux = unitary.x
        uy = unitary.y
        uz = unitary.z
        theta = acos(num / den ) * RADIANS_TO_DEGREES
    }

    private fun spacing(ev: MotionEvent): Float {
        // Sometimes, the pointer 1 is not saved correctly
        if (ev.pointerCount == 1) { return 1f }
        val x = ev.getX(0) - ev.getX(1)
        val y = ev.getY(0) - ev.getY(1)
        return sqrt(x * x + y * y)
    }

    fun handleZoomPress(ev: MotionEvent) {
        oldDist = spacing(ev)
        midTouch.x = (ev.getX(0) + ev.getX(1)) * 0.5f
        midTouch.y = (ev.getY(0) + ev.getY(1)) * 0.5f
        startFov = fov
        previousTouch.x = midTouch.x
        previousTouch.y = midTouch.y
        currentTouch.x = midTouch.x
        currentTouch.y = midTouch.y
    }

    fun handleZoomCamera(ev: MotionEvent): Boolean {
        val minFov = 5f
        val maxFov = 120f

        val newDist = spacing(ev)
        val zoom = oldDist / newDist

        currentFov = startFov * zoom
        previousTouch.x = midTouch.x
        previousTouch.y = midTouch.y

        return if (currentFov in minFov..maxFov) {
            fov = currentFov
            setClipping(origWidth, origHeight)
            Log.i(TAG, "FOV= $fov")
            true
        } else {
            Log.i(TAG, "NOT FOV!!!!")
            false
        }

    }

    fun handleTouchDragToMove(normalizedX: Float, normalizedY: Float) {

        if (spherePressed) {
            val ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
            // Define a plane representing the background
            val plane = Geometry.Plane(Geometry.Point(0f, 0f, maxNorm), Geometry.Vector(0f, maxNorm, maxNorm))
            // Find out where the touched point intersects the plane, move along this plane
            val touchedPoint = Geometry.intersectionPoint(ray, plane)
            spherePosition = Geometry.Point(touchedPoint.x, touchedPoint.y, 0f)
        }

    }

    fun stopMovement(ev: MotionEvent) {
        Matrix.setIdentityM(temp1, 0)
        Matrix.multiplyMM(sphere.modelMatrix, 0, temp1, 0, modelMatrix, 0)
        previousTouch.x = ev.x
        previousTouch.y = ev.y
        theta = 0f
    }

    private fun divideByW(vector: FloatArray) {
        vector[0] /= vector[3]
        vector[1] /= vector[3]
        vector[2] /= vector[3]
    }

    private fun convertNormalized2DPointToRay(normalizedX: Float, normalizedY: Float): Geometry.Ray {

        val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)

        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)

        Matrix.multiplyMV(nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0)
        Matrix.multiplyMV(farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0)

        divideByW(nearPointWorld)
        divideByW(farPointWorld)

        val nearPointRay = Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])
        val farPointRay = Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2])

        return Geometry.Ray(nearPointRay, vectorBetween(nearPointRay, farPointRay))

    }

}