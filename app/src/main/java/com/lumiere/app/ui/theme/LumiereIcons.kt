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

    val Tag: ImageVector by lazy { ImageVector.Builder("Tag", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(21.41f, 11.58f); lineToRelative(-9f, -9f); curveTo(12.05f, 2.22f, 11.55f, 2f, 11f, 2f); horizontalLineTo(4f); curveTo(2.9f, 2f, 2.01f, 2.9f, 2.01f, 4f); lineTo(2f, 11f); curveTo(2f, 11.55f, 2.22f, 12.05f, 2.58f, 12.41f); lineToRelative(9f, 9f); curveTo(11.95f, 21.78f, 12.45f, 22f, 13f, 22f); reflectiveCurveToRelative(1.05f, -0.22f, 1.41f, -0.58f); lineToRelative(7f, -7f); curveTo(21.78f, 14.05f, 22f, 13.55f, 22f, 13f); reflectiveCurveTo(21.78f, 11.95f, 21.41f, 11.58f); close(); moveTo(6.5f, 8f); curveTo(5.67f, 8f, 5f, 7.33f, 5f, 6.5f); reflectiveCurveTo(5.67f, 5f, 6.5f, 5f); reflectiveCurveTo(8f, 5.67f, 8f, 6.5f); reflectiveCurveTo(7.33f, 8f, 6.5f, 8f); close() } }.build() }
    val Work: ImageVector by lazy { ImageVector.Builder("Work", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(20f, 6f); horizontalLineToRelative(-4f); verticalLineTo(4f); curveTo(20f, 2.9f, 19.1f, 2f, 18f, 2f); horizontalLineToRelative(-4f); curveTo(12.9f, 2f, 12f, 2.9f, 12f, 4f); verticalLineToRelative(2f); horizontalLineTo(4f); curveTo(2.9f, 6f, 2.01f, 6.9f, 2.01f, 8f); lineTo(2f, 20f); curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f); horizontalLineToRelative(16f); curveTo(21.1f, 22f, 22f, 21.1f, 22f, 20f); verticalLineTo(8f); curveTo(22f, 6.9f, 21.1f, 6f, 20f, 6f); close(); moveTo(14f, 6f); horizontalLineToRelative(-4f); verticalLineTo(4f); horizontalLineToRelative(4f); verticalLineToRelative(2f); close() } }.build() }
    val School: ImageVector by lazy { ImageVector.Builder("School", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(5f, 13.18f); verticalLineToRelative(3.64f); lineTo(12f, 21f); lineTo(19f, 16.82f); verticalLineToRelative(-3.64f); lineTo(12f, 17.5f); lineTo(5f, 13.18f); close(); moveTo(12f, 3f); lineTo(1f, 9f); lineTo(12f, 15f); lineTo(21f, 10.09f); verticalLineTo(17f); horizontalLineToRelative(2f); verticalLineTo(9f); lineTo(12f, 3f); close() } }.build() }
    val Favorite: ImageVector by lazy { ImageVector.Builder("Favorite", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(12f, 21.35f); lineTo(10.55f, 20.03f); curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f); curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f); curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f); curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f); curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f); curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f); lineTo(12f, 21.35f); close() } }.build() }
    val Money: ImageVector by lazy { ImageVector.Builder("Money", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(11.8f, 10.9f); curveTo(9.53f, 10.31f, 8.8f, 9.7f, 8.8f, 8.71f); curveTo(8.8f, 7.56f, 9.95f, 6.8f, 11.9f, 6.8f); curveTo(13.95f, 6.8f, 14.8f, 7.65f, 14.9f, 9f); horizontalLineToRelative(2.7f); curveTo(15.71f, 6.51f, 14.06f, 5f, 11.9f, 5f); curveTo(9.9f, 5f, 8.3f, 6.2f, 8.3f, 8.8f); curveTo(8.3f, 10.88f, 10f, 11.9f, 13.1f, 12.6f); curveTo(15.9f, 13.2f, 16f, 14.18f, 16f, 14.9f); curveTo(16f, 16.3f, 14.7f, 16.9f, 12.9f, 16.9f); curveTo(10.6f, 16.9f, 9.6f, 15.8f, 9.5f, 14.5f); horizontalLineTo(6.8f); curveTo(6.9f, 17f, 8.7f, 18.2f, 10.9f, 18.6f); verticalLineTo(21f); horizontalLineToRelative(2f); verticalLineTo(18.6f); curveTo(15f, 18.2f, 16.8f, 17.1f, 16.8f, 14.9f); curveTo(16.8f, 12.9f, 15.1f, 11.9f, 11.8f, 10.9f); close() } }.build() }
    val Home: ImageVector by lazy { ImageVector.Builder("Home", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(10f, 20f); verticalLineToRelative(-6f); horizontalLineToRelative(4f); verticalLineToRelative(6f); horizontalLineToRelative(5f); verticalLineTo(12f); horizontalLineToRelative(3f); lineTo(12f, 3f); lineTo(2f, 12f); horizontalLineToRelative(3f); verticalLineTo(20f); close() } }.build() }
    val Person: ImageVector by lazy { ImageVector.Builder("Person", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(12f, 12f); curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f); reflectiveCurveTo(14.21f, 4f, 12f, 4f); reflectiveCurveTo(8f, 5.79f, 8f, 8f); reflectiveCurveTo(9.79f, 12f, 12f, 12f); close(); moveTo(12f, 14f); curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f); verticalLineToRelative(2f); horizontalLineToRelative(16f); verticalLineToRelative(-2f); curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f); close() } }.build() }
    val ShoppingCart: ImageVector by lazy { ImageVector.Builder("ShoppingCart", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(7f, 18f); curveTo(5.9f, 18f, 5.01f, 18.9f, 5.01f, 20f); curveTo(5.01f, 21.1f, 5.9f, 22f, 7f, 22f); curveTo(8.1f, 22f, 9f, 21.1f, 9f, 20f); curveTo(9f, 18.9f, 8.1f, 18f, 7f, 18f); close(); moveTo(1f, 2f); verticalLineToRelative(2f); horizontalLineToRelative(2f); lineTo(6.6f, 14.59f); lineTo(5.24f, 17.04f); curveTo(5.08f, 17.32f, 5f, 17.65f, 5f, 18f); curveTo(5f, 19.1f, 5.9f, 20f, 7f, 20f); horizontalLineToRelative(12f); verticalLineToRelative(-2f); horizontalLineTo(7.42f); curveTo(7.28f, 20f, 7.28f, 19.9f, 7.28f, 19.9f); lineTo(8.1f, 18f); horizontalLineToRelative(8.45f); curveTo(16.3f, 18f, 16.9f, 17.6f, 17.2f, 17f); lineTo(20.88f, 5.38f); curveTo(20.95f, 5.26f, 21f, 5.13f, 21f, 5f); curveTo(21f, 4.45f, 20.55f, 4f, 20f, 4f); horizontalLineTo(5.21f); lineTo(4.27f, 2f); horizontalLineTo(1f); close(); moveTo(17f, 18f); curveTo(15.9f, 18f, 15.01f, 18.9f, 15.01f, 20f); curveTo(15.01f, 21.1f, 15.9f, 22f, 17f, 22f); curveTo(18.1f, 22f, 19f, 21.1f, 19f, 20f); curveTo(19f, 18.9f, 18.1f, 18f, 17f, 18f); close() } }.build() }


    val Fitness: ImageVector by lazy { ImageVector.Builder("Fitness", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(20.57f, 14.86f); lineToRelative(-2.83f, -2.83f); lineToRelative(2.83f, -2.83f); lineToRelative(-2.83f, -2.83f); lineToRelative(-2.83f, 2.83f); lineTo(8.6f, 3.57f); lineTo(3f, 9.17f); lineToRelative(2.83f, 2.83f); lineTo(3f, 14.83f); lineToRelative(2.83f, 2.83f); lineToRelative(2.83f, -2.83f); lineToRelative(6.34f, 6.34f); lineToRelative(5.6f, -5.6f); lineToRelative(-0.03f, -0.71f); close() } }.build() }
    val Medical: ImageVector by lazy { ImageVector.Builder("Medical", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(19f, 3f); horizontalLineTo(5f); curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f); verticalLineToRelative(14f); curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f); horizontalLineToRelative(14f); curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f); verticalLineTo(5f); curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f); close(); moveTo(18f, 14f); horizontalLineToRelative(-4f); verticalLineToRelative(4f); horizontalLineToRelative(-4f); verticalLineToRelative(-4f); horizontalLineTo(6f); verticalLineToRelative(-4f); horizontalLineToRelative(4f); verticalLineTo(6f); horizontalLineToRelative(4f); verticalLineToRelative(4f); horizontalLineToRelative(4f); verticalLineToRelative(4f); close() } }.build() }
    val Restaurant: ImageVector by lazy { ImageVector.Builder("Restaurant", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(11f, 9f); horizontalLineTo(9f); verticalLineTo(2f); horizontalLineTo(7f); verticalLineTo(9f); horizontalLineTo(5f); verticalLineTo(2f); horizontalLineTo(3f); verticalLineTo(9f); curveTo(3f, 11.12f, 4.66f, 12.84f, 6.75f, 12.97f); verticalLineTo(22f); horizontalLineTo(9.25f); verticalLineTo(12.97f); curveTo(11.34f, 12.84f, 13f, 11.12f, 13f, 9f); verticalLineTo(2f); horizontalLineTo(11f); verticalLineTo(9f); close(); moveTo(16f, 6f); verticalLineToRelative(8f); horizontalLineToRelative(2.5f); verticalLineToRelative(8f); horizontalLineTo(21f); verticalLineTo(2f); curveTo(18.24f, 2f, 16f, 4.24f, 16f, 6f); close() } }.build() }
    val Flight: ImageVector by lazy { ImageVector.Builder("Flight", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(21f, 16f); verticalLineToRelative(-2f); lineTo(13f, 9.5f); verticalLineTo(4f); curveTo(13f, 3.45f, 12.55f, 3f, 12f, 3f); reflectiveCurveTo(11f, 3.45f, 11f, 4f); verticalLineTo(9.5f); lineTo(3f, 14f); verticalLineToRelative(2f); lineTo(11f, 13.5f); verticalLineTo(19f); lineTo(8f, 21f); verticalLineToRelative(1f); lineTo(12f, 21f); lineTo(16f, 22f); verticalLineToRelative(-1f); lineTo(13f, 19f); verticalLineTo(13.5f); lineTo(21f, 16f); close() } }.build() }
    val Code: ImageVector by lazy { ImageVector.Builder("Code", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(9.4f, 16.6f); lineTo(4.8f, 12f); lineTo(9.4f, 7.4f); lineTo(8f, 6f); lineTo(2f, 12f); lineTo(8f, 18f); lineTo(9.4f, 16.6f); close(); moveTo(14.6f, 16.6f); lineTo(19.2f, 12f); lineTo(14.6f, 7.4f); lineTo(16f, 6f); lineTo(22f, 12f); lineTo(16f, 18f); lineTo(14.6f, 16.6f); close() } }.build() }
    val Music: ImageVector by lazy { ImageVector.Builder("Music", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(12f, 3f); verticalLineToRelative(10.55f); curveTo(11.41f, 13.21f, 10.73f, 13f, 10f, 13f); curveTo(8.34f, 13f, 7f, 14.34f, 7f, 16f); reflectiveCurveTo(8.34f, 19f, 10f, 19f); curveTo(11.66f, 19f, 13f, 17.66f, 13f, 16f); verticalLineTo(7f); horizontalLineToRelative(4f); verticalLineTo(3f); horizontalLineToRelative(-5f); close() } }.build() }
    val Movie: ImageVector by lazy { ImageVector.Builder("Movie", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(18f, 4f); lineToRelative(2f, 4f); horizontalLineToRelative(-3f); lineToRelative(-2f, -4f); horizontalLineToRelative(-2f); lineToRelative(2f, 4f); horizontalLineToRelative(-3f); lineToRelative(-2f, -4f); horizontalLineTo(8f); lineToRelative(2f, 4f); horizontalLineTo(7f); lineTo(5f, 4f); horizontalLineTo(4f); curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f); verticalLineTo(18f); curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f); horizontalLineToRelative(16f); curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f); verticalLineTo(4f); horizontalLineToRelative(-4f); close() } }.build() }
    val Star: ImageVector by lazy { ImageVector.Builder("Star", 24.dp, 24.dp, 24f, 24f).apply { path(fill = SolidColor(Color.Black)) { moveTo(12f, 17.27f); lineTo(18.18f, 21f); lineToRelative(-1.64f, -7.03f); lineTo(22f, 9.24f); lineToRelative(-7.19f, -0.61f); lineTo(12f, 2f); lineTo(7.19f, 8.63f); lineTo(0f, 9.24f); lineToRelative(5.46f, 4.73f); lineTo(3.82f, 21f); close() } }.build() }

}
