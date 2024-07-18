package chad.orionsoft.sendit.ui.receiver_main

sealed class ReceiverMainEvent {
    data object StartReceiver :ReceiverMainEvent()
}