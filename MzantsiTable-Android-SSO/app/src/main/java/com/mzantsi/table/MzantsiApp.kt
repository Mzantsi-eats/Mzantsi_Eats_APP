package com.mzantsi.table

import android.app.Application
import com.mzantsi.table.data.LocalAccountStore

class MzantsiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //must run before AppViewModel is created (it restores the signed-in user).
        LocalAccountStore.init(this)
    }
}
