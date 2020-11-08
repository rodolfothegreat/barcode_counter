package com.rde.android.barcodecounter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_row.view.*

class BarcodeAdapter (val items : ArrayList<RowData>, val context: Context) : RecyclerView.Adapter<BarcodeAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return BarcodeAdapter.MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.list_item_row, parent, false)
        )
    }


    override fun getItemCount(): Int {
        return items.size
    }

    class MyViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
       // val tvAnimalType = view.tv_animal_type
        val txtRowBarcode = view.txtRowBarcode
        val txtRowQty  = view.txtRowQty
        val btnEdit = view.btnRowEdit
        val btnDelete = view.btnRowDelete
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if(position >= getItemCount())
        {
            return
        }
        val rowData = items[position]
        holder.txtRowBarcode.text = rowData.barcode
        holder.txtRowQty.text = rowData.qty.toString()

    }

}