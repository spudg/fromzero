package com.spudg.fromzero

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ALHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "FZAssetsLiabilities.db"
        private const val TABLE_AL = "assets_liabilities"

        private const val KEY_ID = "_id"
        private const val KEY_NAME = "name"
        private const val KEY_NOTE = "note"
        private const val KEY_COLOUR = "colour"
        private const val KEY_AL = "al"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createALTable =
            ("CREATE TABLE $TABLE_AL($KEY_ID INTEGER PRIMARY KEY,$KEY_AL INTEGER,$KEY_NAME TEXT,$KEY_NOTE TEXT,$KEY_COLOUR TEXT)")
        db?.execSQL(createALTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_AL")
        onCreate(db)
    }

    fun addAL(al: ALModel): Long {
        val values = ContentValues()
        values.put(KEY_NAME, al.name)
        values.put(KEY_NOTE, al.note)
        values.put(KEY_COLOUR, al.colour)
        values.put(KEY_AL, al.al)
        val db = this.writableDatabase
        val success = db.insert(TABLE_AL, null, values)
        db.close()
        return success
    }

    fun updateAL(al: ALModel): Int {
        val values = ContentValues()
        values.put(KEY_AL, al.al)
        values.put(KEY_NAME, al.name)
        values.put(KEY_NOTE, al.note)
        values.put(KEY_COLOUR, al.colour)
        val db = this.writableDatabase
        val success = db.update(TABLE_AL, values, KEY_ID + "=" + al.id, null)
        db.close()
        return success
    }

    fun deleteAL(al: ALModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_AL, KEY_ID + "=" + al.id, null)
        db.close()
        return success
    }

    fun getAllAssets(): ArrayList<ALModel> {
        val list = ArrayList<ALModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_AL", null)

        var id: Int
        var al: Int
        var name: String
        var note: String
        var colour: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                al = cursor.getInt(cursor.getColumnIndex(KEY_AL))
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
                colour = cursor.getString(cursor.getColumnIndex(KEY_COLOUR))
                if (al == 1) {
                    val asset = ALModel(
                        id = id,
                        al = al,
                        name = name,
                        note = note,
                        colour = colour
                    )
                    list.add(asset)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return list

    }

    fun getAllLiabilities(): ArrayList<ALModel> {
        val list = ArrayList<ALModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_AL", null)

        var id: Int
        var al: Int
        var name: String
        var note: String
        var colour: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                al = cursor.getInt(cursor.getColumnIndex(KEY_AL))
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
                colour = cursor.getString(cursor.getColumnIndex(KEY_COLOUR))
                if (al == 0) {
                    val liability = ALModel(
                        id = id,
                        al = al,
                        name = name,
                        note = note,
                        colour = colour
                    )
                    list.add(liability)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return list

    }

    fun getLatestALID(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_AL", null)

        var id = 0

        if (cursor.moveToLast()) {
            id = cursor.getInt(cursor.getColumnIndex(KEY_ID))

        }

        cursor.close()
        db.close()

        return id
    }

    fun getALName(id: Int): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_AL WHERE $KEY_ID IS $id", null)

        var name = ""

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
        }

        cursor.close()
        db.close()

        return name
    }

    fun isAsset(id: Int): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_AL WHERE $KEY_ID IS $id", null)

        var al = 0

        if (cursor.moveToFirst()) {
            al = cursor.getInt(cursor.getColumnIndex(KEY_AL))
        }

        cursor.close()
        db.close()

        return al != 0

    }

    fun getALColour(id: Int): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_AL WHERE $KEY_ID IS $id", null)

        var colour = ""

        if (cursor.moveToFirst()) {
            colour = cursor.getString(cursor.getColumnIndex(KEY_COLOUR))
        }

        cursor.close()
        db.close()

        return colour
    }

    fun getAL(idInput: Int): ALModel {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_AL WHERE $KEY_ID IS $idInput", null)

        var alModel = ALModel(0, 0, "", "", "")
        val id: Int
        val al: Int
        val name: String
        val note: String
        val colour: String

        if (cursor.moveToFirst()) {
            id = idInput
            al = cursor.getInt(cursor.getColumnIndex(KEY_AL))
            name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
            note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
            colour = cursor.getString(cursor.getColumnIndex(KEY_COLOUR))
            alModel = ALModel(id, al, name, note, colour)
        }

        cursor.close()
        db.close()

        return alModel
    }

}
