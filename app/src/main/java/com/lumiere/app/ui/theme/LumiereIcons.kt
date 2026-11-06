package com.lumiere.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object LumiereIcons {
    val Copy: ImageVector by lazy {
        ImageVector.Builder(name = "Copy", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 1f); lineTo(4f, 1f); curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
                verticalLineToRelative(14f); horizontalLineToRelative(2f); verticalLineTo(3f); horizontalLineToRelative(12f); verticalLineTo(1f)
                close()
                moveTo(19f, 5f); lineTo(8f, 5f); curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
                verticalLineToRelative(14f); curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f); horizontalLineToRelative(11f)
                curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f); verticalLineTo(7f); curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
                close()
                moveTo(19f, 21f); horizontalLineTo(8f); verticalLineTo(7f); horizontalLineToRelative(11f); verticalLineToRelative(14f); close()
            }
        }.build()
    }

    val Archive: ImageVector by lazy {
        ImageVector.Builder(name = "Archive", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.54f, 5.23f); lineTo(19.15f, 3.55f); curveTo(18.88f, 3.21f, 18.47f, 3f, 18f, 3f)
                horizontalLineTo(6f); curveTo(5.53f, 3f, 5.12f, 3.21f, 4.84f, 3.55f); lineTo(3.46f, 5.23f)
                curveTo(3.17f, 5.57f, 3f, 6.02f, 3f, 6.5f); verticalLineTo(19f); curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
                horizontalLineToRelative(14f); curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f); verticalLineTo(6.5f)
                curveTo(21f, 6.02f, 20.83f, 5.57f, 20.54f, 5.23f); close()
                moveTo(12f, 17.5f); lineTo(6.5f, 12f); horizontalLineTo(10f); verticalLineToRelative(-2f)
                horizontalLineToRelative(4f); verticalLineToRelative(2f); horizontalLineToRelative(3.5f); lineTo(12f, 17.5f); close()
                moveTo(5.12f, 5f); lineTo(5.93f, 4f); horizontalLineToRelative(12.14f); lineToRelative(0.81f, 1f); horizontalLineTo(5.12f); close()
            }
        }.build()
    }

    val Edit: ImageVector by lazy {
        ImageVector.Builder(name = "Edit", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 17.25f); verticalLineTo(21f); horizontalLineToRelative(3.75f); lineTo(17.81f, 9.94f); lineTo(14.06f, 6.19f); lineTo(3f, 17.25f)
                close()
                moveTo(20.71f, 7.04f); curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f); lineTo(18.37f, 3.29f)
                curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f); lineTo(15.13f, 5.12f); lineTo(18.88f, 8.87f); lineTo(20.71f, 7.04f); close()
            }
        }.build()
    }

    val Delete: ImageVector by lazy {
        ImageVector.Builder(name = "Delete", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 19f); curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f); horizontalLineToRelative(8f); curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f)
                verticalLineTo(7f); horizontalLineTo(6f); verticalLineToRelative(12f); close()
                moveTo(19f, 4f); horizontalLineToRelative(-3.5f); lineToRelative(-1f, -1f); horizontalLineToRelative(-5f); lineToRelative(-1f, 1f)
                horizontalLineTo(5f); verticalLineToRelative(2f); horizontalLineToRelative(14f); verticalLineTo(4f); close()
            }
        }.build()
    }
}
