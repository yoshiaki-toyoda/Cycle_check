package jp.cycle_check

import android.app.Application
import io.realm.Realm

class ReamApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}