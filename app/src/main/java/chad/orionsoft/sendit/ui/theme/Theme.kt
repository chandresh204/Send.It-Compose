package chad.orionsoft.sendit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import chad.orionsoft.sendit.utils.ColorConstants

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

private val greenLightScheme = lightColorScheme(
    primary = ColorGreen. primaryLight,
    onPrimary = ColorGreen. onPrimaryLight,
    primaryContainer = ColorGreen. primaryContainerLight,
    onPrimaryContainer = ColorGreen. onPrimaryContainerLight,
    secondary = ColorGreen. secondaryLight,
    onSecondary = ColorGreen. onSecondaryLight,
    secondaryContainer = ColorGreen. secondaryContainerLight,
    onSecondaryContainer = ColorGreen. onSecondaryContainerLight,
    tertiary = ColorGreen. tertiaryLight,
    onTertiary = ColorGreen. onTertiaryLight,
    tertiaryContainer = ColorGreen. tertiaryContainerLight,
    onTertiaryContainer = ColorGreen. onTertiaryContainerLight,
    error = ColorGreen. errorLight,
    onError = ColorGreen. onErrorLight,
    errorContainer = ColorGreen. errorContainerLight,
    onErrorContainer = ColorGreen. onErrorContainerLight,
    background = ColorGreen. backgroundLight,
    onBackground = ColorGreen. onBackgroundLight,
    surface = ColorGreen. surfaceLight,
    onSurface = ColorGreen. onSurfaceLight,
    surfaceVariant = ColorGreen. surfaceVariantLight,
    onSurfaceVariant = ColorGreen. onSurfaceVariantLight,
    outline = ColorGreen. outlineLight,
    outlineVariant = ColorGreen. outlineVariantLight,
    scrim = ColorGreen. scrimLight,
    inverseSurface = ColorGreen. inverseSurfaceLight,
    inverseOnSurface = ColorGreen. inverseOnSurfaceLight,
    inversePrimary = ColorGreen. inversePrimaryLight,
    surfaceDim = ColorGreen. surfaceDimLight,
    surfaceBright = ColorGreen. surfaceBrightLight,
    surfaceContainerLowest = ColorGreen. surfaceContainerLowestLight,
    surfaceContainerLow = ColorGreen. surfaceContainerLowLight,
    surfaceContainer = ColorGreen. surfaceContainerLight,
    surfaceContainerHigh = ColorGreen. surfaceContainerHighLight,
    surfaceContainerHighest = ColorGreen. surfaceContainerHighestLight,
)

private val greenDarkScheme = darkColorScheme(
    primary = ColorGreen.primaryDark,
    onPrimary = ColorGreen.onPrimaryDark,
    primaryContainer = ColorGreen.primaryContainerDark,
    onPrimaryContainer = ColorGreen.onPrimaryContainerDark,
    secondary = ColorGreen.secondaryDark,
    onSecondary = ColorGreen.onSecondaryDark,
    secondaryContainer = ColorGreen.secondaryContainerDark,
    onSecondaryContainer = ColorGreen.onSecondaryContainerDark,
    tertiary = ColorGreen.tertiaryDark,
    onTertiary = ColorGreen.onTertiaryDark,
    tertiaryContainer = ColorGreen.tertiaryContainerDark,
    onTertiaryContainer = ColorGreen.onTertiaryContainerDark,
    error = ColorGreen.errorDark,
    onError = ColorGreen.onErrorDark,
    errorContainer = ColorGreen.errorContainerDark,
    onErrorContainer = ColorGreen.onErrorContainerDark,
    background = ColorGreen.backgroundDark,
    onBackground = ColorGreen.onBackgroundDark,
    surface = ColorGreen.surfaceDark,
    onSurface = ColorGreen.onSurfaceDark,
    surfaceVariant = ColorGreen.surfaceVariantDark,
    onSurfaceVariant = ColorGreen.onSurfaceVariantDark,
    outline = ColorGreen.outlineDark,
    outlineVariant = ColorGreen.outlineVariantDark,
    scrim = ColorGreen.scrimDark,
    inverseSurface = ColorGreen.inverseSurfaceDark,
    inverseOnSurface = ColorGreen.inverseOnSurfaceDark,
    inversePrimary = ColorGreen.inversePrimaryDark,
    surfaceDim = ColorGreen.surfaceDimDark,
    surfaceBright = ColorGreen.surfaceBrightDark,
    surfaceContainerLowest = ColorGreen.surfaceContainerLowestDark,
    surfaceContainerLow = ColorGreen.surfaceContainerLowDark,
    surfaceContainer = ColorGreen.surfaceContainerDark,
    surfaceContainerHigh = ColorGreen.surfaceContainerHighDark,
    surfaceContainerHighest = ColorGreen.surfaceContainerHighestDark,
)

