package jp.cycle_check

import java.io.Serializable
import java.util.Date
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Task : RealmObject(), Serializable {
    var key:String=""
    var distance:Int=0 // 走行距離
    var cycle_name: String = ""      // 自転車名
    var title:String=""
    var cycle_time:Int=0 //走行時間
    var cycle_uid:String=""
    var contents: String = ""   // 内容
    var date: Date = Date()     // 日時
    // id をプライマリーキーとして設定
    @PrimaryKey
    var id: Int = 0
}