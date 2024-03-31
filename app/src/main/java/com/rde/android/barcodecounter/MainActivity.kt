package com.rde.android.barcodecounter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
//import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.net.URLConnection
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), BarcodeAdapter.IdListItemEdit {

    private val lstBarcode = ArrayList<RowData>()
    internal var qrScanIntegrator: IntentIntegrator? = null
    private var rviewBarcodes : RecyclerView? = null
    private var edtBarcode : EditText? = null
    private var btnDelete : Button? = null
    private var btnShare : Button? = null
    private var btnExport : Button? = null
    private var btnCamera : ImageButton? = null
    private var rootLayout : ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //populateSampledata()

        rootLayout = findViewById(R.id.rootLayout)
        rviewBarcodes = findViewById(R.id.rviewBarcodes)

        rviewBarcodes?.layoutManager = LinearLayoutManager(this)
        rviewBarcodes?.adapter = BarcodeAdapter(lstBarcode, this)
        qrScanIntegrator = IntentIntegrator(this)

        edtBarcode = findViewById(R.id.edtBarcode)
        btnDelete = findViewById(R.id.btnDelete)
        btnShare = findViewById(R.id.btnShare)
        btnCamera = findViewById(R.id.btnCamera)
        btnExport = findViewById(R.id.btnExport)
        edtBarcode!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val pepe = p0.toString();
                if (pepe.endsWith('\n')) {
                    this@MainActivity.edtBarcode!!.setText("");
                    val abarcode = pepe.substringBefore('\n')
                    hideKeyboard()
                    this@MainActivity.processbarcode(abarcode)

                }
            }

            override fun afterTextChanged(p0: Editable?) {


            }

        })


        btnDelete!!.setOnClickListener(View.OnClickListener { deleteData() })
        checkAndRequestPermissions()

        btnShare!!.setOnClickListener {
            saveAll();
            shareFile(FILE_NAME);
        }

        btnExport!!.setOnClickListener{
            exportFile()
        }

        btnCamera!!.setOnClickListener(object :  View.OnClickListener {
            override fun onClick(v: View?) {
                saveAll()
                qrScanIntegrator?.initiateScan()
            }

        })

    }

    private fun showAbout()
    {
        AboutBox.Show(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val  inflater = getMenuInflater()
        inflater.inflate(R.menu.main_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item!!.itemId) {
            R.id.miAbout -> {
                showAbout()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            // If QRCode has no data.
            if (result.contents == null) {
                Toast.makeText(this, "Barcode not found", Toast.LENGTH_LONG).show()
            } else {
                // If QRCode contains data.
                 Snackbar.make(rootLayout!!,result.contents, Snackbar.LENGTH_LONG).show()
                getAll()
                processbarcode(result.contents)
                saveAll()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



    fun deleteData()
    {
        val fragmentManager = getSupportFragmentManager();
        val dlg = ConfirmationDlg.newInstance("Are you sure you want to delete the whole list?", 1);
        dlg.show(fragmentManager, "iDDConfirmationlDlg")
        dlg.idConfirmationListener = object : ConfirmationDlg.IdConfirmDlgListener {
            override fun onConfirm(itemIndex: Int) {
                this@MainActivity.lstBarcode.clear();
                this@MainActivity.rviewBarcodes?.adapter?.notifyDataSetChanged();


            }

        }
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edtBarcode!!.getWindowToken(), 0);
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
                rviewBarcodes!!.adapter?.notifyItemChanged(i)
                bfound = true
                showMessage(barcode + " qty: " + rowData.qty.toString());

                break
            }
        }

        if(!bfound)
        {
            val newRowData = RowData(barcode, 1)
            lstBarcode.add(newRowData)
            rviewBarcodes!!.adapter?.notifyDataSetChanged()
            showMessage(barcode + " qty: " + newRowData.qty.toString());
        }


    }

    fun getAll()
    {
        getDataFromFile_1(FILE_NAME)
    }

    private fun saveFile(csvFileName: String): Boolean {
        val root = getExternalFilesDir(null)
        val rootPath = root!!.absolutePath
        val afilename = rootPath + File.separator + csvFileName
        //String afilename = csvFileName;
        if (lstBarcode == null || lstBarcode.size === 0) {
            val afile = File(afilename)
            if (afile.exists()) {
                afile.delete()
            }
            return false
        }
        val afile = File(afilename)
        var fileWriter: FileWriter? = null
        var br: BufferedWriter? = null
        Log.d(TAG, "filename: $csvFileName")
        return try {
            fileWriter = FileWriter(afilename, false) //overwrites file
            br = BufferedWriter(fileWriter)
            for (i in 0 until lstBarcode.size) {
                val anObj = lstBarcode.get(i)
                br.write(anObj.toCsvString().toString() + System.getProperty("line.separator"))
            }
            true
        } catch (e: IOException) {
            val newFragment =
                WrongLoginDlg("Could not create write file " + afilename + " " + e.message)
            newFragment.show(supportFragmentManager, "loginDlg")
            false
        } finally {
            try {
                br?.close()
                fileWriter?.close()
            } catch (e: java.lang.Exception) {
            }
        }
    }

    private fun getDataFromFile_1(csvFileName : String) {
        if (csvFileName == null || csvFileName.equals("")) return
        val root = getExternalFilesDir(null)
        val rootPath = root!!.absolutePath
        val afilename = rootPath + File.separator + csvFileName
        //String afilename = csvFileName;
        val afile = File(afilename)
        if (!afile.exists()) return
        var aline = ""
        var acount = 0
        var bufferReader: BufferedReader? = null
        try {
            bufferReader = BufferedReader(FileReader(afilename))
            lstBarcode.clear()
            while (bufferReader.readLine().also { aline = it } != null) {
                acount++
                //if(acount == 1)
                //    continue;
                val theFields = aline.split(",".toRegex()).toTypedArray()
                if (theFields.size < 2) {
                    continue
                }
                val anObj = RowData()
                lstBarcode.add(anObj)
                anObj.barcode = theFields[0]
                var aqty = 0
                try {
                    aqty = theFields[1].toInt()
                } catch (ee: java.lang.Exception) {
                }
                anObj.qty = aqty
            }
            rviewBarcodes!!.adapter?.notifyDataSetChanged();
        } catch (ex: Exception) {
            Log.e(TAG, "Error reading file " + afilename + " " + ex.message)
        } finally {
            try {
                bufferReader?.close()
            } catch (exy: IOException) {
                Log.e(TAG, " " + exy.message)
            }
        }
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
                val fields = line.split(',')
                if(fields.size > 1)
                {
                    val rowData = RowData()
                    rowData.barcode = fields[0]
                    var aqty : Int = 0
                    try {
                      aqty = fields[1].toInt()
                    } catch (ee: java.lang.Exception){
                        aqty = 0
                    }
                    rowData.qty = aqty
                    lstBarcode.add(rowData)
                }
                line = reader.readLine()
            }
            rviewBarcodes!!.adapter?.notifyDataSetChanged();
         } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    fun saveAll() {
        /*
        val stringBuilder: StringBuilder = StringBuilder()
        for (i in lstBarcode.indices)
        {
            val rowData = lstBarcode[i]
            val aline = rowData.barcode.replace(",", ";") + "," + rowData.qty.toString() + System.getProperty(
                "line.separator"
            );
            stringBuilder.append(aline)
        }

        val content = stringBuilder.toString()
        val root = getExternalFilesDir(null)
        val rootPath = root!!.absolutePath

        val afilename: String = rootPath + File.separator + FILE_NAME
        createUpdateFile(FILE_NAME, content, false)

         */

        saveFile(FILE_NAME)

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

    private fun exportFile()
    {
        val docs = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS
            ), "barcodecounter"
        )



// Make the directory if it does not yet exist
// Make the directory if it does not yet exist
        try {
            docs.mkdirs()
            saveAll()
            val root = getExternalFilesDir(null)
            val rootPath = root!!.absolutePath

            val afilsourcename = rootPath + File.separator + FILE_NAME
            //String afilename = csvFileName;
            val afile = File(afilsourcename)


            val afilename: String = docs.absolutePath + File.separator + FILE_NAME

            val afileDest = File(afilename)
            if(afileDest.exists())
            {
                afileDest.delete()
            }
            afile.copyTo(afileDest)
            Snackbar.make(
                rootLayout!!,
                "File copied to " + docs.absolutePath + File.separator + FILE_NAME,
                Snackbar.LENGTH_LONG
            ).show()
        } catch (E: Exception)
        {
            Snackbar.make(rootLayout!!, "Could not export file. " + E.message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun shareFile(filename: String) {

        val root = getExternalFilesDir(null)
        val rootPath = root!!.absolutePath

        val afilename: String = rootPath + File.separator + filename


        val file = File(afilename)
        val intentShareFile = Intent(Intent.ACTION_SEND)

        // Uri apkURI = FileProvider.getUriForFile(ListActivity.this,
        //        BuildConfig.APPLICATION_ID + ".provider",
        //         file);
        val apkURI: Uri =
            FileProvider.getUriForFile(applicationContext, "$packageName.provider", file)
        intentShareFile.type = URLConnection.guessContentTypeFromName(file.getName())
        intentShareFile.putExtra(Intent.EXTRA_STREAM, apkURI)

        //if you need
        //intentShareFile.putExtra(Intent.EXTRA_SUBJECT,"Sharing File Subject);
        //intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File Description");
        startActivity(Intent.createChooser(intentShareFile, "Share File"))
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
        Snackbar.make(rootLayout!!, msg, Snackbar.LENGTH_LONG).show()
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
        const val TAG = "MainActivity"
    }

    override fun itemEdit(index: Int) {
        if(index < 0 || index >= lstBarcode.size)
            return;

        val rowData = lstBarcode[index]
        val fragmentManager = getSupportFragmentManager();
        val dlg = RecordDlg.newInstance(rowData.barcode, rowData.qty);
        dlg.show(fragmentManager, "iDDConfirmationlDlg")
        dlg.idSaveDlgListener = object : RecordDlg.IdSaveDlgListener{
            override fun onsave(qty: Int) {
                rowData.qty = qty
                this@MainActivity.rviewBarcodes!!.adapter?.notifyItemChanged(index)
            }

        }

    }

    override fun itemDelete(index: Int) {
        if(index < 0 || index >= lstBarcode.size)
            return;
        val rowData = lstBarcode[index]
        val fragmentManager = getSupportFragmentManager();
        val dlg = ConfirmationDlg.newInstance(
            "Are you sure you want to delete the barcode " + rowData.barcode + "?",
            1
        );
        dlg.show(fragmentManager, "iDDConfirmationlDlg")
        dlg.idConfirmationListener = object : ConfirmationDlg.IdConfirmDlgListener {
            override fun onConfirm(itemIndex: Int) {
                lstBarcode.removeAt(index)
                this@MainActivity.rviewBarcodes!!.adapter?.notifyDataSetChanged();
            }

        }





    }

}