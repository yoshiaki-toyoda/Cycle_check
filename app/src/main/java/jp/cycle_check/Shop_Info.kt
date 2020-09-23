package jp.cycle_check

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
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
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_shop_info.*
import java.io.ByteArrayOutputStream

class Shop_Info : AppCompatActivity(),View.OnClickListener {
    private val PERMISSIONS_REQUEST_CODE = 100
    private val CHOOSER_REQUEST_CODE = 100
    private var mPictureUri: Uri? = null
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private var mCycleRef: DatabaseReference? = null
    var user = FirebaseAuth.getInstance().currentUser!!.uid
    var age=""
    var area=""
    var exp=""
    var name=""
    var sex=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_info)
        shop_image.setOnClickListener(this)
        toolbar_shopR.setTitle("店舗情報")
        setSupportActionBar(toolbar_shopR)
        get_number()
        shop_enry.setOnClickListener(){
            shopinfo_entry()
        }
        shop_delete.setOnClickListener(){
            shopinfo_delete()
        }
    }

    override fun onClick(v: View) {
        if (v == shop_image) {
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_CODE
                    )
                    return
                }
            } else {
                showChooser()
            }
        }
    }

    fun get_number() {
        var shop_ID=""
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mCycleRef = mDatabaseReference.child("users").child(user)
        mCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value!== null) {
                    val map = snapshot.value!! as Map<String, String>
                    var shop_ID = map["shop_ID"] ?: ""
                    age=map["age"]?: ""
                    area=map["area"]?: ""
                    exp=map["exp"]?: ""
                    name=map["name"]?: ""
                    sex=map["sex"]?: ""
                    shop_id1.text=shop_ID.toString()
                    shop_info(shop_ID)
                }else{
                    val shop_ID=""
                    shop_info(shop_ID)
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        val shop_id=shop_id1.text.toString()?:""
        shop_info(shop_id)
    }

    fun shop_info(shop_ID:String) {
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mCycleRef = mDatabaseReference.child("shop").child(shop_ID)
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
                    var service=map["service"]?:""
                    var bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    Shop_name1.setText(shop_name)
                    shop_area1.setText(shop_area)
                    shop_mail1.setText(shop_mail)
                    shop_tel1.setText(shop_tell)
                    shop_comment.setText(shop_com)
                    service_comment.setText(service)

                    if (bytes.isNotEmpty()) {
                        val image =
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                        val imageView = findViewById<View>(R.id.shop_image) as ImageView
                        imageView.setImageBitmap(image)
                    }
                }else{
                    var id=""
                        for(w_length in 0..10){
                            var rnds = (0..10).random()
                            id=rnds.toString()+id
                        }
                        shop_id1.text=id

                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    fun shopinfo_entry(){
        val user = FirebaseAuth.getInstance().currentUser!!.uid
        val data = java.util.HashMap<String, Any>()
        val shop_name=Shop_name1.text.toString()
        val shop_ID=shop_id1.text.toString()
        val shop_area=shop_area1.text.toString()
        val shop_mail=shop_mail1.text.toString()
        val shop_tell=shop_tel1.text.toString()
        val shop_com=shop_comment.text.toString()
        val drawable = shop_image.drawable as? BitmapDrawable
        val service=service_comment

        if (drawable != null) {
            val bitmap = drawable.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            data["image"] = bitmapString ?: ""
        }else{
            data["image"] =  ""
        }

        data["name"]=shop_name ?: ""
        data["ID"]=shop_ID ?: ""
        data["area"]=shop_area ?: ""
        data["mail"]=shop_mail ?: ""
        data["Tel"]=shop_tell ?: ""
        data["com"]=shop_com ?: ""
        data["user"]=user
        data["service"]=service

        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        val vGenreRef = mDatabaseReference.child("shop").child(shop_ID)
        vGenreRef.setValue(data)
        Toast.makeText(applicationContext, "店舗情報を登録しました", Toast.LENGTH_LONG).show()
        val userRef = mDatabaseReference.child("users").child(user)
        val data1 = java.util.HashMap<String, Any>()
        data1["age"]=age
        data1["area"]=area
        data1["exp"]=exp
        data1["name"]=name
        data1["sex"]=sex
        data1["shop_ID"]=shop_ID
        userRef!!.setValue(data1)
    }

    fun shopinfo_delete(){
        AlertDialog.Builder(this) // FragmentではActivityを取得して生成
            .setTitle("確認")
            .setMessage("店舗情報を削除しますか？")
            .setPositiveButton("OK", { dialog, which ->
                // TODO:Yesが押された時の挙動
                val shop_id=shop_id1.text.toString()?:""
                mDatabaseReference = FirebaseDatabase.getInstance().reference
                val vGenreRef = mDatabaseReference.child("shop").child(shop_id)
                vGenreRef.removeValue()
            })
            .setNegativeButton("No", { dialog, which ->
                // TODO:Noが押された時の挙動
            })
            .show()







    }













    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    showChooser()
                }
                return
            }
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
            shop_image.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }

    private fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        mPictureUri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, "画像を取得")

        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }
}




