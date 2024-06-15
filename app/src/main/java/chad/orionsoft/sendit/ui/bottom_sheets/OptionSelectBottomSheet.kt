package chad.orionsoft.sendit.ui.bottom_sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import chad.orionsoft.sendit.ui.components.TextWithIcon
import chad.orionsoft.sendit.ui.model.SortOrder
import chad.orionsoft.sendit.ui.theme.SendItTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> OptionSelectBottomSheet(
    title: String,
    options: List<T>,
    selectedOption: T,
    onSelectOption: (T) -> Unit,
    onCancel: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = {false}
    )
    var selectedOption1 by remember {
        mutableStateOf(selectedOption)
    }
    val coroutineScope = rememberCoroutineScope()

    val onOptionSelected : (T) -> Unit = {
        coroutineScope.launch {
            selectedOption1 = it
            delay(500)
            sheetState.hide()
            delay(500)
            onSelectOption(it)
            onCancel()
        }
    }
    ModalBottomSheet(onDismissRequest = onCancel, sheetState = sheetState) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close,
                        modifier = Modifier.size(40.dp),
                        contentDescription = "close")
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                options.forEach {
                    SelectionItem(it == selectedOption1, it.toString()) {
                        onOptionSelected(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionItem(isSelected: Boolean, text: String, onSelect: () -> Unit) {
    if(isSelected) {
        Button(onClick ={}) {
            TextWithIcon(imageVector = Icons.Default.Check,
                text = text)
        }
    } else {
        OutlinedButton(onClick = onSelect) {
            Text(text = text)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SendItTheme {
        OptionSelectBottomSheet("Select Sort Order",
            options = listOf(
                SortOrder.ByName, SortOrder.ByDate
            ),
            selectedOption = SortOrder.ByName,
            onSelectOption = {}
        ) {}
    }
}

