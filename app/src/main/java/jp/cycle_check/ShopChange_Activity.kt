package jp.cycle_check

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_shop_change_.*
import kotlinx.android.synthetic.main.activity_shop_info.*

class ShopChange_Activity : AppCompatActivity() {
    private lateinit var mDataBaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_change_)
        toolbar1.setTitle("店舗番号登録")
        setSupportActionBar(toolbar1)
        val extras = intent.extras
        mCycle = extras.get("cycleinfo") as Cycleinfo
        var s_number= mCycle.shop_ID
        shop_number.setText(s_number)

        shop_number_entry.setOnClickListener(){
            shop_id_confirm()
        }

    }

    fun shop_id_confirm(){
        mDataBaseReference = FirebaseDatabase.getInstance().reference
        val shop_id=shop_number.text.toString()
        val shopRef = mDataBaseReference.child("shop").child(shop_id)

        shopRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    shop_id_resister(shop_id)
                }else{
                    Toast.makeText(applicationContext, "店舗IDが不正です", Toast.LENGTH_LONG).show()
                }
            }
        })

    }

    fun shop_id_resister(shop_id:String){
        val userRef = mDataBaseReference.child(CyclePATH).child(mCycle.cycleUid)
        val data = HashMap<String, Any>()
        data.put("shop_ID",shop_id)
        userRef.updateChildren(data)
        Toast.makeText(applicationContext, "店舗番号を登録しました", Toast.LENGTH_LONG).show()
    }

}
