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

    
    
    val Search: ImageVector by lazy {
        ImageVector.Builder(name = "Search", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.5f, 14f); horizontalLineToRelative(-0.79f); lineToRelative(-0.28f, -0.27f)
                curveTo(15.41f, 12.59f, 16f, 11.11f, 16f, 9.5f); curveTo(16f, 5.91f, 13.09f, 3f, 9.5f, 3f)
                reflectiveCurveTo(3f, 5.91f, 3f, 9.5f); reflectiveCurveTo(5.91f, 16f, 9.5f, 16f)
                curveToRelative(1.61f, 0f, 3.09f, -0.59f, 4.23f, -1.57f); lineToRelative(0.27f, 0.28f); verticalLineToRelative(0.79f)
                lineToRelative(5f, 4.99f); lineTo(20.49f, 19f); lineToRelative(-4.99f, -5f); close()
                moveTo(9.5f, 14f); curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f); reflectiveCurveTo(7.01f, 5f, 9.5f, 5f)
                reflectiveCurveTo(14f, 7.01f, 14f, 9.5f); reflectiveCurveTo(11.99f, 14f, 9.5f, 14f); close()
            }
        }.build()
    }

    val Filter: ImageVector by lazy {
        ImageVector.Builder(name = "Filter", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 18f); horizontalLineToRelative(4f); verticalLineToRelative(-2f); horizontalLineToRelative(-4f); verticalLineToRelative(2f); close()
                moveTo(3f, 6f); verticalLineToRelative(2f); horizontalLineToRelative(18f); verticalLineTo(6f); horizontalLineTo(3f); close()
                moveTo(6f, 13f); horizontalLineToRelative(12f); verticalLineToRelative(-2f); horizontalLineTo(6f); verticalLineToRelative(2f); close()
            }
        }.build()
    }

    val Folder: ImageVector by lazy {
        ImageVector.Builder(name = "Folder", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 4f); horizontalLineTo(4f); curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f); lineTo(2f, 18f)
                curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f); horizontalLineToRelative(16f); curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                verticalLineTo(8f); curveTo(22f, 6.9f, 21.1f, 6f, 20f, 6f); horizontalLineToRelative(-8f); lineToRelative(-2f, -2f); close()
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
