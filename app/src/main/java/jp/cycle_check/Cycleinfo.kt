package jp.cycle_check

import java.io.Serializable
import java.util.ArrayList

    class Cycleinfo(val cycle_name: String, val shop_ID: String, val name: String, val uid: String,val com:String, val alert:String, val report:String,val distance:String, val cycleUid:String, val type:String,val date:String, bytes: ByteArray) ://val answers: ArrayList<Parts>
        Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}