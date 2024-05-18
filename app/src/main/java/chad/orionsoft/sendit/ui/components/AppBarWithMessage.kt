package chad.orionsoft.sendit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithMessage(title: String,
                      message: String? = null,
                      isErrorMessage: Boolean = false) {
    Column {
        TopAppBar(title = {
            Text(text = title)
        })
        AnimatedVisibility(visible = !message.isNullOrBlank()) {
            Card(
                shape = RoundedCornerShape(0.dp, 0.dp, 8.dp,8.dp)
            ) {
                Text(
                    text = message ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isErrorMessage) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}