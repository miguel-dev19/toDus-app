package cu.todus.app.data.remote

import java.util.Base64

object ToDusProtocol {
    const val STREAM_NS = "jc"
    const val STREAM_XMLNS = "x1"
    const val SASL_NS = "urn:ietf:params:xml:ns:xmpp-sasl"
    const val BIND_NS = "urn:ietf:params:xml:ns:xmpp-bind"
    const val SESSION_NS = "x5"
    const val SESSION_ELEM = "s1"
    const val MSG_NS = "jc"
    const val BODY_K_NS = "x8"
    const val DOMAIN = "im.todus.cu"

    fun buildStreamOpen(): String = "<?xml version=\"1.0\"?><stream:stream o=\"$DOMAIN\" xmlns=\"$STREAM_NS\" xmlns:stream=\"$STREAM_XMLNS\" v=\"1.0\">"
    
    fun buildAuthPacket(phone: String, jwt: String): String {
        val authBytes = "\u0000$phone\u0000$jwt".toByteArray()
        return "<auth xmlns=\"$SASL_NS\" mechanism=\"PLAIN\">${Base64.getEncoder().encodeToString(authBytes)}</auth>"
    }
    
    fun isAuthSuccess(response: String): Boolean = response.contains("<ok") || response.contains("<success")
    fun isAuthFailure(response: String): Boolean = response.contains("<failure")
    
    fun buildBindIq(resource: String): String {
        val iqId = randomHex(8)
        return "<iq t=\"set\" i=\"$iqId\"><bind xmlns=\"$BIND_NS\"><resource>$resource</resource></bind></iq>"
    }
    
    fun buildSessionIq(): String {
        val iqId = randomHex(8)
        return "<iq t=\"set\" i=\"$iqId\"><$SESSION_ELEM xmlns=\"$SESSION_NS\"/></iq>"
    }
    
    fun buildPresence(): String = "<presence/>"
    
    fun buildOutgoingMessage(to: String, body: String, msgId: String = randomHex(16)): String =
        "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><b>${escapeXml(body)}</b></m>"
    
    fun buildGroupMessage(groupJid: String, body: String, msgId: String = randomHex(16)): String =
        "<m o=\"$groupJid\" t=\"gc\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><b>${escapeXml(body)}</b></m>"
    
    fun buildReceivedReceipt(to: String, originalMsgId: String): String {
        val ackId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$ackId\" xmlns=\"$MSG_NS\"><rd xmlns=\"$BODY_K_NS\" i=\"$originalMsgId\"/></m>"
    }
    
    fun buildDeliveredReceipt(to: String, originalMsgId: String): String {
        val ackId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$ackId\" xmlns=\"$MSG_NS\"><dd xmlns=\"$BODY_K_NS\" i=\"$originalMsgId\"/></m>"
    }
    
    fun buildComposing(to: String): String =
        "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"${randomHex(16)}\" xmlns=\"$MSG_NS\"><csp xmlns=\"uc1\"/></m>"
    
    fun buildComposingStopped(to: String): String =
        "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"${randomHex(16)}\" xmlns=\"$MSG_NS\"><csc xmlns=\"uc1\"/></m>"
    
    fun buildEditedMessage(to: String, originalMsgId: String, newBody: String): String =
        "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$originalMsgId\" xmlns=\"$MSG_NS\"><edited xmlns=\"edited:n\" i=\"edit_${randomHex(8)}\" mi=\"$originalMsgId\"/><k xmlns=\"$BODY_K_NS\"/><b>${escapeXml(newBody)}</b></m>"
    
    fun buildDeletedMessage(to: String, originalMsgId: String): String =
        "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$originalMsgId\" xmlns=\"$MSG_NS\"><deleted xmlns=\"deleted:n\" i=\"del_${randomHex(8)}\" mi=\"$originalMsgId\"/><k xmlns=\"$BODY_K_NS\"/><b/></m>"
    
