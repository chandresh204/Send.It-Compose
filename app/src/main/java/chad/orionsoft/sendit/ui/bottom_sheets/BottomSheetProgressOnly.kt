package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetProgressOnly(title: String, progress:Float? = null) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = {}, sheetState = sheetState) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Constants.PAGE_PADDING.dp, vertical = 32.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.padding(vertical = 16.dp))
            if(progress != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = progress)
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}