package chad.orionsoft.sendit.ui.components

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun AnimatedLinearProgressBar(modifier: Modifier,
                              newProgress: () -> Float) {
    LinearProgressIndicator(progress = newProgress.invoke(),
        strokeCap = StrokeCap.Round,
        trackColor = MaterialTheme.colorScheme.surface,
        modifier = modifier)
}