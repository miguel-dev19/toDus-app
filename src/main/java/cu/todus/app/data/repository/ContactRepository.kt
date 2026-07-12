package cu.todus.app.data.repository
import cu.todus.app.data.local.dao.ContactDao
import cu.todus.app.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val contactDao: ContactDao) {
    fun getAllContacts(): Flow<List<ContactEntity>> = contactDao.getAllContacts()
    suspend fun addContact(contact: ContactEntity) = contactDao.insert(contact)
}
