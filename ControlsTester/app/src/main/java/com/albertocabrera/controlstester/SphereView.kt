package com.albertocabrera.controlstester

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import com.albertocabrera.controlstester.utils.VIEW_TAG

private const val NONE = 0
private const val DRAG = 1
private const val ZOOM = 2

private const val TAG = VIEW_TAG

class SphereView(context: Context): GLSurfaceView(context) {

    private var gesture = NONE
    private val sphereRenderer = SphereRenderer(context)

    init {
        setEGLContextClientVersion(2)
        setRenderer(sphereRenderer)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {

        // Variable to save if the event has been consumed. Only false when ev == null or
        // the zoom cannot be made
        var consumed = true

        val normalizedX = (ev.x / width) * 2f - 1f
        val normalizedY = -((ev.y / height) * 2f - 1f)
        Log.i(TAG, "NormalizedX= %.4f  NormalizedY= %.4f".format(normalizedX, normalizedY))

        when (ev.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {
                gesture = DRAG
                queueEvent {
                    sphereRenderer.handleTouchPress(normalizedX, normalizedY)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                gesture = ZOOM
                queueEvent {
                    sphereRenderer.handleZoomPress(ev)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (gesture == DRAG) {
                    queueEvent {
                        sphereRenderer.handleTouchDragToRotate(normalizedX, normalizedY)
                    }
                } else if (gesture == ZOOM) {
                    queueEvent {
                        consumed = sphereRenderer.handleZoomCamera(ev)
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                gesture = NONE
                queueEvent {
                    sphereRenderer.stopMovement(ev)
                }
            }

        }

        return consumed

    }

}