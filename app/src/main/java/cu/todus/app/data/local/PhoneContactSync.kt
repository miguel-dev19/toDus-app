package cu.todus.app.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import cu.todus.app.data.local.entity.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PhoneContactSync(
    private val context: Context,
    private val countryCode: Int = 53
) {
    
    @SuppressLint("Range")
    suspend fun getPhoneContacts(): List<ContactEntity> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<ContactEntity>()
        val seen = mutableSetOf<String>()
        
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        
        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null, null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: continue
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: ""
                    val type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                    val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)) ?: ""
                    
                    val cleanNumber = number.replace(Regex("[^0-9+]"), "")
                    
                    if (cleanNumber.startsWith("+53") || cleanNumber.startsWith("53")) {
                        val normalized = cleanNumber.removePrefix("+")
                        
                        if (seen.add(normalized)) {
                            contacts.add(ContactEntity(
                                phone = normalized,
                                alias = name,
                                toDusId = "",
                                avatarUrl = photoUri,
                                isInRoster = false
                            ))
                        }
                    }
                }
            }
        } catch (e: SecurityException) { }
        
        contacts
    }
}
