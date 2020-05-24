package jp.cycle_check

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
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
import android.view.KeyEvent
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
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_setting.*
import java.io.ByteArrayOutputStream

class DetailActivity : AppCompatActivity(), View.OnClickListener, DatabaseReference.CompletionListener {
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
        setContentView(R.layout.activity_detail)
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        imageView3.setOnClickListener(this)


        //リストからのデータ移動
        val extras = intent.extras
        mCycle = extras.get("cycleinfo") as Cycleinfo
        cycle_name =mCycle.cycle_name
        cycle_uid=mCycle.cycleUid

        var cuid = mCycle.cycleUid//ユーザーuid
        var cycname=mCycle.cycle_name
        var username = mCycle.name//ユーザー名
        var shopid = mCycle.shop_ID//購入店
        var com = mCycle.com//コメント
        var dis = mCycle.distance//　走行距離
        var bytes = mCycle.imageBytes
        var cycUid=mCycle.cycleUid

        //取得したデータを画面に配置

        cyclenameText.setText(cycname, TextView.BufferType.NORMAL)
        shopText.text = shopid
        comText2.setText(com, TextView.BufferType.NORMAL)
        disText.text =  dis +"Km"

        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            val imageView = findViewById<View>(R.id.imageView3) as ImageView
            imageView.setImageBitmap(image)
        }

        distancebutton.setOnClickListener { v ->
            //走行距離入力ボタン押下時の処理
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            val intent = Intent(applicationContext, DistanceActivity::class.java)
            intent.putExtra("cyclename", cycle_name)
            intent.putExtra("cycle_uid",cycle_uid)
            startActivity(intent)

        }
        mentebutton.setOnClickListener { v ->
            //メンテボタン押下時の処理
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)


        }
        partbutton.setOnClickListener { v ->
            //パーツボタン押下時の処理
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            val intent = Intent(applicationContext, PartresisterActivity::class.java)
            intent.putExtra("cycle",cycUid)

            startActivity(intent)

        }

        confirmbutton.setOnClickListener{v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            val intent = Intent(applicationContext, QuestionActivity::class.java)
            intent.putExtra("cycleinfo",mCycle)
            startActivity(intent)
        }
        allconfirmbutton.setOnClickListener{v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            val intent = Intent(applicationContext, QuestionAllActivity::class.java)
            intent.putExtra("cycleinfo",mCycle)
            startActivity(intent)
        }

        cyclenameText.setOnKeyListener { v, keyCode, event ->
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
               update()
                true
            } else {
                false
            }
        }

        comText2.setOnKeyListener { v, keyCode, event ->
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                update()
                true
            } else {
                false
            }
        }


        cyclenameText.isFocusableInTouchMode = true
        cyclenameText.requestFocus()
/*
        compbutton.setOnClickListener { v -> //変更確定ボタン押下時の処理
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            var data = HashMap<String, String>() // 添付画像を取得する
            var data2=HashMap<String,ArrayList<String>>()
            val drawable = imageView3.drawable as? BitmapDrawable
            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                data["image"] = bitmapString
            }
            // Preferenceから表示名を取得してEditTextに反映させる
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val name = sp.getString(NameKEY, "")


            data["cycle_name"] =  cyclenameText.text.toString()
            data["shop_ID"] = mCycle.shop_ID
            data["com"] = comText2.text.toString()
            data["name"] = name.toString()
            data["uid"] = mCycle.uid
            data["alert"] = mCycle.alert
            data["distance"] = mCycle.distance

            mAuth = FirebaseAuth.getInstance()
            val cycleRef = mDatabaseReference.child(CyclePATH).child(cuid)
            cycleRef.setValue(data)
            finish()

        }
*/

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





    override fun onClick(v: View) {
        if (v === imageView3) {
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
        } else if (v === cycleresisterbutton) {
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)

            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val genreRef = dataBaseReference.child(CyclePATH)
            val data = java.util.HashMap<String, Any>()

            data["uid"] = FirebaseAuth.getInstance().currentUser!!.uid// UID

            // 添付画像を取得する
            val drawable = imageView3.drawable as? BitmapDrawable

            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                val cycleRef = mDatabaseReference.child(CyclePATH).child(mCycle.cycleUid)
                data["image"] = bitmapString

                data.put("image",bitmapString)
                cycleRef.updateChildren(data)

            }





        }
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

    override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {

        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }

    fun update(){

        var data = HashMap<String, Any>() // 添付画像を取得する
        // Preferenceから表示名を取得してEditTextに反映させる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")

        mAuth = FirebaseAuth.getInstance()
        val cycleRef = mDatabaseReference.child(CyclePATH).child(mCycle.cycleUid)
        data.put("com",comText2.text.toString())
        data.put("cycle_name", cyclenameText.text.toString())
        cycleRef.updateChildren(data)




        finish()

    }



}
