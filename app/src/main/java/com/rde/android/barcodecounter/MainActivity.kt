package com.rde.android.barcodecounter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader


class MainActivity : AppCompatActivity(), BarcodeAdapter.IdListItemEdit {

    private val lstBarcode = ArrayList<RowData>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //populateSampledata()
        rviewBarcodes.layoutManager = LinearLayoutManager(this)
        rviewBarcodes.adapter = BarcodeAdapter(lstBarcode, this)

        edtBarcode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val pepe = p0.toString();
                if (pepe.endsWith('\n')) {
                    this@MainActivity.edtBarcode.setText("");
                    val abarcode = pepe.substringBefore('\n')
                    hideKeyboard()
                    this@MainActivity.processbarcode(abarcode)

                }
            }

            override fun afterTextChanged(p0: Editable?) {


            }

        })

        btnDelete.setOnClickListener(View.OnClickListener { deleteData() })
        checkAndRequestPermissions()
    }

    fun deleteData()
    {
        val fragmentManager = getSupportFragmentManager();
        val dlg = ConfirmationDlg.newInstance("Are you sure you want to delete the whole list?", 1);
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

    private fun processbarcode(barcode: String)
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

    fun getAll()
    {
        getDataFromFile(FILE_NAME)
    }

    fun getDataFromFile(fileName: String)
    {
        lstBarcode.clear()
        try {
            val fileInputStream: FileInputStream = openFileInput(fileName)
            val reader = BufferedReader(InputStreamReader(fileInputStream))
            val sb = StringBuffer()
            var line: String = reader.readLine()
            while (line != null) {
                sb.append(line)
                val fields = line.split(';')
                if(fields.size > 1)
                {
                    val rowData = RowData()
                    rowData.barcode = fields[0]
                    var aqty : Int = 0
                    try {
                      aqty = fields[1].toInt()
                    } catch (ee : java.lang.Exception){
                        aqty = 0
                    }
                    rowData.qty = aqty
                    lstBarcode.add(rowData)
                }
                line = reader.readLine()
            }
            rviewBarcodes.adapter?.notifyDataSetChanged();
         } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    fun saveAll() {
        val stringBuilder: StringBuilder = StringBuilder()
        for (i in lstBarcode.indices)
        {
            val rowData = lstBarcode[i]
            val aline = rowData.barcode.replace(";", ",") + ";" + rowData.qty.toString() + System.getProperty(
                "line.separator"
            );
            stringBuilder.append(aline)
        }

        val content = stringBuilder.toString()
        createUpdateFile(FILE_NAME, content, false)
    }

    private fun createUpdateFile(fileName: String, content: String, update: Boolean) {
        val outputStream: FileOutputStream
        try {
            outputStream = if (update) {
                openFileOutput(fileName, MODE_APPEND)
            } else {
                openFileOutput(fileName, MODE_PRIVATE)
            }
            outputStream.write(content.toByteArray())
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        getAll()
        hideKeyboard()
    }

    override fun onPause() {
        super.onPause()
        saveAll()
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

    private fun showMessage(msg: String) {
        Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG).show()
        //Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }

    val REQUEST_ID_MULTIPLE_PERMISSIONS = 1

    private fun checkAndRequestPermissions(): Boolean {
        val camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storage =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val storageRead =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val loc =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val loc2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
       // val serperm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
        }
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        //if (serperm != PackageManager.PERMISSION_GRANTED) {
        //    listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        // }
        if (camera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (storageRead != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_ID_MULTIPLE_PERMISSIONS
            )
            return false
        }
        return true
    }


    companion object
    {
        const val FILE_NAME = "barcodes.csv"
        const val EXPORT_FILE = "download.txt"
    }

    override fun itemEdit(index: Int) {
        //TODO("Not yet implemented")
    }

    override fun itemDelete(index: Int) {
        if(index >= 0 && index < lstBarcode.size) {
            lstBarcode.removeAt(index)
            this@MainActivity.rviewBarcodes.adapter?.notifyDataSetChanged();
        }
    }

}