package id.tandara.parent

import android.app.Application
import id.tandara.parent.core.common.AppContainer
import id.tandara.parent.core.common.DefaultAppContainer

class TandaraApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
        container.parentRealtimeCoordinator.start()
        instance = this
    }

    companion object {
        lateinit var instance: TandaraApplication
            private set
    }
}
