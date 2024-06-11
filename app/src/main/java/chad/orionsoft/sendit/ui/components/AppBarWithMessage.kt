package chad.orionsoft.sendit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarWithMessage(title: String,
                      message: String? = null,
                      isErrorMessage: Boolean = false,
                      withProgress: Float? = null,
                      appBarMenu: List<DropDownMenuData> = emptyList(),
                      isProvideSearch: Boolean = false,
                      onSearchQueryChanged: (String) -> Unit = {},
                      onBackPress: (() -> Unit)? = null) {
    var messageSize : Pair<Dp, Dp> by remember {
        mutableStateOf(Pair(0.dp, 0.dp))
    }
    var isSearching by remember {
        mutableStateOf(false)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    var expandedDropDown by remember {
        mutableStateOf(false)
    }

    val density = LocalDensity.current
    Column {
        if(isSearching) {
            SearchView(query = searchText, onQueryChanged = {
                searchText = it
                onSearchQueryChanged(it)
            }) {
                isSearching = false
            }
        } else {
            TopAppBar(title = {
                Text(text = title)
            },
                navigationIcon = {
                    onBackPress?.let {
                        IconButton(onClick = onBackPress) {
                            Icon(Icons.Default.ArrowBack, "back")
                        }
                    }
                },
                actions = {
                    if(isProvideSearch) {
                        IconButton(onClick = {
                            isSearching = true
                        }) {
                            Icon(Icons.Default.Search, "search")
                        }
                    }
                    if(appBarMenu.isNotEmpty()) {
                        Box {
                            IconButton(onClick = {
                                expandedDropDown = true
                            }) {
                                Icon(Icons.Default.MoreVert, "menu")
                            }
                        }
                        DropdownMenu(expanded = expandedDropDown,
                            onDismissRequest = {
                                expandedDropDown = false
                            }) {
                            appBarMenu.forEach {
                                DropdownMenuItem(text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(it.icon, null)
                                        Text(text = it.text)
                                    }
                                },
                                    onClick = {
                                        it.onSelected
                                        expandedDropDown = false
                                    })
                            }
                        }
                    }
                }
            )
        }
        AnimatedVisibility(visible = !message.isNullOrBlank()) {
            Card(
                shape = RoundedCornerShape(0.dp, 0.dp, 8.dp,8.dp),
                modifier =  if(withProgress != null) {
                    Modifier.onGloballyPositioned {
                        with(density) {
                            val height = it.size.height.toDp()
                            val width = it.size.width.toDp()
                            messageSize = Pair(width, height)
                        }
                    }
                } else Modifier
            ) {
                Box {
                    if(withProgress != null) {
                        Box(
                            modifier = Modifier
                                .width(messageSize.first.times(withProgress))
                                .height(messageSize.second)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text(
                        text = message ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            shadow = Shadow(
                                offset = Offset(1f,1f),
                                blurRadius = 2f
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (withProgress == null) {
                                    if (isErrorMessage) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                                } else Color.Transparent
                            )
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

data class DropDownMenuData(
    val icon: ImageVector,
    val text: String,
    val onSelected: () -> Unit,
)