    fun buildForwardedMessage(to: String, originalMsg: ToDusMessage, replyBody: String): String {
        val newMsgId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$newMsgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><forwarded xmlns=\"urn:xmpp:forward:0\"><delay xmlns=\"urn:xmpp:delay\" stamp=\"${originalMsg.timestamp}\"/><m xmlns=\"$MSG_NS\" o=\"${originalMsg.to}\" f=\"${originalMsg.from}\" i=\"${originalMsg.id}\" t=\"c\"><b>${escapeXml(originalMsg.body)}</b><k xmlns=\"$BODY_K_NS\"/></m></forwarded><b>${escapeXml(replyBody)}</b></m>"
    }
    
    fun buildOfflineIq(): String = "<iq t=\"get\" i=\"off_${randomHex(8)}\"><query xmlns=\"t:offline\"/></iq>"
    
    fun buildOfflineConfirmIq(messageIds: List<String>): String {
        val ids = messageIds.joinToString("") { "<id>$it</id>" }
        return "<iq t=\"set\" i=\"offdel_${randomHex(8)}\"><query xmlns=\"t:offline:del\">$ids</query></iq>"
    }
    
    fun buildGetUserInfoIq(phone: String): String =
        "<iq t=\"get\" i=\"prof_${randomHex(8)}\"><query xmlns=\"todus:users:getinfo\" users=\"$phone\"/></iq>"
    
    fun buildRosterListIq(): String =
        "<iq t=\"get\" i=\"rost_${randomHex(8)}\"><query xmlns=\"todus:roster:list:2\"/></iq>"
    
    fun buildBlockListIq(): String =
        "<iq t=\"get\" i=\"blk_${randomHex(8)}\" to=\"$DOMAIN\"><query xmlns=\"todus:block:get:2\"/></iq>"
    
    fun buildBlockUserIq(phone: String): String =
        "<iq t=\"set\" i=\"blk_${randomHex(8)}\" to=\"$DOMAIN\"><query xmlns=\"todus:block:set\" jid=\"$phone@$DOMAIN\"/></iq>"
    
    fun buildUnblockUserIq(phone: String): String =
        "<iq t=\"set\" i=\"unblk_${randomHex(8)}\" to=\"$DOMAIN\"><query xmlns=\"todus:block:unset\" jid=\"$phone@$DOMAIN\"/></iq>"
    
    fun buildRosterDeleteIq(phone: String): String =
        "<iq t=\"set\" i=\"del_${randomHex(8)}\"><query xmlns=\"todus:roster:delete\" jid=\"$phone@$DOMAIN\"/></iq>"
    
    fun buildPrivacyQueryIq(): String =
        "<iq t=\"get\" i=\"priv_${randomHex(8)}\"><query xmlns=\"todus:privacy\"/></iq>"
    
    fun buildPrivacyUpdateIq(info: String, last: String, photo: String): String =
        "<iq t=\"set\" i=\"privu_${randomHex(8)}\"><query xmlns=\"todus:privacy\" info=\"$info\" last=\"$last\" profile_photo=\"$photo\"/></iq>"
    
    fun buildS3UploadIq(fileType: Int, size: Long): String =
        "<iq t=\"get\" i=\"s3_${randomHex(8)}\"><query xmlns=\"todus:purl\" type=\"$fileType\" persistent=\"true\" size=\"$size\" room=\"\"/></iq>"
    
    fun buildMyGroupsIq(): String =
        "<iq t=\"get\" i=\"grp_${randomHex(8)}\" to=\"muclight.im.todus.cu\"><query xmlns=\"todus:muclight:my_mucs:2\" limit=\"100\" offset=\"0\"/></iq>"
    
    fun buildGroupInfoIq(groupJid: String): String =
        "<iq t=\"get\" i=\"gi_${randomHex(8)}\" to=\"$groupJid\"><query xmlns=\"td:g:info_by_id\"/></iq>"
    
