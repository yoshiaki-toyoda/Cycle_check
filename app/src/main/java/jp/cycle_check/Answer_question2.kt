package jp.cycle_check


import java.io.Serializable
import java.util.ArrayList

class Answer_question2(val cycle_name: String, val shop_ID: String, val name: String, val uid: String,val com:String,val distance:String, val cycleUid:String,val answer_type:String, val old_type:String,val other_request:String, val part:String,val problem:String,val report_type:String,val title:String,val yosan:String,val area:String,bytes: ByteArray,val cause:String,val com2: String,val contact:String,val estimate:String,val question_cyclruid:String,result:String) ://val answers: ArrayList<Parts>
    Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}