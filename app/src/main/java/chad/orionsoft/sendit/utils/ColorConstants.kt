package chad.orionsoft.sendit.utils

import androidx.compose.ui.graphics.Color
import chad.orionsoft.sendit.ui.theme.ColorBlue
import chad.orionsoft.sendit.ui.theme.ColorGreen
import chad.orionsoft.sendit.ui.theme.ColorRed
import chad.orionsoft.sendit.ui.theme.Purple40

enum class ColorConstants(val id:Int, val color: Color?) {
    Device(100, null),
    Blue(101, ColorBlue.primaryLight),
    Red(102, ColorRed.primaryLight),
    Green(103, ColorGreen.primaryLight),
    Purple(104, Purple40)
}