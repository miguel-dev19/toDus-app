package cu.todus.app

import android.app.Application
import cu.todus.app.data.local.ToDusDatabase
import cu.todus.app.data.remote.XmppClient

class ToDusApp : Application() {
    lateinit var database: ToDusDatabase
    lateinit var xmppClient: XmppClient
    
    override fun onCreate() {
        super.onCreate()
        database = ToDusDatabase.getInstance(this)
        xmppClient = XmppClient()
    }
}
