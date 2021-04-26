package com.spudg.fromzero

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.spudg.fromzero.databinding.*
import java.util.*

class ValuationActivity : AppCompatActivity() {

    private lateinit var bindingValuation: ActivityValuationBinding
    private lateinit var bindingAddValuation: DialogAddValuationBinding
    private lateinit var bindingUpdateValuation: DialogUpdateValuationBinding
    private lateinit var bindingDeleteValuation: DialogDeleteValuationBinding
    private lateinit var bindingUpdateAL: DialogUpdateAlBinding
    private lateinit var bindingDeleteAL: DialogDeleteAlBinding

    private lateinit var bindingDMYPicker: DayMonthYearPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingValuation = ActivityValuationBinding.inflate(layoutInflater)
        val view = bindingValuation.root
        setContentView(view)

        setUpTitles()
        setUpValuationList(Globals.alSelected)

        bindingValuation.addValuation.setOnClickListener {
            addValuation()
        }

        bindingValuation.alUpdate.setOnClickListener {
            updateAL()
        }

        bindingValuation.alDelete.setOnClickListener {
            deleteAL()
        }

        bindingValuation.backToMainFromAL.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun deleteAL() {
        val deleteDialog = Dialog(this, R.style.Theme_Dialog)
        deleteDialog.setCancelable(false)
        bindingDeleteAL = DialogDeleteAlBinding.inflate(layoutInflater)
        val view = bindingDeleteAL.root
        deleteDialog.setContentView(view)
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val valuationHandler = ValuationHandler(this, null)
        val alHandler = ALHandler(this, null)

        if (alHandler.isAsset(Globals.alSelected)) {
            bindingDeleteAL.deleteALTitle.text = "Delete asset"
            bindingDeleteAL.deleteALWarning.text = "Are you sure you want to delete this asset?"
        } else {
            bindingDeleteAL.deleteALTitle.text = "Delete liability"
            bindingDeleteAL.deleteALWarning.text = "Are you sure you want to delete this liability?"
        }

        bindingDeleteAL.tvDelete.setOnClickListener {

            val associatedValuations = valuationHandler.getValuationsForAL(Globals.alSelected)
            for (valuation in associatedValuations) {
                valuationHandler.deleteValuation(valuationHandler.getValuation(valuation.id))
            }

            alHandler.deleteAL(alHandler.getAL(Globals.alSelected))

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

            Toast.makeText(this, "Valuation deleted.", Toast.LENGTH_LONG).show()

            setUpValuationList(Globals.alSelected)
            valuationHandler.close()
            deleteDialog.dismiss()
        }

        bindingDeleteAL.tvCancel.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()
    }

