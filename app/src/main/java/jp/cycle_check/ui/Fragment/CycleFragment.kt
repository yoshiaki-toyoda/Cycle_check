package jp.cycle_check.ui.Fragment


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jp.cycle_check.*
import kotlinx.android.synthetic.main.app_header_main.toolbar
import kotlinx.android.synthetic.main.fragment_cycle.*
import java.io.ByteArrayOutputStream

class CycleFragment:Fragment(){

    var mCycle:Cycleinfo?=null
    private var mListener: FragmentListener? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference
    private val PERMISSIONS_REQUEST_CODE = 100
    private val CHOOSER_REQUEST_CODE = 100
    private var mPictureUri: Uri? = null

    interface FragmentListener {
        fun onClickButton()
        fun onDelete()
    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        if (context is FragmentListener){
           mListener = context
        }
    }

    fun addFragment(){
        mListener?.onClickButton()
    }

    fun DeleteFragment(){
        mListener?.onDelete()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCycle = arguments!!.getSerializable("CycleInfo") as Cycleinfo
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return  inflater.inflate(R.layout.fragment_cycle, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle("車両情報")

        val addFragment = view.findViewById<Button>(R.id.partbutton)
        addFragment.setOnClickListener {
            addFragment()
        }

        val addFragment2=view.findViewById<EditText>(R.id.comText2)
        addFragment2.setOnKeyListener { v, keyCode, event ->
            if (event.getAction() == KeyEvent.ACTION_DOWN || keyCode == KeyEvent.KEYCODE_ENTER) {
                update()
                true
            } else {
                false
            }
        }

        val addFragment3=view.findViewById<EditText>(R.id.cyclenameText)
        addFragment3.setOnKeyListener { v, keyCode, event ->
            if (event.getAction() == KeyEvent.ACTION_DOWN || keyCode == KeyEvent.KEYCODE_ENTER) {
                update()
                true
            } else {
                false
            }
        }

        val addFragment4=view.findViewById<ImageView>(R.id.imageView3)
        addFragment4.setOnClickListener {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_CODE)
        }

        val addFragment5=view.findViewById<Button>(R.id.delete_button)
        addFragment5.setOnClickListener {
           delete()
        }


        //layoutのtextview id取得
        val cyclename_text=view.findViewById<EditText>(R.id.cyclenameText)
        val com_text=view.findViewById<EditText>(R.id.comText2)
        val shop_text=view.findViewById<TextView>(R.id.shopText)
        val imageView = view.findViewById<View>(R.id.imageView3) as ImageView
        val distance_text=view.findViewById<TextView>(R.id.disText)

        //textviewへの入力
        if (mCycle!!.imageBytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(mCycle!!.imageBytes, 0, mCycle!!.imageBytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            imageView.setImageBitmap(image)
        }

        cyclename_text.setText(mCycle!!.cycle_name, TextView.BufferType.NORMAL)
        com_text.setText(mCycle!!.com, TextView.BufferType.NORMAL)
        shop_text.text=mCycle!!.shop_ID
        distance_text.text=mCycle!!.distance+"Km"
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    fun update(){
        var data = HashMap<String, Any>() // 添付画像を取得する
        mAuth = FirebaseAuth.getInstance()

        val cycleRef = mDatabaseReference.child(CyclePATH).child(mCycle!!.cycleUid)
        data.put("com",comText2.text.toString())
        data.put("cycle_name", cyclenameText.text.toString())
        cycleRef.updateChildren(data)

    }

    fun update_firebase(){
        val mDatabaseReference = FirebaseDatabase.getInstance().reference
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
            val cycleRef = mDatabaseReference.child(CyclePATH).child(jp.cycle_check.mCycle.cycleUid)
            data["image"] = bitmapString
            data.put("image",bitmapString)
            cycleRef.updateChildren(data)

        }
    }

    fun delete(){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("削除")
        builder.setMessage("全てのデータを削除しますか")
        builder.setPositiveButton("OK") { _, _ ->

        var data = null
        mAuth = FirebaseAuth.getInstance()
        val cycleRef = mDatabaseReference.child(CyclePATH).child(mCycle!!.cycleUid)
        cycleRef.removeValue()
            DeleteFragment()

    }
        val dialog = builder.create()
        builder.setNegativeButton("CANCEL", null)
        dialog.show()

        true
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
                    val contentResolver = activity!!.contentResolver
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
                val contentResolver = activity!!.contentResolver
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
            val imageView = view!!.findViewById<View>(R.id.imageView3) as ImageView
            imageView.setImageBitmap(resizedImage)
            update_firebase()
            mPictureUri = null
        }
    }


  fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        val contentResolver = activity!!.contentResolver
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