package jp.cycle_check


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_cycleresister.*
import kotlinx.android.synthetic.main.activity_cycleresister.resistershopText
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_detail.comText2
import kotlinx.android.synthetic.main.activity_detail.cyclenameText
import kotlinx.android.synthetic.main.activity_detail.disText
import kotlinx.android.synthetic.main.activity_detail.imageView3
import kotlinx.android.synthetic.main.activity_detail.partbutton
import kotlinx.android.synthetic.main.activity_detail.shopText
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_otheruser.*
import kotlinx.android.synthetic.main.activity_setting.*
import java.io.ByteArrayOutputStream

class OtheruserActivity : AppCompatActivity()  {
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mCycleArrayList: ArrayList<Cycleinfo>
    private lateinit var mAdapter: CycleListAdapter
    private lateinit var mCycle: Cycleinfo
    private lateinit var cycle_name:String
    private lateinit var cycle_uid:String
    private lateinit var mAuth: FirebaseAuth
    private var mCycleRef: DatabaseReference? = null
    private var vCycleRef: DatabaseReference? = null
    private lateinit var mToolbar: Toolbar
    private var list_type:Int =0

    private val PERMISSIONS_REQUEST_CODE = 100
    private val CHOOSER_REQUEST_CODE = 100
    private var mPictureUri: Uri? = null


    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otheruser)
        mDatabaseReference = FirebaseDatabase.getInstance().reference


        //リストからのデータ移動
        val extras = intent.extras
        mCycle = extras.get("cycleinfo") as Cycleinfo
        cycle_name =mCycle.cycle_name
        cycle_uid=mCycle.cycleUid

        var cuid = mCycle.cycleUid//ユーザーuid
        var cycname=mCycle.cycle_name
        var ctitle = mCycle.name//ユーザー名
        var shopid = mCycle.shop_ID//購入店
        var com = mCycle.com//コメント
        var dis = mCycle.distance//　走行距離
        var bytes = mCycle.imageBytes
        var cycUid=mCycle.cycleUid

        //取得したデータを画面に配置

        cyclenameText.setText(cycname, TextView.BufferType.NORMAL)
        shopText.text = "Shop Id:"+shopid
        disText.text = "走行距離："+ dis +"Km"
        comText3.text=com

        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            val imageView = findViewById<View>(R.id.imageView3) as ImageView
            imageView.setImageBitmap(image)
        }


        partbutton.setOnClickListener { v ->
            //パーツボタン押下時の処理
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            val intent = Intent(applicationContext, Partresister2Activity::class.java)
            intent.putExtra("cycle",cycUid)
            startActivity(intent)

        }



    }
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSER_REQUEST_CODE) {

            if (resultCode != Activity.RESULT_OK) {
                if (mPictureUri != null) {
                    contentResolver.delete(mPictureUri!!, null, null)
                    mPictureUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mPictureUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight) // (1)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage =
                Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            // BitmapをImageViewに設定する
            imageView3.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }








}