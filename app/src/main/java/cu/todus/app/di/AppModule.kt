package cu.todus.app.di
import android.content.Context
import cu.todus.app.data.local.ToDusDatabase
import cu.todus.app.data.remote.*

object AppModule {
    private lateinit var context: Context
    fun init(ctx: Context) { context = ctx.applicationContext }
    fun database() = ToDusDatabase.getInstance(context)
    fun messageDao() = database().messageDao()
    fun chatDao() = database().chatDao()
    fun contactDao() = database().contactDao()
    fun xmppClient() = XmppClient()
}
