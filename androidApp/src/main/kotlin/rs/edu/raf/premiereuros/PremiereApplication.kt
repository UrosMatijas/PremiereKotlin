package rs.edu.raf.premiereuros

import android.app.Application
import rs.edu.raf.premiereuros.core.platform.setApplicationContext
import rs.edu.raf.premiereuros.di.initKoin

class PremiereApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setApplicationContext(this)
        initKoin()
    }
}