private val redLightScheme = lightColorScheme(
    primary = ColorRed. primaryLight,
    onPrimary = ColorRed. onPrimaryLight,
    primaryContainer = ColorRed. primaryContainerLight,
    onPrimaryContainer = ColorRed. onPrimaryContainerLight,
    secondary = ColorRed. secondaryLight,
    onSecondary = ColorRed. onSecondaryLight,
    secondaryContainer = ColorRed. secondaryContainerLight,
    onSecondaryContainer = ColorRed. onSecondaryContainerLight,
    tertiary = ColorRed. tertiaryLight,
    onTertiary = ColorRed. onTertiaryLight,
    tertiaryContainer = ColorRed. tertiaryContainerLight,
    onTertiaryContainer = ColorRed. onTertiaryContainerLight,
    error = ColorRed. errorLight,
    onError = ColorRed. onErrorLight,
    errorContainer = ColorRed. errorContainerLight,
    onErrorContainer = ColorRed. onErrorContainerLight,
    background = ColorRed. backgroundLight,
    onBackground = ColorRed. onBackgroundLight,
    surface = ColorRed. surfaceLight,
    onSurface = ColorRed. onSurfaceLight,
    surfaceVariant = ColorRed. surfaceVariantLight,
    onSurfaceVariant = ColorRed. onSurfaceVariantLight,
    outline = ColorRed. outlineLight,
    outlineVariant = ColorRed. outlineVariantLight,
    scrim = ColorRed. scrimLight,
    inverseSurface = ColorRed. inverseSurfaceLight,
    inverseOnSurface = ColorRed. inverseOnSurfaceLight,
    inversePrimary = ColorRed. inversePrimaryLight,
    surfaceDim = ColorRed. surfaceDimLight,
    surfaceBright = ColorRed. surfaceBrightLight,
    surfaceContainerLowest = ColorRed. surfaceContainerLowestLight,
    surfaceContainerLow = ColorRed. surfaceContainerLowLight,
    surfaceContainer = ColorRed. surfaceContainerLight,
    surfaceContainerHigh = ColorRed. surfaceContainerHighLight,
    surfaceContainerHighest = ColorRed. surfaceContainerHighestLight,
)

private val redDarkScheme = darkColorScheme(
    primary = ColorRed.primaryDark,
    onPrimary = ColorRed.onPrimaryDark,
    primaryContainer = ColorRed.primaryContainerDark,
    onPrimaryContainer = ColorRed.onPrimaryContainerDark,
    secondary = ColorRed.secondaryDark,
    onSecondary = ColorRed.onSecondaryDark,
    secondaryContainer = ColorRed.secondaryContainerDark,
    onSecondaryContainer = ColorRed.onSecondaryContainerDark,
    tertiary = ColorRed.tertiaryDark,
    onTertiary = ColorRed.onTertiaryDark,
    tertiaryContainer = ColorRed.tertiaryContainerDark,
    onTertiaryContainer = ColorRed.onTertiaryContainerDark,
    error = ColorRed.errorDark,
    onError = ColorRed.onErrorDark,
    errorContainer = ColorRed.errorContainerDark,
    onErrorContainer = ColorRed.onErrorContainerDark,
    background = ColorRed.backgroundDark,
    onBackground = ColorRed.onBackgroundDark,
    surface = ColorRed.surfaceDark,
    onSurface = ColorRed.onSurfaceDark,
    surfaceVariant = ColorRed.surfaceVariantDark,
    onSurfaceVariant = ColorRed.onSurfaceVariantDark,
    outline = ColorRed.outlineDark,
    outlineVariant = ColorRed.outlineVariantDark,
    scrim = ColorRed.scrimDark,
    inverseSurface = ColorRed.inverseSurfaceDark,
    inverseOnSurface = ColorRed.inverseOnSurfaceDark,
    inversePrimary = ColorRed.inversePrimaryDark,
    surfaceDim = ColorRed.surfaceDimDark,
    surfaceBright = ColorRed.surfaceBrightDark,
    surfaceContainerLowest = ColorRed.surfaceContainerLowestDark,
    surfaceContainerLow = ColorRed.surfaceContainerLowDark,
    surfaceContainer = ColorRed.surfaceContainerDark,
    surfaceContainerHigh = ColorRed.surfaceContainerHighDark,
    surfaceContainerHighest = ColorRed.surfaceContainerHighestDark,
)

