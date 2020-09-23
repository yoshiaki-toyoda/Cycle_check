package jp.cycle_check

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_shop_info.*
import kotlinx.android.synthetic.main.activity_shop_info2_.*

class Shop_info2_Activity : AppCompatActivity() {
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var mCycleRef: DatabaseReference? = null
    var shop_id:String?=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_info2_)
        toolbar_shop.setTitle("店舗情報")
        setSupportActionBar(toolbar_shop)

        val extras = intent.extras
         shop_id=extras.get("shop_id") as String?

        if(shop_id==""|| shop_id==null) {
            mCycle = extras.get("cycleinfo") as Cycleinfo
            shop_id= mCycle.shop_ID.toString()
        }

        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mCycleRef = mDatabaseReference.child("shop").child(shop_id.toString())
        mCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value!== null) {
                    val map = snapshot.value!! as Map<String, String>
                    var shop_name = map["name"] ?: ""
                    var shop_ID = map["ID"] ?: ""
                    var shop_area = map["area"] ?: ""
                    var shop_mail = map["mail"] ?: ""
                    var shop_tell: String = map["Tel"] ?: ""
                    var shop_com = map["com"] ?: ""
                    var imageString = map["image"] ?: ""
                    var bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    Shop_name2.text=shop_name.toString()
                    shop_area2.text=shop_area
                    shop_mail2.text=shop_mail
                    shop_area2.text=shop_area
                    shop_tel2.text=shop_tell
                    shop_comment2.text=shop_com
                    shop_id2.text= mCycle.shop_ID



                    if (bytes.isNotEmpty()) {
                        val image =
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                        val imageView = findViewById<View>(R.id.shop_image2) as ImageView
                        imageView.setImageBitmap(image)
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

}


