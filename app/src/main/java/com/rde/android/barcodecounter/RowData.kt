package com.rde.android.barcodecounter

class RowData() {
    public var barcode : String = ""
    public var qty : Int = 0

    constructor (_barcode : String, _qty : Int) : this()
    {
        barcode = _barcode
        qty = _qty
    }

    fun toCsvString() : String
    {
        return barcode.replace(",", ";")  + "," + qty.toString()
    }
}