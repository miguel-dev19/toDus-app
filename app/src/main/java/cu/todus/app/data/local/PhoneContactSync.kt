package cu.todus.app.data.local

import android.content.Context
import android.provider.ContactsContract
import cu.todus.app.data.local.dao.ContactDao
import cu.todus.app.data.local.entity.ContactEntity

class PhoneContactSync(private val context: Context, private val contactDao: ContactDao) {
    
    suspend fun syncContacts() {
        val contacts = mutableListOf<ContactEntity>()
        val contentResolver = context.contentResolver
        
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            ),
            null, null, null
        )
        
        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            
            while (it.moveToNext()) {
                val number = it.getString(numberIndex).replace(Regex("[^0-9]"), "")
                val name = it.getString(nameIndex) ?: number
                
                if (number.length >= 8) {
                    contacts.add(
                        ContactEntity(
                            phone = number,
                            name = name
                        )
                    )
                }
            }
        }
        
        if (contacts.isNotEmpty()) {
            contactDao.insertAll(contacts)
        }
    }
}
