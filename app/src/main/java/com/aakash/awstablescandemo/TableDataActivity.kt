package com.aakash.awstablescandemo

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_table_data.*

class TableDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_data)

        val gson = Gson()
        val modal = gson.fromJson(intent.getStringExtra("TABLEDATA"),
            ArrayModal::class.java)
        modal.array = modal.getArray()
        val list = ArrayList(modal.array)
        Log.i("TABLEDATANEW", list.toString())
        if (list.size > 0) {
            val columnSize = list[0].size
            for (i in list.indices) {
                val columns = list[i]
                val row1 = TableRow(this@TableDataActivity)
//                row1.setPadding(5, 20, 5, 20)
                row1.setBackgroundResource(R.drawable.cellshape)
                for (j in list[i].indices) {
                    val txt = TextView(this@TableDataActivity)
                    txt.setPadding(5, 20, 5, 20)
                    txt.setBackgroundResource(R.drawable.cellshape)
                    if (columnSize >= 4) txt.setTextSize(TypedValue.COMPLEX_UNIT_PT,
                        4f) else txt.setTextSize(TypedValue.COMPLEX_UNIT_PT, 6f)
                    txt.setTypeface(Typeface.SERIF, Typeface.BOLD)
                    txt.gravity = Gravity.CENTER
                    if(columns[j] =="NOT_SELECTED") txt.text = ""
                    else txt.text = columns[j]
                    txt.setTextColor(Color.BLACK)
                    row1.addView(txt)
                }
                tableInvoices!!.addView(row1)
                tableInvoices!!.weightSum = 1f
            }
        }
    }
}