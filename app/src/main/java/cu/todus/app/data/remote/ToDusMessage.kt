package cu.todus.app.data.remote

data class ToDusMessage(
    val id: String,
    val from: String,
    val to: String,
    val body: String,
    val type: String = "c",
    val timestamp: Long = System.currentTimeMillis(),
    val rawXml: String = "",
    val isReceipt: Boolean = false,
    val receiptMsgId: String? = null,
    val isComposing: Boolean = false,
    val isPresence: Boolean = false,
    val isDeliveryAck: Boolean = false
)
