package com.rde.android.barcodecounter

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val lstBarcode = ArrayList<RowData>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        populateSampledata()
        rviewBarcodes.layoutManager = LinearLayoutManager(this)
        rviewBarcodes.adapter = BarcodeAdapter(lstBarcode, this)

        edtBarcode.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val pepe = p0.toString();
                if(pepe.endsWith('\n'))
                {
                     this@MainActivity.edtBarcode.setText("") ;
                     val abarcode = pepe.substringBefore('\n')
                     hideKeyboard()
                     this@MainActivity.processbarcode(abarcode)

                }
            }

            override fun afterTextChanged(p0: Editable?) {


            }

        })

        btnDelete.setOnClickListener(View.OnClickListener { deleteData() })
    }

    fun deleteData()
    {
        val fragmentManager = getSupportFragmentManager();
        val dlg = ConfirmationDlg.newInstance("Are you sure you want to delete the whole list?" , 1);
        dlg.show(fragmentManager, "iDDConfirmationlDlg")
        dlg.idConfirmationListener = object : ConfirmationDlg.IdConfirmDlgListener {
            override fun onConfirm(itemIndex: Int) {
                this@MainActivity.lstBarcode.clear();
                this@MainActivity.rviewBarcodes.adapter?.notifyDataSetChanged();


            }

        }
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edtBarcode.getWindowToken(), 0);
    }

    private fun processbarcode(barcode : String)
    {
        var bfound = false
        if(barcode.length < 3)
            return
        for (i in lstBarcode.indices)
        {
            val rowData = lstBarcode[i]
            if(rowData.barcode.equals(barcode) )
            {
                rowData.qty = rowData.qty + 1
                rviewBarcodes.adapter?.notifyItemChanged(i)
                bfound = true
                showMessage(barcode + " qty: " + rowData.qty.toString());

                break
            }
        }

        if(!bfound)
        {
            val newRowData = RowData(barcode, 1)
            lstBarcode.add(newRowData)
            rviewBarcodes.adapter?.notifyDataSetChanged()
            showMessage(barcode + " qty: " + newRowData.qty.toString());
        }


    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    private fun populateSampledata()
    {
        lstBarcode.clear()
        lstBarcode.add(RowData("111111111", 3))
        lstBarcode.add(RowData("111111112", 2))
        lstBarcode.add(RowData("111111113", 36))
        lstBarcode.add(RowData("111111114", 300))
        lstBarcode.add(RowData("111111115", 3000))
        lstBarcode.add(RowData("111111116", 32))
        lstBarcode.add(RowData("111111117", 39))
    }

    private fun showMessage(msg:String) {
        Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG).show()
        //Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }

}