private val blueLightScheme = lightColorScheme(
    primary = ColorBlue. primaryLight,
    onPrimary = ColorBlue. onPrimaryLight,
    primaryContainer = ColorBlue. primaryContainerLight,
    onPrimaryContainer = ColorBlue. onPrimaryContainerLight,
    secondary = ColorBlue. secondaryLight,
    onSecondary = ColorBlue. onSecondaryLight,
    secondaryContainer = ColorBlue. secondaryContainerLight,
    onSecondaryContainer = ColorBlue. onSecondaryContainerLight,
    tertiary = ColorBlue. tertiaryLight,
    onTertiary = ColorBlue. onTertiaryLight,
    tertiaryContainer = ColorBlue. tertiaryContainerLight,
    onTertiaryContainer = ColorBlue. onTertiaryContainerLight,
    error = ColorBlue. errorLight,
    onError = ColorBlue. onErrorLight,
    errorContainer = ColorBlue. errorContainerLight,
    onErrorContainer = ColorBlue. onErrorContainerLight,
    background = ColorBlue. backgroundLight,
    onBackground = ColorBlue. onBackgroundLight,
    surface = ColorBlue. surfaceLight,
    onSurface = ColorBlue. onSurfaceLight,
    surfaceVariant = ColorBlue. surfaceVariantLight,
    onSurfaceVariant = ColorBlue. onSurfaceVariantLight,
    outline = ColorBlue. outlineLight,
    outlineVariant = ColorBlue. outlineVariantLight,
    scrim = ColorBlue. scrimLight,
    inverseSurface = ColorBlue. inverseSurfaceLight,
    inverseOnSurface = ColorBlue. inverseOnSurfaceLight,
    inversePrimary = ColorBlue. inversePrimaryLight,
    surfaceDim = ColorBlue. surfaceDimLight,
    surfaceBright = ColorBlue. surfaceBrightLight,
    surfaceContainerLowest = ColorBlue. surfaceContainerLowestLight,
    surfaceContainerLow = ColorBlue. surfaceContainerLowLight,
    surfaceContainer = ColorBlue. surfaceContainerLight,
    surfaceContainerHigh = ColorBlue. surfaceContainerHighLight,
    surfaceContainerHighest = ColorBlue. surfaceContainerHighestLight,
)

private val blueDarkScheme = darkColorScheme(
    primary = ColorBlue.primaryDark,
    onPrimary = ColorBlue.onPrimaryDark,
    primaryContainer = ColorBlue.primaryContainerDark,
    onPrimaryContainer = ColorBlue.onPrimaryContainerDark,
    secondary = ColorBlue.secondaryDark,
    onSecondary = ColorBlue.onSecondaryDark,
    secondaryContainer = ColorBlue.secondaryContainerDark,
    onSecondaryContainer = ColorBlue.onSecondaryContainerDark,
    tertiary = ColorBlue.tertiaryDark,
    onTertiary = ColorBlue.onTertiaryDark,
    tertiaryContainer = ColorBlue.tertiaryContainerDark,
    onTertiaryContainer = ColorBlue.onTertiaryContainerDark,
    error = ColorBlue.errorDark,
    onError = ColorBlue.onErrorDark,
    errorContainer = ColorBlue.errorContainerDark,
    onErrorContainer = ColorBlue.onErrorContainerDark,
    background = ColorBlue.backgroundDark,
    onBackground = ColorBlue.onBackgroundDark,
    surface = ColorBlue.surfaceDark,
    onSurface = ColorBlue.onSurfaceDark,
    surfaceVariant = ColorBlue.surfaceVariantDark,
    onSurfaceVariant = ColorBlue.onSurfaceVariantDark,
    outline = ColorBlue.outlineDark,
    outlineVariant = ColorBlue.outlineVariantDark,
    scrim = ColorBlue.scrimDark,
    inverseSurface = ColorBlue.inverseSurfaceDark,
    inverseOnSurface = ColorBlue.inverseOnSurfaceDark,
    inversePrimary = ColorBlue.inversePrimaryDark,
    surfaceDim = ColorBlue.surfaceDimDark,
    surfaceBright = ColorBlue.surfaceBrightDark,
    surfaceContainerLowest = ColorBlue.surfaceContainerLowestDark,
    surfaceContainerLow = ColorBlue.surfaceContainerLowDark,
    surfaceContainer = ColorBlue.surfaceContainerDark,
    surfaceContainerHigh = ColorBlue.surfaceContainerHighDark,
    surfaceContainerHighest = ColorBlue.surfaceContainerHighestDark,
)

    @Composable
fun SendItTheme(
        selectedAppColor: ColorConstants = ColorConstants.Device,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
    val appColorScheme = when(selectedAppColor) {
        ColorConstants.Device -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        ColorConstants.Blue -> {
            if (darkTheme) blueDarkScheme else blueLightScheme
        }
        ColorConstants.Red -> {
            if (darkTheme) redDarkScheme else redLightScheme
        }
        ColorConstants.Green -> {
            if (darkTheme) greenDarkScheme else greenLightScheme
        }
        ColorConstants.Purple -> {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = appColorScheme,
        typography = Typography,
        content = content
    )
}