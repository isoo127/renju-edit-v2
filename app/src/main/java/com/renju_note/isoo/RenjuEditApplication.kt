package com.renju_note.isoo

import android.app.Application
import com.renju_note.isoo.board.SeqTreeBoardManager
import com.renju_note.isoo.setting.BoardColorSetting
import com.renju_note.isoo.setting.BoardDisplaySetting
import com.renju_note.isoo.setting.ModeSetting
import com.renju_note.isoo.setting.TextAreaSetting
import com.renju_note.isoo.storage.StorageElement
import com.renju_note.isoo.util.PreferenceUtil
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class RenjuEditApplication : Application() {

    class Settings {
        var boardColorSetting = BoardColorSetting.getDefaultSetting()
        var boardDisplaySetting = BoardDisplaySetting.getDefaultSetting()
        var textAreaSetting = TextAreaSetting.getDefaultSetting()
        var modeSetting = ModeSetting.getDefaultSetting()

        fun save(pref : PreferenceUtil) {
            boardColorSetting.save(pref)
            boardDisplaySetting.save(pref)
            textAreaSetting.save(pref)
            modeSetting.save(pref)
        }

        fun load(pref : PreferenceUtil) {
            boardColorSetting.load(pref)
            boardDisplaySetting.load(pref)
            textAreaSetting.load(pref)
            modeSetting.load(pref)
        }

        fun setDefaultSetting() {
            boardColorSetting = BoardColorSetting.getDefaultSetting()
            boardDisplaySetting.sequenceVisible = true
            textAreaSetting = TextAreaSetting.getDefaultSetting()
            modeSetting = ModeSetting.getDefaultSetting()
        }
    }

    companion object {
        lateinit var pref : PreferenceUtil
        lateinit var realm: Realm
        var settings = Settings()
        var boardManager = SeqTreeBoardManager()
        var editingFile : StorageElement? = null
    }

    override fun onCreate() {
        super.onCreate()
        pref = PreferenceUtil(applicationContext)
        settings.load(pref)

        val config = RealmConfiguration.Builder(schema = setOf(StorageElement::class))
            .name("renju_edit.realm")
            .deleteRealmIfMigrationNeeded()
            .build()
        realm = Realm.open(config)
    }

}