    private fun updateAL() {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        bindingUpdateAL = DialogUpdateAlBinding.inflate(layoutInflater)
        val view = bindingUpdateAL.root
        updateDialog.setContentView(view)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val alHandler = ALHandler(this, null)
        val originalAL = alHandler.getAL(Globals.alSelected)

        bindingUpdateAL.etName.setText(originalAL.name)
        bindingUpdateAL.colourPicker.color = originalAL.colour.toInt()
        bindingUpdateAL.colourPicker.showOldCenterColor = false

        if (originalAL.al == 1) {
            bindingUpdateAL.updateALTitle.text = "Update asset"
        } else {
            bindingUpdateAL.updateALTitle.text = "Update liability"
        }

        bindingUpdateAL.tvUpdate.setOnClickListener {
            val name = bindingUpdateAL.etName.text.toString()
            val colour = bindingUpdateAL.colourPicker.color.toString()
            val updatedAL = ALModel(originalAL.id, originalAL.al, name, originalAL.note, colour)
            if (name.isNotEmpty()) {
                alHandler.updateAL(updatedAL)
                setUpTitles()
                updateDialog.dismiss()
                if (originalAL.al == 1) {
                    Toast.makeText(this, "Asset updated.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Liability updated.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bindingUpdateAL.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()

    }

    private fun setUpTitles() {

        val dbHandler = ALHandler(this, null)

        bindingValuation.alTitle.text = dbHandler.getALName(Globals.alSelected)
        if (dbHandler.isAsset(Globals.alSelected)) {
            bindingValuation.assetOrLiability.text = "Asset"
        } else {
            bindingValuation.assetOrLiability.text = "Liability"
        }

        bindingValuation.alColour.setBackgroundColor(dbHandler.getALColour(Globals.alSelected).toInt())

    }

    private fun getValuationList(al: Int): ArrayList<ValuationModel> {
        val dbHandler = ValuationHandler(this, null)
        val result = dbHandler.getValuationsForAL(al)
        dbHandler.close()
        return result
    }

    private fun setUpValuationList(al: Int) {
        if (getValuationList(al).size > 0) {
            bindingValuation.rvValuations.visibility = View.VISIBLE
            bindingValuation.tvNoValuations.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            bindingValuation.rvValuations.layoutManager = manager
            val assetAdapter = ValuationAdapter(this, getValuationList(al))
            bindingValuation.rvValuations.adapter = assetAdapter
        } else {
            bindingValuation.rvValuations.visibility = View.GONE
            bindingValuation.tvNoValuations.visibility = View.VISIBLE
        }

    }

    private fun addValuation() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        bindingAddValuation = DialogAddValuationBinding.inflate(layoutInflater)
        val view = bindingAddValuation.root
        addDialog.setContentView(view)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        var monthPicked = Calendar.getInstance()[Calendar.MONTH]
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        bindingAddValuation.date.text = "$dayPicked ${Globals.getShortMonth(monthPicked+1)} $yearPicked"

        bindingAddValuation.date.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingDMYPicker = DayMonthYearPickerBinding.inflate(layoutInflater)
            val viewDMYP = bindingDMYPicker.root
            changeDateDialog.setContentView(viewDMYP)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 4 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 6 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 9 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 11) {
                bindingDMYPicker.dmypDay.maxValue = 30
                bindingDMYPicker.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 == 0)) {
                bindingDMYPicker.dmypDay.maxValue = 29
                bindingDMYPicker.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 != 0)) {
                bindingDMYPicker.dmypDay.maxValue = 28
                bindingDMYPicker.dmypDay.minValue = 1
            } else {
                bindingDMYPicker.dmypDay.maxValue = 31
                bindingDMYPicker.dmypDay.minValue = 1
            }

            bindingDMYPicker.dmypMonth.maxValue = 12
            bindingDMYPicker.dmypMonth.minValue = 1
            bindingDMYPicker.dmypYear.maxValue = 2999
            bindingDMYPicker.dmypYear.minValue = 1000

            bindingDMYPicker.dmypDay.value = dayPicked
            bindingDMYPicker.dmypMonth.value = monthPicked
            bindingDMYPicker.dmypYear.value = yearPicked

            bindingDMYPicker.dmypMonth.displayedValues = Globals.monthsShortArray

            bindingDMYPicker.dmypDay.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            bindingDMYPicker.dmypMonth.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    bindingDMYPicker.dmypDay.maxValue = 30
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYPicker.dmypYear.value % 4 == 0)) {
                    bindingDMYPicker.dmypDay.maxValue = 29
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYPicker.dmypYear.value % 4 != 0)) {
                    bindingDMYPicker.dmypDay.maxValue = 28
                    bindingDMYPicker.dmypDay.minValue = 1
                } else {
                    bindingDMYPicker.dmypDay.maxValue = 31
                    bindingDMYPicker.dmypDay.minValue = 1
                }
                monthPicked = newVal
            }

            bindingDMYPicker.dmypYear.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && bindingDMYPicker.dmypMonth.value == 2) {
                    bindingDMYPicker.dmypDay.maxValue = 29
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal % 4 != 0 && bindingDMYPicker.dmypMonth.value == 2) {
                    bindingDMYPicker.dmypDay.maxValue = 28
                    bindingDMYPicker.dmypDay.minValue = 1
                }
                yearPicked = newVal
            }

            bindingDMYPicker.submitDmy.setOnClickListener {
                bindingAddValuation.date.text = "$dayPicked ${Globals.getShortMonth(monthPicked+1)} $yearPicked"
                changeDateDialog.dismiss()
            }

            bindingDMYPicker.dmypDay.wrapSelectorWheel = true
            bindingDMYPicker.dmypMonth.wrapSelectorWheel = true
            bindingDMYPicker.dmypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        bindingAddValuation.tvAdd.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked - 1, dayPicked)

            val value = bindingAddValuation.etValue.text.toString()
            val date = calendar.timeInMillis.toString()

            if (value.isNotEmpty()) {

                val valuationHandler = ValuationHandler(this, null)

                valuationHandler.addValuation(ValuationModel(0, Globals.alSelected, value, date))

                Toast.makeText(this, "Valuation added.", Toast.LENGTH_LONG).show()

                setUpValuationList(Globals.alSelected)
                valuationHandler.close()
                addDialog.dismiss()

            } else {
                Toast.makeText(this, "Name or value can't be blank.", Toast.LENGTH_LONG)
                        .show()
            }

        }
        addDialog.show()

        bindingAddValuation.tvCancel.setOnClickListener {
            addDialog.dismiss()
        }
    }

    fun updateValuation(valuation: ValuationModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        bindingUpdateValuation = DialogUpdateValuationBinding.inflate(layoutInflater)
        val view = bindingUpdateValuation.root
        updateDialog.setContentView(view)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cal = Calendar.getInstance()
        cal.timeInMillis = valuation.date.toLong()
        var dayPicked = cal.get(Calendar.DAY_OF_MONTH)
        var monthPicked = cal.get(Calendar.MONTH)
        var yearPicked = cal.get(Calendar.YEAR)

        bindingUpdateValuation.date.text = "$dayPicked ${Globals.getShortMonth(monthPicked+1)} $yearPicked"

        bindingUpdateValuation.etValue.setText(valuation.value)

        bindingUpdateValuation.date.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            bindingDMYPicker = DayMonthYearPickerBinding.inflate(layoutInflater)
            val viewDMYP = bindingDMYPicker.root
            changeDateDialog.setContentView(viewDMYP)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 4 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 6 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 9 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 11) {
                bindingDMYPicker.dmypDay.maxValue = 30
                bindingDMYPicker.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 == 0)) {
                bindingDMYPicker.dmypDay.maxValue = 29
                bindingDMYPicker.dmypDay.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 != 0)) {
                bindingDMYPicker.dmypDay.maxValue = 28
                bindingDMYPicker.dmypDay.minValue = 1
            } else {
                bindingDMYPicker.dmypDay.maxValue = 31
                bindingDMYPicker.dmypDay.minValue = 1
            }

            bindingDMYPicker.dmypMonth.maxValue = 12
            bindingDMYPicker.dmypMonth.minValue = 1
            bindingDMYPicker.dmypYear.maxValue = 2999
            bindingDMYPicker.dmypYear.minValue = 1000

            bindingDMYPicker.dmypDay.value = dayPicked
            bindingDMYPicker.dmypMonth.value = monthPicked
            bindingDMYPicker.dmypYear.value = yearPicked

            bindingDMYPicker.dmypMonth.displayedValues = Globals.monthsShortArray

            bindingDMYPicker.dmypDay.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            bindingDMYPicker.dmypMonth.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    bindingDMYPicker.dmypDay.maxValue = 30
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYPicker.dmypYear.value % 4 == 0)) {
                    bindingDMYPicker.dmypDay.maxValue = 29
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal == 2 && (bindingDMYPicker.dmypYear.value % 4 != 0)) {
                    bindingDMYPicker.dmypDay.maxValue = 28
                    bindingDMYPicker.dmypDay.minValue = 1
                } else {
                    bindingDMYPicker.dmypDay.maxValue = 31
                    bindingDMYPicker.dmypDay.minValue = 1
                }
                monthPicked = newVal
            }

            bindingDMYPicker.dmypYear.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && bindingDMYPicker.dmypMonth.value == 2) {
                    bindingDMYPicker.dmypDay.maxValue = 29
                    bindingDMYPicker.dmypDay.minValue = 1
                } else if (newVal % 4 != 0 && bindingDMYPicker.dmypMonth.value == 2) {
                    bindingDMYPicker.dmypDay.maxValue = 28
                    bindingDMYPicker.dmypDay.minValue = 1
                }
                yearPicked = newVal
            }

            bindingDMYPicker.submitDmy.setOnClickListener {
                bindingUpdateValuation.date.text = "$dayPicked ${Globals.getShortMonth(monthPicked+1)} $yearPicked"
                changeDateDialog.dismiss()
            }

            bindingDMYPicker.dmypDay.wrapSelectorWheel = true
            bindingDMYPicker.dmypMonth.wrapSelectorWheel = true
            bindingDMYPicker.dmypYear.wrapSelectorWheel = true

            changeDateDialog.show()

        }

        bindingUpdateValuation.tvUpdate.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.set(yearPicked, monthPicked, dayPicked)

            val value = bindingUpdateValuation.etValue.text.toString()
            val date = calendar.timeInMillis.toString()

            if (value.isNotEmpty()) {

                val valuationHandler = ValuationHandler(this, null)

                valuationHandler.updateValuation(ValuationModel(valuation.id, Globals.alSelected, value, date))

                Toast.makeText(this, "Valuation updated.", Toast.LENGTH_LONG).show()

                setUpValuationList(Globals.alSelected)
                valuationHandler.close()
                updateDialog.dismiss()

            } else {
                Toast.makeText(this, "Name or value can't be blank.", Toast.LENGTH_LONG)
                        .show()
            }

        }
        updateDialog.show()

        bindingUpdateValuation.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }
    }

    fun deleteValuation(valuation: ValuationModel) {
        val deleteDialog = Dialog(this, R.style.Theme_Dialog)
        deleteDialog.setCancelable(false)
        bindingDeleteValuation = DialogDeleteValuationBinding.inflate(layoutInflater)
        val view = bindingDeleteValuation.root
        deleteDialog.setContentView(view)
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        bindingDeleteValuation.tvDelete.setOnClickListener {
            val valuationHandler = ValuationHandler(this, null)
            valuationHandler.deleteValuation(ValuationModel(valuation.id, 0, "", ""))

            Toast.makeText(this, "Valuation deleted.", Toast.LENGTH_LONG).show()

            setUpValuationList(Globals.alSelected)
            valuationHandler.close()
            deleteDialog.dismiss()
        }

        bindingDeleteValuation.tvCancel.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()

    }

}