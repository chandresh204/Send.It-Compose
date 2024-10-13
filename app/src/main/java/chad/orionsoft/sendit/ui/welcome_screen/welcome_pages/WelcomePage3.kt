package chad.orionsoft.sendit.ui.welcome_screen.welcome_pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.utils.Constants

@Composable
fun WelcomePage3(modifier: Modifier = Modifier,
                 nextButtonString : String,
                 onProvideClick: () -> Unit) {
    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Constants.PAGE_PADDING.dp)) {
        Text(text = "Provide Permissions", style = MaterialTheme.typography.titleLarge)
        Text(text = "Send.It allows you to easily share your media to the devices nearby. so it requires your permission to access your media so you can easily select whatever media content you want to share with others." +
                "Send.It is not made to share your data to any third party server it is only capable to share data on local network. " +
                "There is no need for active internet connection for Send.It to work only two devices must connected with wifi network. " +
                "Whether this Wifi is connected to internet or not is irrelevant")

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