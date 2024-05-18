package chad.orionsoft.sendit.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.utils.Constants
import chad.orionsoft.sendit.utils.RuntimeConstants

@Preview(device = "spec:width=360dp,height=720dp,dpi=440", showBackground = true,
    showSystemUi = true
)
@Composable
fun WelcomeScreen(navToHome: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(id = R.drawable.sendit_icon_play),
            contentDescription = "App Icon",
            modifier = Modifier.size(RuntimeConstants.screenWidthDp / 1.5f))
        Text(text = stringResource(id = R.string.welcome_to_send_it),
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = stringResource(id = R.string.enter_name_to_continue))
        Spacer(modifier = Modifier.padding(4.dp))
        OutlinedTextField(
            leadingIcon = {
                          Icon(Icons.Default.Face, contentDescription = "User")
            },
            value = "",
            onValueChange = {},
            placeholder = {
                Text(text = "e.g. John",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            },
            shape = RoundedCornerShape(Constants.OUTLINE_TEXT_FIELD_RADIUS.dp),
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Button(onClick = navToHome) {
            Text(stringResource(id = R.string.next))
        }
    }
}