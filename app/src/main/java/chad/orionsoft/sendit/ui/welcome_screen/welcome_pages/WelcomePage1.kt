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
import chad.orionsoft.sendit.ui.components.UserNameTextField
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.RuntimeConstants

@Composable
fun WelcomePage1(modifier: Modifier = Modifier,
                 nameText: String,
                 onNameUpdated: (String) -> Unit,
                 onSaveClicked: () -> Unit) {
    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Constants.PAGE_PADDING.dp)) {
        Text(text = "Welcome to Send.It", style = MaterialTheme.typography.titleLarge)
        Text(text = "Before starting the Send.It there are few things that needs to be set up, " +
                "please provide few details that you can get the best from the Send.It")
        UserNameTextField(myName = nameText, onNameEdited = onNameUpdated)
        if(nameText.isNotEmpty()) {
            Text(text = "Other devices will see you as ${RuntimeConstants.createMyUserName(nameText)}")
        }
        Button(onClick = onSaveClicked, enabled = nameText.isNotEmpty()) {
            Text(text = "Save and Continue")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Preview() {
    WelcomePage1(nameText = "As", onNameUpdated = {}) {}
}