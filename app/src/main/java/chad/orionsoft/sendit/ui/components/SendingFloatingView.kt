package chad.orionsoft.sendit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chad.orionsoft.sendit.R

@Composable
fun SendFloatingButton(
    modifier: Modifier = Modifier,
    selectedCount : Int,
    sizeText : String,
    isWarning: Boolean,
    onClick : () -> Unit
) {
    Card(modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
        defaultElevation = 4.dp
    ), colors = CardDefaults.cardColors(
        containerColor = if (isWarning) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.surface
    )) {
        Row(modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = "$selectedCount selected",
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall)
                Text(text = sizeText,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = {  }) {
                Image(painterResource(id = R.drawable.sendit_icon_play), contentDescription = "send")
            }
        }
    }
}

@Composable
fun NotConnectedFloatingButton(
    modifier: Modifier = Modifier,
    onClick : () -> Unit
) {
   Box(modifier.clickable(onClick = onClick)) {
        Card(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp),
            shape = RoundedCornerShape(
                topEnd = 8.dp, bottomEnd = 8.dp
            ),
            elevation = CardDefaults
                .cardElevation(
                    defaultElevation = 4.dp
                )
        ) {
            Text(text = "Not\nConnected",
                modifier = Modifier.padding(start = 33.dp, top = 4.dp, bottom = 4.dp, end = 8.dp),
                style = MaterialTheme.typography.bodySmall)
        }
        Image(
            painterResource(id = R.drawable.devices_off_24px),
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(12.dp),
            colorFilter = ColorFilter.tint(
                color = MaterialTheme.colorScheme.onPrimary
            ),
            contentDescription = null
        )
    }
}

@Composable
fun ConnectedFloatingButton(modifier: Modifier = Modifier,
                            startLetter: Char,
                            color: Color,
                            onClick: () -> Unit) {
    Text(text = startLetter.toString(),
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .padding(horizontal = 22.dp, vertical = 12.dp)
            .clickable(onClick = onClick)
    )
}

@Composable
fun SendingFloatingCard(modifier: Modifier,
                        sendingSpeed: String,
                        sendingSpeedUnit: String,
                        currentItem: String,
                        progressUpdate: () -> Float,
                        onClick: () -> Unit) {
    Card(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
        defaultElevation = 4.dp
    )) {
        Box {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconInProgressBar(Icons.Default.KeyboardArrowUp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(text = "Sending...", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = currentItem,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = sendingSpeed, style = MaterialTheme.typography.titleLarge)
                    Text(text = sendingSpeedUnit, style = MaterialTheme.typography.bodySmall)
                }
            }
            AnimatedLinearProgressBar(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
                progressUpdate)
        }
    }
}

@Composable
fun ReceivingFloatingCard(modifier: Modifier,
                          receivingSpeed: String,
                          receivingSpeedUnit: String,
                          currentItem: String,
                          progressUpdate: () -> Float,
                          received: Int,
                          onClick: () -> Unit
) {
    Card(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
        defaultElevation = 4.dp
    )) {
        Box {
            if(receivingSpeed == "0" && progressUpdate.invoke() == 1f) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = "Done")
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(text = "$received file received", style = MaterialTheme.typography.titleSmall)
                        Text(text = "Tap here to view", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
            } else {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = receivingSpeed, style = MaterialTheme.typography.titleLarge)
                        Text(text = receivingSpeedUnit, style = MaterialTheme.typography.bodySmall)
                    }
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.End) {
                        Text(text = "Receiving...", style = MaterialTheme.typography.titleSmall)
                        Text(text = currentItem, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                    IconInProgressBar(Icons.Default.KeyboardArrowDown)
                }
                AnimatedLinearProgressBar(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                    progressUpdate)
            }
        }
    }
}

@Composable
private fun IconInProgressBar(
    imageVector: ImageVector,
    modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Icon(
            imageVector,
            contentDescription = "icon",
            modifier = Modifier.align(Alignment.Center)
        )
        CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun Preview() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ConnectedFloatingButton(
                    modifier = Modifier.padding(16.dp),
                    startLetter ='E',
                    color = Color.DarkGray
                    ) {}
                NotConnectedFloatingButton {

                }
                Spacer(modifier = Modifier.weight(1f))
                SendFloatingButton(modifier = Modifier
                    .padding(16.dp),
                    selectedCount = 15,
                    sizeText = "15 MB/1.4 GB",
                    isWarning = false
                ) {}
            }
            SendingFloatingCard(
                modifier = Modifier
                    .padding(16.dp),
                sendingSpeed = "1.25",
                sendingSpeedUnit = "MB/s",
                currentItem = "(1/6) The milky way.mp4",
                { 0.71f}
            ) {}
            ReceivingFloatingCard(
                modifier = Modifier
                    .padding(16.dp),
                receivingSpeed = "0",
                receivingSpeedUnit = "KB/s",
                currentItem = "The sky is blue but .mp4",
                { 1f },
                received = 5,
            ) {}
        }

    }
}