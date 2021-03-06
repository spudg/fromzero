package com.spudg.fromzero

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*
import kotlin.collections.ArrayList

class ValuationHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "FZValuations.db"
        private const val TABLE_VALUATIONS = "valuations"

        private const val KEY_ID = "_id"
        private const val KEY_AL = "al"
        private const val KEY_VALUE = "value"
        private const val KEY_DATE = "date"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createValuationTable =
            ("CREATE TABLE $TABLE_VALUATIONS($KEY_ID INTEGER PRIMARY KEY,$KEY_AL INTEGER,$KEY_VALUE TEXT,$KEY_DATE TEXT)")
        db?.execSQL(createValuationTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_VALUATIONS")
        onCreate(db)
    }

    fun addValuation(valuation: ValuationModel): Long {
        val values = ContentValues()
        values.put(KEY_AL, valuation.al)
        values.put(KEY_VALUE, valuation.value)
        values.put(KEY_DATE, valuation.date)
        val db = this.writableDatabase
        val success = db.insert(TABLE_VALUATIONS, null, values)
        db.close()
        return success
    }

    fun updateValuation(valuation: ValuationModel): Int {
        val values = ContentValues()
        values.put(KEY_AL, valuation.al)
        values.put(KEY_VALUE, valuation.value)
        values.put(KEY_DATE, valuation.date)
        val db = this.writableDatabase
        val success = db.update(TABLE_VALUATIONS, values, KEY_ID + "=" + valuation.id, null)
        db.close()
        return success
    }

    fun deleteValuation(valuation: ValuationModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_VALUATIONS, KEY_ID + "=" + valuation.id, null)
        db.close()
        return success
    }

    fun getValuationsForAL(alFilter: Int): ArrayList<ValuationModel> {
        val list = ArrayList<ValuationModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_VALUATIONS", null)

        var id: Int
        var al: Int
        var value: String
        var date: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                al = cursor.getInt(cursor.getColumnIndex(KEY_AL))
                value = cursor.getString(cursor.getColumnIndex(KEY_VALUE))
                date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                if (alFilter == al) {
                    val valuation = ValuationModel(
                        id = id,
                        al = al,
                        value = value,
                        date = date
                    )
                    list.add(valuation)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        list.sortByDescending { it.date.toFloat() }

        return list

    }

    fun getNetWorthForMonthYear(monthNo: Int): String {
        val valuations = ArrayList<ValuationModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_VALUATIONS", null)
        val month = monthNo % 12
        val year = (monthNo - month) / 12

        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, (month))
        cal.set(Calendar.YEAR, year)

        /*
        var id: Int
        var al: Int
        var value: String
        var date: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                al = cursor.getInt(cursor.getColumnIndex(KEY_AL))
                value = cursor.getString(cursor.getColumnIndex(KEY_VALUE))
                date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                val cal2 = Calendar.getInstance()
                cal2.timeInMillis = date.toLong()
                if (((cal2.get(Calendar.MONTH)) == month) && (cal2.get(Calendar.YEAR) == year)) {
                    val valuation = ValuationModel(
                        id = id,
                        al = al,
                        value = value,
                        date = date
                    )
                    valuations.add(valuation)
                }
            } while (cursor.moveToNext())
        }

         */

        val allValuations = getAllValuations()
        val allALs = arrayListOf<Int>()

        for (valuation in allValuations) {
            if (!allALs.contains(valuation.al)) {
                allALs.add(valuation.al)
            }
        }

        for (alItem in allALs) {
            val relevantVals = ArrayList<ValuationModel>()
            val allVals = getValuationsForAL(alItem)
            for (valuation in allVals) {
                if (valuation.date.toLong() <= cal.timeInMillis) {
                    relevantVals.add(valuation)
                }
            }
            if (relevantVals.size > 0) {
                valuations.add(relevantVals.sortedBy { it.date }.last())
            }
        }


        var rollingNetWorth = 0F

        for (valuation in valuations) {
            rollingNetWorth += valuation.value.toFloat()
        }

        cursor.close()
        db.close()

        return rollingNetWorth.toString()

    }

    fun getAllValuations(): ArrayList<ValuationModel> {
        val list = ArrayList<ValuationModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_VALUATIONS", null)

        var id: Int
        var al: Int
        var value: String
        var date: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                al = cursor.getInt(cursor.getColumnIndex(KEY_AL))
                value = cursor.getString(cursor.getColumnIndex(KEY_VALUE))
                date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                val valuation = ValuationModel(
                    id = id,
                    al = al,
                    value = value,
                    date = date
                )
                list.add(valuation)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        list.sortByDescending { it.date.toFloat() }

        return list

    }

    fun getLatestValuationForAL(alFilter: Int): String {
        val list = ArrayList<ValuationModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_VALUATIONS", null)

        var id: Int
        var al: Int
        var value: String
        var date: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                al = cursor.getInt(cursor.getColumnIndex(KEY_AL))
                value = cursor.getString(cursor.getColumnIndex(KEY_VALUE))
                date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
                if (alFilter == al) {
                    val valuation = ValuationModel(
                        id = id,
                        al = al,
                        value = value,
                        date = date
                    )
                    list.add(valuation)
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        list.sortBy { it.date.toFloat() }

        return if (list.size > 0) {
            list[list.size - 1].value
        } else {
            "0"
        }


    }

    fun getValuation(idInput: Int): ValuationModel {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_VALUATIONS WHERE $KEY_ID is $idInput", null)

        val id: Int
        val al: Int
        val value: String
        val date: String
        var valuation = ValuationModel(0, 0, "", "")

        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
            al = cursor.getInt(cursor.getColumnIndex(KEY_AL))
            value = cursor.getString(cursor.getColumnIndex(KEY_VALUE))
            date = cursor.getString(cursor.getColumnIndex(KEY_DATE))
            valuation = ValuationModel(
                id = id,
                al = al,
                value = value,
                date = date
            )
        }

        cursor.close()
        db.close()

        return valuation
    }

}