    fun buildGroupMembersIq(groupJid: String): String =
        "<iq t=\"get\" i=\"gm_${randomHex(8)}\" to=\"$groupJid\"><query xmlns=\"x11\"/></iq>"
    
    fun buildGroupLinkIq(groupJid: String): String =
        "<iq t=\"get\" i=\"gl_${randomHex(8)}\" to=\"$groupJid\"><query xmlns=\"x14\"/></iq>"
    
    fun buildCreateGroupIq(groupJid: String, name: String, description: String = ""): String =
        "<iq t=\"set\" i=\"cg_${randomHex(8)}\" to=\"$groupJid\"><query xmlns=\"x16\"><configuration><roomname>${escapeXml(name)}</roomname><subject>${escapeXml(description)}</subject></configuration><occupants/></query></iq>"
    
    fun buildAddGroupMemberIq(groupJid: String, phone: String): String =
        "<iq t=\"set\" i=\"add_${randomHex(8)}\" to=\"$groupJid\"><query xmlns=\"td:g:add_occupant\" jid=\"$phone@$DOMAIN\"/></iq>"
    
    // ⭐ Multimedia builders
    fun buildImageMessage(to: String, s3Url: String, fileName: String, size: Long, width: Int = 800, height: Int = 600, thumbnail: String = ""): String {
        val msgId = randomHex(16); val fileId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><image xmlns=\"image:n\" i=\"$fileId\" mi=\"$msgId\" url=\"$s3Url\" n=\"${escapeXml(fileName)}\" s=\"$size\" h=\"\" w=\"$width\" he=\"$height\" tnail=\"$thumbnail\"/><b>${escapeXml(fileName)}</b></m>"
    }
    
    fun buildVideoMessage(to: String, s3Url: String, fileName: String, size: Long, duration: Int, width: Int = 1920, height: Int = 1080, thumbnail: String = ""): String {
        val msgId = randomHex(16); val fileId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><video xmlns=\"video:n\" i=\"$fileId\" mi=\"$msgId\" url=\"$s3Url\" n=\"${escapeXml(fileName)}\" s=\"$size\" h=\"\" d=\"$duration\" w=\"$width\" he=\"$height\" tnail=\"$thumbnail\"/><b>${escapeXml(fileName)}</b></m>"
    }
    
    fun buildAudioMessage(to: String, s3Url: String, fileName: String, size: Long, duration: Int): String {
        val msgId = randomHex(16); val fileId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><audio xmlns=\"audio:n\" i=\"$fileId\" mi=\"$msgId\" url=\"$s3Url\" s=\"$size\" h=\"\" d=\"$duration\" n=\"${escapeXml(fileName)}\"/><b>${escapeXml(fileName)}</b></m>"
    }
    
    fun buildVoiceMessage(to: String, s3Url: String, size: Long, duration: Int, waveform: String = ""): String {
        val msgId = randomHex(16); val fileId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><voice xmlns=\"voice:n\" i=\"$fileId\" mi=\"$msgId\" url=\"$s3Url\" s=\"$size\" h=\"\" d=\"$duration\" ws=\"$waveform\"/><b/></m>"
    }
    
    fun buildFileMessage(to: String, s3Url: String, fileName: String, size: Long): String {
        val msgId = randomHex(16); val fileId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><file xmlns=\"file:n\" i=\"$fileId\" mi=\"$msgId\" n=\"${escapeXml(fileName)}\" url=\"$s3Url\" s=\"$size\" h=\"\"/><b>${escapeXml(fileName)}</b></m>"
    }
    
    fun buildStickerMessage(to: String, stickerName: String, packId: String, hash: String = ""): String {
        val msgId = randomHex(16); val fileId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><sticker xmlns=\"sticker:n\" i=\"$fileId\" mi=\"$msgId\" n=\"${escapeXml(stickerName)}\" f=\"$packId\" url=\"\" s=\"0\" h=\"$hash\" json=\"\"/><b/></m>"
    }
    
