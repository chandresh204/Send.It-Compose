package chad.orionsoft.sendit.ui.components

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun AnimatedLinearProgressBar(modifier: Modifier,
                              color: Color,
                              newProgress: () -> Float) {
    LinearProgressIndicator(progress = newProgress,
        strokeCap = StrokeCap.Round,
        trackColor = MaterialTheme.colorScheme.surface,
        color = color,
        modifier = modifier)
}