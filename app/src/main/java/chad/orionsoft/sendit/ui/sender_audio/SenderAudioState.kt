package chad.orionsoft.sendit.ui.sender_audio

import chad.orionsoft.sendit.model.MediaAudioData

data class SenderAudioState(
    val audioList : List<AudioUIData> = emptyList(),
    val selectedCount : Int = 0,
    val selectedSize: Long =0,
    val selectedSizeToDisplay: String = "0 B",
    val isSearching: Boolean = false,
    val searchedList : List<AudioUIData> = emptyList()
)

data class AudioUIData(
    val audioData: MediaAudioData,
    val selected: Boolean,
)