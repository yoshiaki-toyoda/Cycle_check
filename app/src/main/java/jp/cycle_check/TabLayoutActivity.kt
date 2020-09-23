package jp.cycle_check

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import jp.cycle_check.ui.Fragment.CycleFragment
import jp.cycle_check.ui.Fragment.Tab01Fragment
import jp.cycle_check.ui.Fragment.Tab02Fragment
import kotlinx.android.synthetic.main.activity_partresister.*
import kotlinx.android.synthetic.main.activity_tab_layout.*
import kotlinx.android.synthetic.main.app_header_main.*
import kotlinx.android.synthetic.main.fragment_tab_01.*
import java.util.ArrayList

class TabLayoutActivity : AppCompatActivity(),Tab01Fragment.FragmentListener,Tab02Fragment.FragmentListener{

    var xCycle: Answer_question2? = null
    var yCycle: Questioninfo?=null
    var mCycle: Answer?=null
    var A_type=0
    private lateinit var mDatabaseReference: DatabaseReference
    private var mCycleRef: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        xCycle = null
        setContentView(R.layout.activity_tab_layout)
        val extras = intent.extras
        val type= extras.get("Q_type")?:""
        //Firebase情報
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        if(type==0){
            yCycle = extras.get("Q_Ainfo") as Questioninfo
            getcycle()
        }else if(type==2){
             A_type = extras.get("A_type")as Int
            mCycle = extras.get("Q_Ainfo") as Answer
            getquestion()
        }else if(type==5){
            yCycle = extras.get("Q_Ainfo") as Questioninfo
            getshopcycle()
        }
        else{
            xCycle = extras.get("Q_Ainfo") as Answer_question2
            conect(xCycle!!,1)
        }
    }


    fun conect(xCycle:Answer_question2,type:Int){
        pager.adapter = TabAdapter(supportFragmentManager,xCycle,type)
        tab_layout.setupWithViewPager(pager)
    }

    fun getshopcycle(){
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        var user = FirebaseAuth.getInstance().currentUser!!.uid
        val cycleRef = mDatabaseReference.child("report").child("Answer").child("shop").child(yCycle!!.shop_ID).child(yCycle!!.cycleUid)
        cycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value!=null) {
                    val map = snapshot.value as kotlin.collections.Map<String, String>
                    var cause = map["cause"] ?: ""
                    var com2 = map["com"] ?: ""
                    var contact = map["contact"] ?: ""
                    var estimate = map["estimate"] ?: ""
                    var question_cycleuid = map["question_cycleuid"] ?: ""
                    var result = map["result"] ?: ""
                    val imageString=map["image"]?:""
                    val Anbytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }
                    val cycle_name=yCycle!!.cycle_name
                    val shop_ID=yCycle!!.shop_ID
                    val name=yCycle!!.name
                    val uid=yCycle!!.uid
                    val com=yCycle!!.com
                    val distance=yCycle!!.distance
                    val cycleUid=yCycle!!.cycleUid
                    val answer_type=yCycle!!.answer_type
                    val old_type=yCycle!!.old_type
                    val other_request=yCycle!!.other_request
                    val part=yCycle!!.part
                    val problem=yCycle!!.problem
                    val report_type=yCycle!!.report_type
                    val title=yCycle!!.title
                    val yosan=yCycle!!.yosan
                    val area=yCycle!!.area
                    val bytes=yCycle!!.imageBytes
                    val day=yCycle!!.date

                    xCycle=Answer_question2( cycle_name,  shop_ID,  name, uid, com,distance,cycleUid,answer_type, old_type,other_request,part,problem,report_type, title,yosan,area,bytes,cause,com2,contact,estimate,question_cycleuid,result,day,Anbytes)
                    //回答情報から質問を読み込む

                }else{
                    val cycle_name=yCycle!!.cycle_name
                    val shop_ID=yCycle!!.shop_ID
                    val name=yCycle!!.name
                    val uid=yCycle!!.uid
                    val com=yCycle!!.com
                    val distance=yCycle!!.distance
                    val cycleUid=yCycle!!.cycleUid
                    val answer_type=yCycle!!.answer_type
                    val old_type=yCycle!!.old_type
                    val other_request=yCycle!!.other_request
                    val part=yCycle!!.part
                    val problem=yCycle!!.problem
                    val report_type=yCycle!!.report_type
                    val title=yCycle!!.title
                    val yosan=yCycle!!.yosan
                    val area=yCycle!!.area
                    val bytes=yCycle!!.imageBytes
                    val day=yCycle!!.date

                    var cause =  ""
                    var com2 =  ""
                    var contact = ""
                    var estimate =  ""
                    var question_cycleuid =  ""
                    var result = ""
                    val imageString=""
                    val Anbytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }
                    xCycle=Answer_question2( cycle_name,  shop_ID,  name, uid, com,distance,cycleUid,answer_type, old_type,other_request,part,problem,report_type, title,yosan,area,bytes,cause,com2,contact,estimate,question_cycleuid,result,day,Anbytes)
                    //回答情報から質問を読み込む
                }
                conect(xCycle!!,5)
            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })
    }






    fun getcycle(){
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        var user = FirebaseAuth.getInstance().currentUser!!.uid
        val cycleRef = mDatabaseReference.child("report").child("Answer").child(user).child(yCycle!!.cycleUid)
        cycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value!=null) {
                    val map = snapshot.value as kotlin.collections.Map<String, String>
                    var cause = map["cause"] ?: ""
                    var com2 = map["com"] ?: ""
                    var contact = map["contact"] ?: ""
                    var estimate = map["estimate"] ?: ""
                    var question_cycleuid = map["question_cycleuid"] ?: ""
                    var result = map["result"] ?: ""
                    val imageString=map["image"]?:""
                    val Anbytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }
                    val cycle_name=yCycle!!.cycle_name
                    val shop_ID=yCycle!!.shop_ID
                    val name=yCycle!!.name
                    val uid=yCycle!!.uid
                    val com=yCycle!!.com
                    val distance=yCycle!!.distance
                    val cycleUid=yCycle!!.cycleUid
                    val answer_type=yCycle!!.answer_type
                    val old_type=yCycle!!.old_type
                    val other_request=yCycle!!.other_request
                    val part=yCycle!!.part
                    val problem=yCycle!!.problem
                    val report_type=yCycle!!.report_type
                    val title=yCycle!!.title
                    val yosan=yCycle!!.yosan
                    val area=yCycle!!.area
                    val bytes=yCycle!!.imageBytes
                    val day=yCycle!!.date

                    xCycle=Answer_question2( cycle_name,  shop_ID,  name, uid, com,distance,cycleUid,answer_type, old_type,other_request,part,problem,report_type, title,yosan,area,bytes,cause,com2,contact,estimate,question_cycleuid,result,day,Anbytes)
                    //回答情報から質問を読み込む

                }else{
                    val cycle_name=yCycle!!.cycle_name
                    val shop_ID=yCycle!!.shop_ID
                    val name=yCycle!!.name
                    val uid=yCycle!!.uid
                    val com=yCycle!!.com
                    val distance=yCycle!!.distance
                    val cycleUid=yCycle!!.cycleUid
                    val answer_type=yCycle!!.answer_type
                    val old_type=yCycle!!.old_type
                    val other_request=yCycle!!.other_request
                    val part=yCycle!!.part
                    val problem=yCycle!!.problem
                    val report_type=yCycle!!.report_type
                    val title=yCycle!!.title
                    val yosan=yCycle!!.yosan
                    val area=yCycle!!.area
                    val bytes=yCycle!!.imageBytes
                    val day=yCycle!!.date

                    var cause =  ""
                    var com2 =  ""
                    var contact = ""
                    var estimate =  ""
                    var question_cycleuid =  ""
                    var result = ""
                    val imageString=""
                    val Anbytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }
                    xCycle=Answer_question2( cycle_name,  shop_ID,  name, uid, com,distance,cycleUid,answer_type, old_type,other_request,part,problem,report_type, title,yosan,area,bytes,cause,com2,contact,estimate,question_cycleuid,result,day,Anbytes)
                    //回答情報から質問を読み込む
                }
                conect(xCycle!!,0)
            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })
    }

    fun getquestion(){
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        var user = FirebaseAuth.getInstance().currentUser!!.uid
        val cycleRef = mDatabaseReference.child("report").child("All").child(mCycle!!.question_cyclruid)
        cycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value!=null) {
                    val map = snapshot.value as Map<String, String>
                    var cycle_name = map["cycle_name"] ?: ""
                    var shop_ID = map["shop_ID"] ?: ""
                    var name = map["name"] ?: ""
                    var uid = map["uid"] ?: ""
                    var com:String=map["com"]?:""
                    var imageString = map["image"] ?: ""
                    var distance = map["distance"] ?: "0"
                    var answer_type=map["answer_type"]?:""
                    var old_type=map["old_type"]?:""
                    var other_request=map["other_request"]?:""
                    var part=map["part"]?:""
                    var problem=map["problem"]?:""
                    var report_type=map["report_type"]?:""
                    var title=map["title"]?:""
                    var yosan=map["yosan"]?:""
                    var cycleUid=map["cycle_uid"]?:""
                    var area=map["area"]?:""
                    var alert=""
                    var day=map["day"]?:""
                    var report=report_type
                    var bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    val question =Questioninfo( cycle_name,  shop_ID,  name, uid, com,distance,cycleUid,answer_type, old_type,other_request,part,problem,report_type, title,yosan,area,day,bytes)
                    val cause=mCycle!!.cause
                    val com2=mCycle!!.com2
                    val contact=mCycle!!.contact
                    val estimate=mCycle!!.estimate
                    val question_cycleuid=mCycle!!.question_cyclruid
                    val result=mCycle!!.result
                    val Anbytes=mCycle!!.Anbytes
                    xCycle=Answer_question2( cycle_name,  shop_ID,  name, uid, com,distance,cycleUid,answer_type, old_type,other_request,part,problem,report_type, title,yosan,area,bytes,cause,com2,contact,estimate,question_cycleuid,result,day,Anbytes)
                    //回答情報から質問を読み込む

                    if(A_type==1){
                        conect(xCycle!!,0)
                    }else{
                        conect(xCycle!!,1)
                    }


                }else if(snapshot.value==null){
                    AlertDialog.Builder(this@TabLayoutActivity) // FragmentではActivityを取得して生成
                        .setTitle("メッセージ")
                        .setMessage("質問への回答が打ち切られました。回答を消去します")
                        .setPositiveButton("OK", { dialog, which ->
                            var user = FirebaseAuth.getInstance().currentUser!!.uid
                            val cycleRef = mDatabaseReference.child("report").child("Answer").child(user).child(mCycle!!.question_cyclruid)
                            cycleRef!!.removeValue()
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                        })
                        .show()

                }
            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })
    }

    override fun onClickButton() {
        val intent = Intent(applicationContext, Partresister2Activity::class.java)
        intent.putExtra("cycle",yCycle!!.cycleUid)
        startActivity(intent)
    }

    override fun shop(key:String) {
        val intent = Intent(applicationContext, Shop_info2_Activity::class.java)
        intent.putExtra("shop_id",key)
        startActivity(intent)
    }

    }
