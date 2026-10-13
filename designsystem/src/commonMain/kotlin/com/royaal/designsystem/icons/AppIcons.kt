package com.royaal.designsystem.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {

    @Suppress("CheckReturnValue")
    val add: ImageVector
        get() {
            if (_add != null) {
                return _add!!
            }
            _add =
                ImageVector.Builder(
                    name = "add",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.Companion.NonZero,
                        ) {
                            moveTo(11f, 13f)
                            horizontalLineTo(6f)
                            quadTo(5.58f, 13f, 5.29f, 12.71f)
                            quadTo(5f, 12.43f, 5f, 12f)
                            reflectiveQuadTo(5.29f, 11.29f)
                            reflectiveQuadTo(6f, 11f)
                            horizontalLineToRelative(5f)
                            verticalLineTo(6f)
                            quadTo(11f, 5.57f, 11.29f, 5.29f)
                            reflectiveQuadTo(12f, 5f)
                            reflectiveQuadToRelative(0.71f, 0.29f)
                            reflectiveQuadTo(13f, 6f)
                            verticalLineToRelative(5f)
                            horizontalLineToRelative(5f)
                            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                            reflectiveQuadTo(19f, 12f)
                            reflectiveQuadToRelative(-0.29f, 0.71f)
                            reflectiveQuadTo(18f, 13f)
                            horizontalLineTo(13f)
                            verticalLineToRelative(5f)
                            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                            reflectiveQuadTo(12f, 19f)
                            reflectiveQuadTo(11.29f, 18.71f)
                            quadTo(11f, 18.43f, 11f, 18f)
                            verticalLineTo(13f)
                            close()
                        }
                    }
                    .build()
            return _add!!
        }

    private var _add: ImageVector? = null

    @Suppress("CheckReturnValue")
    val close: ImageVector
        get() {
            if (_close != null) {
                return _close!!
            }
            _close =
                ImageVector.Builder(
                    name = "close",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.Companion.NonZero,
                        ) {
                            moveTo(12f, 12.99f)
                            lineTo(6.96f, 18.03f)
                            quadTo(6.75f, 18.24f, 6.47f, 18.24f)
                            reflectiveQuadTo(5.97f, 18.03f)
                            quadTo(5.76f, 17.81f, 5.77f, 17.54f)
                            reflectiveQuadTo(5.97f, 17.05f)
                            lineTo(11.01f, 12f)
                            lineTo(5.97f, 6.95f)
                            quadTo(5.78f, 6.75f, 5.77f, 6.47f)
                            reflectiveQuadToRelative(0.2f, -0.5f)
                            reflectiveQuadTo(6.46f, 5.76f)
                            reflectiveQuadToRelative(0.5f, 0.21f)
                            lineTo(12f, 11.01f)
                            lineTo(17.05f, 5.96f)
                            quadToRelative(0.2f, -0.2f, 0.49f, -0.2f)
                            reflectiveQuadToRelative(0.5f, 0.21f)
                            quadToRelative(0.2f, 0.21f, 0.2f, 0.49f)
                            reflectiveQuadTo(18.03f, 6.95f)
                            lineTo(12.99f, 12f)
                            lineToRelative(5.03f, 5.05f)
                            quadToRelative(0.2f, 0.2f, 0.2f, 0.48f)
                            reflectiveQuadToRelative(-0.2f, 0.5f)
                            reflectiveQuadToRelative(-0.49f, 0.22f)
                            reflectiveQuadTo(17.05f, 18.03f)
                            lineTo(12f, 12.99f)
                            close()
                        }
                    }
                    .build()
            return _close!!
        }

    private var _close: ImageVector? = null

    @Suppress("CheckReturnValue")
    val search: ImageVector
        get() {
            if (_search != null) {
                return _search!!
            }
            _search =
                ImageVector.Builder(
                    name = "search",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.Companion.NonZero,
                        ) {
                            moveTo(9.56f, 15.49f)
                            quadToRelative(-2.51f, 0f, -4.24f, -1.74f)
                            reflectiveQuadTo(3.58f, 9.52f)
                            reflectiveQuadTo(5.31f, 5.28f)
                            reflectiveQuadTo(9.55f, 3.55f)
                            reflectiveQuadToRelative(4.24f, 1.74f)
                            reflectiveQuadToRelative(1.74f, 4.24f)
                            quadToRelative(0f, 1.05f, -0.35f, 2.02f)
                            reflectiveQuadToRelative(-0.94f, 1.68f)
                            lineToRelative(5.71f, 5.7f)
                            quadToRelative(0.2f, 0.2f, 0.2f, 0.49f)
                            reflectiveQuadToRelative(-0.21f, 0.5f)
                            reflectiveQuadToRelative(-0.5f, 0.21f)
                            reflectiveQuadTo(18.95f, 19.91f)
                            lineTo(13.25f, 14.2f)
                            quadToRelative(-0.74f, 0.62f, -1.71f, 0.96f)
                            reflectiveQuadTo(9.56f, 15.49f)
                            close()
                            moveToRelative(-0f, -1.4f)
                            quadToRelative(1.92f, 0f, 3.25f, -1.33f)
                            reflectiveQuadTo(14.13f, 9.52f)
                            reflectiveQuadTo(12.8f, 6.27f)
                            reflectiveQuadTo(9.55f, 4.95f)
                            reflectiveQuadTo(6.3f, 6.27f)
                            reflectiveQuadTo(4.98f, 9.52f)
                            reflectiveQuadTo(6.3f, 12.77f)
                            reflectiveQuadTo(9.55f, 14.1f)
                            close()
                        }
                    }
                    .build()
            return _search!!
        }

    private var _search: ImageVector? = null

    @Suppress("CheckReturnValue")
    val check: ImageVector
        get() {
            if (_check != null) {
                return _check!!
            }
            _check =
                ImageVector.Builder(
                    name = "check",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.Companion.NonZero,
                        ) {
                            moveTo(9.58f, 14.69f)
                            lineTo(17.7f, 6.57f)
                            quadTo(18.08f, 6.19f, 18.6f, 6.19f)
                            reflectiveQuadToRelative(0.9f, 0.39f)
                            reflectiveQuadToRelative(0.39f, 0.9f)
                            reflectiveQuadTo(19.5f, 8.38f)
                            lineTo(10.48f, 17.4f)
                            quadToRelative(-0.39f, 0.39f, -0.9f, 0.39f)
                            reflectiveQuadTo(8.67f, 17.4f)
                            lineTo(4.5f, 13.23f)
                            quadTo(4.11f, 12.84f, 4.11f, 12.33f)
                            reflectiveQuadTo(4.5f, 11.42f)
                            reflectiveQuadTo(5.4f, 11.04f)
                            reflectiveQuadToRelative(0.9f, 0.39f)
                            lineToRelative(3.27f, 3.27f)
                            close()
                        }
                    }
                    .build()
            return _check!!
        }

    private var _check: ImageVector? = null

    val favorite: ImageVector
        get() {
            if (_favorite != null) {
                return _favorite!!
            }
            _favorite =
                ImageVector.Builder(
                    name = "favorite",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.Companion.NonZero,
                        ) {
                            moveTo(11.38f, 19.41f)
                            quadTo(11.08f, 19.3f, 10.85f, 19.07f)
                            lineTo(9.53f, 17.87f)
                            quadTo(6.83f, 15.42f, 4.76f, 13.1f)
                            reflectiveQuadTo(2.7f, 8.17f)
                            quadTo(2.7f, 6.1f, 4.1f, 4.71f)
                            reflectiveQuadTo(7.56f, 3.31f)
                            quadToRelative(1.17f, 0f, 2.34f, 0.55f)
                            reflectiveQuadTo(12f, 5.7f)
                            quadTo(12.95f, 4.42f, 14.11f, 3.87f)
                            reflectiveQuadTo(16.44f, 3.31f)
                            quadToRelative(2.07f, 0f, 3.46f, 1.39f)
                            reflectiveQuadToRelative(1.4f, 3.46f)
                            quadToRelative(0f, 2.64f, -2.1f, 4.99f)
                            reflectiveQuadToRelative(-4.73f, 4.72f)
                            lineToRelative(-1.32f, 1.2f)
                            quadTo(12.91f, 19.3f, 12.6f, 19.41f)
                            reflectiveQuadToRelative(-0.61f, 0.11f)
                            reflectiveQuadTo(11.38f, 19.41f)
                            close()
                            moveTo(11.33f, 7.16f)
                            quadTo(10.58f, 5.91f, 9.65f, 5.31f)
                            reflectiveQuadTo(7.56f, 4.71f)
                            quadToRelative(-1.48f, 0f, -2.47f, 0.98f)
                            reflectiveQuadTo(4.1f, 8.17f)
                            quadToRelative(0f, 1.19f, 0.77f, 2.49f)
                            reflectiveQuadToRelative(1.92f, 2.59f)
                            reflectiveQuadToRelative(2.5f, 2.51f)
                            reflectiveQuadToRelative(2.51f, 2.28f)
                            quadToRelative(0.09f, 0.08f, 0.2f, 0.08f)
                            reflectiveQuadToRelative(0.2f, -0.08f)
                            quadToRelative(1.17f, -1.05f, 2.51f, -2.28f)
                            reflectiveQuadToRelative(2.5f, -2.51f)
                            reflectiveQuadToRelative(1.92f, -2.59f)
                            reflectiveQuadTo(19.9f, 8.17f)
                            quadToRelative(0f, -1.49f, -0.99f, -2.47f)
                            reflectiveQuadTo(16.44f, 4.71f)
                            quadToRelative(-1.16f, 0f, -2.1f, 0.6f)
                            reflectiveQuadTo(12.66f, 7.16f)
                            quadToRelative(-0.12f, 0.19f, -0.3f, 0.28f)
                            reflectiveQuadTo(12f, 7.53f)
                            reflectiveQuadTo(11.62f, 7.44f)
                            reflectiveQuadTo(11.33f, 7.16f)
                            close()
                            moveTo(12f, 11.42f)
                            close()
                        }
                    }
                    .build()
            return _favorite!!
        }
    private var _favorite: ImageVector? = null

    val home: ImageVector
        get() {
            if (_home != null) {
                return _home!!
            }
            _home =
                ImageVector.Builder(
                    name = "home",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                    .apply {
                        path(
                            fill = SolidColor(Color.Black),
                            fillAlpha = 1f,
                            stroke = null,
                            strokeAlpha = 1f,
                            strokeLineWidth = 1f,
                            strokeLineCap = StrokeCap.Butt,
                            strokeLineJoin = StrokeJoin.Bevel,
                            strokeLineMiter = 1f,
                            pathFillType = PathFillType.Companion.NonZero,
                        ) {
                            moveTo(6f, 19f)
                            lineTo(9f, 19f)
                            lineTo(9f, 13f)
                            lineTo(15f, 13f)
                            lineTo(15f, 19f)
                            lineTo(18f, 19f)
                            lineTo(18f, 10f)
                            lineTo(12f, 5.5f)
                            lineTo(6f, 10f)
                            close()
                            moveTo(4f, 21f)
                            lineTo(4f, 9f)
                            lineTo(12f, 3f)
                            lineTo(20f, 9f)
                            lineTo(20f, 21f)
                            lineTo(13f, 21f)
                            lineTo(13f, 15f)
                            lineTo(11f, 15f)
                            lineTo(11f, 21f)
                            close()
                        }
                    }
                    .build()
            return _home!!
        }
    private var _home: ImageVector? = null

    // Material Icons, as path data.

    val arrowBack: ImageVector by lazy {
        materialIcon("arrowBack", "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z")
    }

    val pin: ImageVector by lazy {
        materialIcon(
            "pin",
            "M16,9V4h1c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1H7C6.45,2 6,2.45 6,3s0.45,1 1,1h1v5" +
                "c0,1.66 -1.34,3 -3,3v2h5.97v7l1,1 1,-1v-7H19v-2c-1.66,0 -3,-1.34 -3,-3z",
        )
    }

    val delete: ImageVector by lazy {
        materialIcon(
            "delete",
            "M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM8,9h8v10H8V9zm7.5,-5l-1,-1h-5l-1,1H5v2h14V4z",
        )
    }

    val collections: ImageVector by lazy {
        materialIcon(
            "collections",
            "M9.17,6l2,2H20v10H4V6h5.17M10,4H4c-1.1,0 -1.99,0.9 -1.99,2L2,18c0,1.1 0.9,2 2,2h16" +
                "c1.1,0 2,-0.9 2,-2V8c0,-1.1 -0.9,-2 -2,-2h-8l-2,-2z",
        )
    }

    private fun materialIcon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
            .addPath(pathData = addPathNodes(pathData), fill = SolidColor(Color.Black))
            .build()
}
