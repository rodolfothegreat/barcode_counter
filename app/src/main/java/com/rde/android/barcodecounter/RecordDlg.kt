package com.rde.android.barcodecounter

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.shawnlin.numberpicker.NumberPicker

class RecordDlg : DialogFragment() {

    init {
        retainInstance = true
    }

    private var btnRecOk : Button? = null
    private var btnRecCancel : Button? = null
    private var tvDlgBarcode : TextView? = null
    private var number_picker : NumberPicker? = null

    var idSaveDlgListener : IdSaveDlgListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroyView() {
        val _dialog = dialog
        if (_dialog != null && getRetainInstance()) {
            _dialog.setDismissMessage(null);
        }
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.record_dlg, container);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnRecCancel = view.findViewById(R.id.btnRecCancel)
        btnRecCancel?.setOnClickListener { dismiss() }

        tvDlgBarcode = view.findViewById(R.id.tvDlgBarcode)
        tvDlgBarcode?.text = arguments?.getString(ID_BARCODE, "")

        number_picker = view.findViewById(R.id.number_picker)
        number_picker?.value = requireArguments().getInt(ID_QTY, 0)

        btnRecOk = view.findViewById(R.id.btnRecOk)
        btnRecOk?.setOnClickListener {
            val newQty = number_picker?.value
            if(newQty != null)
                idSaveDlgListener?.onsave(newQty)
            dismiss()
        }



    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE);
        return dialog
    }
    override fun onResume() {
        super.onResume()
        val params = dialog?.window?.attributes
        params?.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        dialog?.window?.attributes = params
    }

    interface IdSaveDlgListener {
        fun onsave(qty: Int)
    }


    companion object {
        const val COMPANION_OBJECT_NUMBER = 40
        const val ID_BARCODE = "id_barcode";
        const val ID_QTY = "id_qty"
        fun newInstance(abarcode: String, aqty: Int) : RecordDlg
        {
            val dlg = RecordDlg()
            val abundle = Bundle()
            abundle.putString(ID_BARCODE, abarcode)
            abundle.putInt(ID_QTY, aqty)
            dlg.setArguments(abundle)
            return dlg
        }
    }


}