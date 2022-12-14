package com.albertocabrera.controlstester.utils

import kotlin.math.PI

const val SHADER_TAG = "ShaderTag"
const val VIEW_TAG = "ViewTag"
const val RENDERER_TAG = "RendererTag"

const val U_PROJECTION_MATRIX = "u_ProjectionMatrix"
const val U_VIEW_MATRIX = "u_ViewMatrix"
const val U_MODEL_MATRIX = "u_ModelMatrix"
const val A_POSITION = "a_Position"
const val A_COLOR = "a_Color"

const val PI_F = PI.toFloat()
const val BYTES_PER_FLOAT = 4

const val POSITION_COUNT_3D = 3
const val POSITION_COUNT_2D = 2
const val COLOR_COUNT = 4

const val RADIANS_TO_DEGREES = 57.295779513f

const val POSITION_STRIDE_3D = POSITION_COUNT_3D * BYTES_PER_FLOAT
const val COLOR_STRIDE = COLOR_COUNT * BYTES_PER_FLOAT

val RED = floatArrayOf(1f, 0f, 0f)
val GREEN = floatArrayOf(0f, 1f, 0f)
val BLUE = floatArrayOf(0f, 0f, 1f)
val WHITE = floatArrayOf(1f, 1f, 1f)