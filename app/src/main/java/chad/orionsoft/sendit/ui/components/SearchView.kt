package chad.orionsoft.sendit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    query: String,
    onQueryChanged: (String) -> Unit,
    onDismiss : () -> Unit
) {
    SearchBar(query = query,
        onQueryChange = onQueryChanged,
        leadingIcon = {
            IconButton(onClick = {  }) {
                Icon(Icons.Default.Search, null)
            }
        },
        trailingIcon = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null)
            }
        },
        onSearch = {},
        active = false,
        onActiveChange = {}) {

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Preview() {
    SearchView(
        "hei",
        {}, {}
    )
}