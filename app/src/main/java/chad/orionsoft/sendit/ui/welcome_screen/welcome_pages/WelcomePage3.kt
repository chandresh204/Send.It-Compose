package chad.orionsoft.sendit.ui.welcome_screen.welcome_pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.utils.Constants

@Composable
fun WelcomePage3(modifier: Modifier = Modifier,
                 nextButtonString : String,
                 onProvideClick: () -> Unit) {
    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Constants.PAGE_PADDING.dp)) {
        Text(text = stringResource(R.string.provide_permission_from_settings), style = MaterialTheme.typography.titleLarge)
        Text(text = stringResource(R.string.app_welcome_permission_explain))

        Button(onClick = onProvideClick) {
            Text(text = nextButtonString)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    WelcomePage3(nextButtonString = "Next") {}
}