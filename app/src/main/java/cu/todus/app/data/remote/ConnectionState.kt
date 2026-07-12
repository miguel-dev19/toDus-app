package cu.todus.app.data.remote

enum class ConnectionState {
    WAITING_FOR_CONNECTION, CONNECTING, BEFORE_CONNECTED,
    AUTHENTICATED, CONNECTED, DISCONNECTED, RECONNECTING;
    val isConnected get() = this == CONNECTED
    val isDisconnected get() = this == DISCONNECTED
}
