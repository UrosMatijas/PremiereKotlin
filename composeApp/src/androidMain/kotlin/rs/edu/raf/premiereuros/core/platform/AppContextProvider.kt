package rs.edu.raf.premiereuros.core.platform

import android.content.Context

private var appContext: Context? = null

fun setApplicationContext(context: Context) {
    appContext = context.applicationContext
}

internal fun requireApplicationContext(): Context {
    return requireNotNull(appContext) {
        "Application context is not initialized. Call setApplicationContext(context) in Application.onCreate()."
    }
}
