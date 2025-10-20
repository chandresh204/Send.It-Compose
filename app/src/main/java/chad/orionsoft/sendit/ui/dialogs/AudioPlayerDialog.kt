package chad.orionsoft.sendit.ui.dialogs

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import chad.orionsoft.sendit.R
import chad.orionsoft.sendit.utils.Converters.formatDuration
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerDialog(audioUri: Uri, albumArt: Bitmap?, audioTitle : String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var mediaPlayer : MediaPlayer by remember {
        mutableStateOf(MediaPlayer())
    }
    var sliderProgress by remember { mutableFloatStateOf(0f) }
    var currentDurationString by remember { mutableStateOf("00:00") }
    var totalDuration by remember { mutableIntStateOf(0) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var seekingValue by remember { mutableFloatStateOf(0f) }
    var seekNow by remember { mutableStateOf(false) }

    val updatedSliderView = {
        mediaPlayer.let { vView ->
            sliderProgress = vView.currentPosition.toFloat() / vView.duration
            currentDurationString = formatDuration(vView.currentPosition)
        }
    }
    DisposableEffect(audioUri) {
        onDispose {
            if(isAudioPlaying) {
                isAudioPlaying = false
                mediaPlayer.stop()
            }
        }
    }
    LaunchedEffect(audioUri) {
        mediaPlayer.let {
            println("Audio play >> setting data source")
            it.setDataSource(context, audioUri)
            it.prepareAsync()
            it.setOnPreparedListener { mediaPlayer ->
                println("Audio play >> prepared $audioUri")
                mediaPlayer.start()
                isAudioPlaying = true
                totalDuration = mediaPlayer.duration
            }
            it.setOnCompletionListener { mediaPlayer ->
                mediaPlayer?.seekTo(0)
                mediaPlayer?.start()
                updatedSliderView()
            }
            it.setOnErrorListener { mp, what, extra ->
                isAudioPlaying = false
                println("audio play error >> $what")
                true
            }
        }
    }
    LaunchedEffect(isAudioPlaying) {
        while (isAudioPlaying) {
            delay(1.seconds)
            updatedSliderView()
        }
    }
    LaunchedEffect(seekNow) {
        if(seekNow) {
            seekNow = false
            mediaPlayer.seekTo((seekingValue*totalDuration).toInt())
            updatedSliderView()
        }
    }
    Column(modifier = Modifier
        .clip(
            RoundedCornerShape(size = 24.dp)
        )
        .background(color = MaterialTheme.colorScheme.background)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(audioTitle,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Clear, contentDescription = null)
            }
        }
        if(albumArt == null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(painterResource(R.drawable.music_note_24px),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(100.dp),
                    contentDescription = "Audio")
            }
        } else {
            Image(
                bitmap = albumArt.asImageBitmap(),
                contentDescription = "AlbumArt",
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .padding(horizontal = 8.dp)
            .height(30.dp)) {
            Text(currentDurationString, style = MaterialTheme.typography.bodySmall)
            Slider(
                value = sliderProgress,
                onValueChange = {
                    seekingValue = it
                },
                onValueChangeFinished = {
                    seekNow = true
                },
                thumb = {
                    Box(modifier = Modifier
                        .clip(
                            CircleShape
                        )
                        .size(15.dp)
                        .background(MaterialTheme.colorScheme.primary))
                },
                track = {
                    SliderDefaults.Track(it,
                        modifier = Modifier.scale(1f, 0.5f))
                },
                modifier = Modifier.weight(1f)
            )
            Text(formatDuration(totalDuration), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview
@Composable
private fun AudioPlayerPreview() {
    AudioPlayerDialog(":".toUri(),
        null,
        "sadfdsf") { }
}