    fun buildContactMessage(to: String, contactName: String, contactPhone: String): String {
        val msgId = randomHex(16); val fileId = randomHex(16)
        return "<m o=\"$to@$DOMAIN\" t=\"c\" i=\"$msgId\" xmlns=\"$MSG_NS\"><k xmlns=\"$BODY_K_NS\"/><contact xmlns=\"contact:n\" i=\"$fileId\" mi=\"$msgId\" n=\"${escapeXml(contactName)}\" num=\"$contactPhone\"/><b/></m>"
    }
    
    // ⭐ Parsers
    fun parseIncomingMessage(xml: String): ToDusMessage? {
        return try {
            val id = extractAttribute(xml, "i") ?: return null
            val from = extractAttribute(xml, "f") ?: ""
            val to = extractAttribute(xml, "o") ?: ""
            val type = extractAttribute(xml, "t") ?: "c"
            val body = extractBody(xml) ?: ""
            val isReceipt = xml.contains("<rd ") || xml.contains("<dd ")
            val receiptMsgId = if (isReceipt) extractReceiptMsgId(xml) else null
            ToDusMessage(id = id, from = from, to = to, body = body, type = type, rawXml = xml,
                isReceipt = isReceipt, receiptMsgId = receiptMsgId,
                isComposing = xml.contains("<csp ") || xml.contains("<csc "),
                isPresence = xml.contains("<p "),
                isDeliveryAck = xml.contains("<tdack "))
        } catch (e: Exception) { null }
    }
    
    fun parseOfflineMessages(xml: String): List<ToDusMessage> {
        val messages = mutableListOf<ToDusMessage>()
        Regex("""<m\b.*?</m>""", RegexOption.DOT_MATCHES_ALL).findAll(xml).forEach { match ->
            parseIncomingMessage(match.value)?.let { msg ->
                val ts = Regex("""<todus_offline ts='(\d+)'/>""").find(match.value)?.groupValues?.get(1)?.toLongOrNull()
                messages.add(if (ts != null) msg.copy(timestamp = ts) else msg)
            }
        }
        return messages
    }
    
    fun parseRosterContacts(xml: String): List<Pair<String, String>> {
        val contacts = mutableListOf<Pair<String, String>>()
        Regex("""<contact alias='([^']*)' phone='([^']*)'""").findAll(xml).forEach { match ->
            val alias = match.groupValues[1]; val phone = match.groupValues[2]
            if (phone.matches(Regex("^\\d{8,}$"))) contacts.add(phone to alias)
        }
        return contacts
    }
    
    fun extractBody(xml: String): String? = Regex("""<b[^>]*>(.*?)</b>""", RegexOption.DOT_MATCHES_ALL).find(xml)?.groupValues?.get(1)?.let { unescapeXml(it) }
    fun extractAttribute(xml: String, attrName: String): String? = Regex("""$attrName\s*=\s*['"]([^'"]*)['"]""").find(xml)?.groupValues?.get(1)
    fun extractBindJid(xml: String): String? = Regex("""<jid>(.*?)</jid>""").find(xml)?.groupValues?.get(1)
    fun extractReceiptMsgId(xml: String): String? = Regex("""<rd[^>]*i=['"]([^'"]*)['"]""").find(xml)?.groupValues?.get(1) ?: Regex("""<dd[^>]*i=['"]([^'"]*)['"]""").find(xml)?.groupValues?.get(1)
    fun extractDeliveryAckMsgId(xml: String): String? = Regex("""<tdack[^>]*mi=['"]([^'"]*)['"]""").find(xml)?.groupValues?.get(1)
    fun isMessage(xml: String): Boolean = xml.contains("<m ")
    fun isReceipt(xml: String): Boolean = xml.contains("<rd ") || xml.contains("<dd ")
    
    fun randomHex(length: Int): String {
        val chars = "abcdef0123456789"
        return (1..length).map { chars.random() }.joinToString("")
    }
    
    private fun escapeXml(text: String): String = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
    private fun unescapeXml(text: String): String = text.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&apos;", "'")
}
