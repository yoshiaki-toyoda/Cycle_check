package jp.cycle_check

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.constraintlayout.solver.widgets.Snapshot
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_partresister.*
import java.util.HashMap
import java.util.Map

class Partresister2Activity : AppCompatActivity() {
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var mReportRef: DatabaseReference? = null


    //自分の自転車リストを取得
    private val vEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map: Map<String, String> = dataSnapshot.value as Map<String, String>

            var flame = map["Flame"] ?: ""
            var fdelay = map["fdelay"] ?: ""
            var ldelay = map["ldelay"] ?: ""
            var sproket = map["sproket"] ?: ""
            var wheel = map["wheel"] ?: ""
            var taiya = map["Taiya"] ?: ""
            var cheenling = map["cheemling"] ?: ""
            var STIT = map["STIT"] ?: ""
            var shet = map["shet"] ?: ""

            flamelText.setText(flame, TextView.BufferType.NORMAL)
            fdelayText.setText(fdelay, TextView.BufferType.NORMAL)
            ldelayText.setText(ldelay, TextView.BufferType.NORMAL)
            sproketText.setText(sproket, TextView.BufferType.NORMAL)
            wheelText.setText(wheel, TextView.BufferType.NORMAL)
            TaiyaText.setText(taiya, TextView.BufferType.NORMAL)
            cheenlingText.setText(cheenling, TextView.BufferType.NORMAL)
            STIText.setText(STIT, TextView.BufferType.NORMAL)
            shetText.setText(shet, TextView.BufferType.NORMAL)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partresister2)
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        //自転車のUidを取得
        val extras = intent.extras
        var CycleUid = extras.get("cycle").toString()
        //mReportRef = mDatabaseReference.child(PartPath).child(CycleUid)
        //mReportRef!!.addChildEventListener(vEventListener)
        getcycle(CycleUid)

        //各部品のデータ取得



    }

    fun getcycle(key:String){

        val cycleRef = mDatabaseReference.child(PartPath).child(key)
        cycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.value as kotlin.collections.Map<String, String>
                var flame = map["Flame"] ?: ""
                var fdelay = map["fdelay"] ?: ""
                var ldelay = map["ldelay"] ?: ""
                var sproket = map["sproket"] ?: ""
                var wheel = map["wheel"] ?: ""
                var taiya = map["Taiya"] ?: ""
                var cheenling = map["cheemling"] ?: ""
                var STIT = map["STIT"] ?: ""
                var shet = map["shet"] ?: ""

                flamelText.setText(flame, TextView.BufferType.NORMAL)
                fdelayText.setText(fdelay, TextView.BufferType.NORMAL)
                ldelayText.setText(ldelay, TextView.BufferType.NORMAL)
                sproketText.setText(sproket, TextView.BufferType.NORMAL)
                wheelText.setText(wheel, TextView.BufferType.NORMAL)
                TaiyaText.setText(taiya, TextView.BufferType.NORMAL)
                cheenlingText.setText(cheenling, TextView.BufferType.NORMAL)
                STIText.setText(STIT, TextView.BufferType.NORMAL)
                shetText.setText(shet, TextView.BufferType.NORMAL)


            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })
    }



}