package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chad.orionsoft.sendit.ui.theme.SendItTheme
import chad.orionsoft.sendit.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetShowConnectCode(title: String,
                               subtitle: String,
                               code: String,
                               bottomLine: String,
                               onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Constants.PAGE_PADDING.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
            Text(text = code, fontSize = 35.sp, letterSpacing = 16.sp,
                modifier = Modifier
                    .border(
                        2.dp, MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.4f
                        ),
                        shape = RoundedCornerShape(Constants.OUTLINE_TEXT_FIELD_RADIUS.dp)
                    )
                    .padding(16.dp))
            Text(text = bottomLine, style = MaterialTheme.typography.bodySmall)
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        BottomSheetShowConnectCode("Connect Code",
            "Enter below code to connect", "4524",
            "timeout in 15s") {}
    }
}