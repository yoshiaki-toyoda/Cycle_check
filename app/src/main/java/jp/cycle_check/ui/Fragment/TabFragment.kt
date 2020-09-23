package jp.cycle_check.ui.Fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import jp.cycle_check.*
import kotlinx.android.synthetic.main.activity_answer.*
import kotlinx.android.synthetic.main.activity_distance.*
import kotlinx.android.synthetic.main.fragment_tab_01.*
import kotlinx.android.synthetic.main.fragment_tab_01.Report_typetext
import kotlinx.android.synthetic.main.fragment_tab_01.Titile_ditail
import kotlinx.android.synthetic.main.fragment_tab_01.area_name
import kotlinx.android.synthetic.main.fragment_tab_01.com_edit
import kotlinx.android.synthetic.main.fragment_tab_01.cycle_name
import kotlinx.android.synthetic.main.fragment_tab_01.disText
import kotlinx.android.synthetic.main.fragment_tab_01.imageView4
import kotlinx.android.synthetic.main.fragment_tab_01.oldpart_text
import kotlinx.android.synthetic.main.fragment_tab_01.part_name
import kotlinx.android.synthetic.main.fragment_tab_01.problem_edit
import kotlinx.android.synthetic.main.fragment_tab_01.reportAnsertext
import kotlinx.android.synthetic.main.fragment_tab_01.shopText
import kotlinx.android.synthetic.main.fragment_tab_01.type_otheretext
import kotlinx.android.synthetic.main.fragment_tab_01.user_name
import kotlinx.android.synthetic.main.fragment_tab_01.yosan_text
import kotlinx.android.synthetic.main.fragment_tab_02.*
import kotlinx.android.synthetic.main.fragment_tab_02.answer_name
import kotlinx.android.synthetic.main.fragment_tab_02.answerarea_name
import java.io.ByteArrayOutputStream
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast

class Tab01Fragment(mCycle:Answer_question2): Fragment(){
    var xCycle:Answer_question2=mCycle
    private var xListener: Tab01Fragment.FragmentListener? = null



    interface FragmentListener {
        fun onClickButton()
    }

    fun addFragment(){
        xListener?.onClickButton()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentListener) {
            xListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_01,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bytes = xCycle.imageBytes
        val report_type = xCycle.report_type
        var report = ""

        if (report_type == "defect") {
            report = "不具合相談"
        } else if (report_type == "update") {
            report = "アップグレード(部品交換)相談"
        } else if (report_type == "mitsumori") {
            report = "見積相談"
        } else if (report_type == "other") {
            report = "その他相談"
        }

        val answer_type = xCycle.answer_type
        var answer = ""
        if (answer_type == "perform") {
            answer = "パフォーマンス重視"
        } else if (answer_type == "cospa") {
            answer = "コスパ重視"
        } else if (answer_type == "otherchoise") {
            answer = "その他"
        }

        val old_type = xCycle.old_type
        var old = ""
        if (old_type == "oldpart_yes") {
            old = "中古部品使用可能"
        } else if (old_type == "oldpart_no") {
            old = "中古部品使用不可"
        }

        Titile_ditail.text = xCycle.title
        user_name.text=xCycle.name
        area_name.text =xCycle.area
        cycle_name.text = xCycle.cycle_name
        shopText.text = xCycle.shop_ID
        disText.text = xCycle.distance+"km"
        Report_typetext.text = report
        part_name.text = xCycle.part
        problem_edit.text = xCycle.problem
        reportAnsertext.text = answer
        type_otheretext.text = xCycle.other_request
        yosan_text.text = xCycle.yosan
        oldpart_text.text = old
        com_edit.text = xCycle.com
        dayeditText.text=xCycle.day

        //画像データ貼り付け
        if (bytes.isNotEmpty()) {
            val image =
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            imageView4.setImageBitmap(image)
        }

        part_button.setOnClickListener { view ->
            addFragment()
        }
    }


}

class Tab02Fragment(mCycle: Answer_question2,type:Int): Fragment(){
    var xCycle:Answer_question2=mCycle
    private var mCycleRef: DatabaseReference? = null
    private lateinit var mDatabaseReference: DatabaseReference
    private var mListener: FragmentListener? = null
    private lateinit var mAuth: FirebaseAuth
    private val PERMISSIONS_REQUEST_CODE = 100
    private val CHOOSER_REQUEST_CODE = 100
    val type=type
    private var mPictureUri: Uri? = null

    interface FragmentListener {
        fun shop(key:String)

    }

    fun addFragment(){
    }

    fun addFragment1(key:String){
        mListener?.shop(key)
    }


    override fun onAttach(context: Context){
        super.onAttach(context)
        if (context is FragmentListener){
            mListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_02,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //回答者情報入力
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val name = sp.getString(NameKEY, "")
        val erea = sp.getString(AreaKEY, "")

        answer_name.text = name
        answerarea_name.text = erea
        answer_resultedit1.setText(xCycle.result)
        answer_yosanedit1.setText(xCycle.estimate)
        answer_reasonedit1.setText(xCycle.cause)
        answer_comeditText1.setText(xCycle.com2)
        contact_editText1.setText(xCycle.contact)
        //画像データ貼り付け

        if (xCycle.Anbytes.isNotEmpty()) {
            val image =
                BitmapFactory.decodeByteArray(xCycle.Anbytes, 0, xCycle.Anbytes.size).copy(Bitmap.Config.ARGB_8888, true)
            imageView7.setImageBitmap(image)
        }


        if(type==1) {
            answer_bottun2.setVisibility(View.GONE)
            answer_name.setRawInputType(InputType.TYPE_CLASS_TEXT);
            answer_name.setTextIsSelectable(true)

            answerarea_name.setRawInputType(InputType.TYPE_CLASS_TEXT);
            answerarea_name.setTextIsSelectable(true)

            answer_resultedit1.setRawInputType(InputType.TYPE_CLASS_TEXT);
            answer_resultedit1.setTextIsSelectable(true)

            answer_yosanedit1.setRawInputType(InputType.TYPE_CLASS_TEXT);
            answer_yosanedit1.setTextIsSelectable(true)

            answer_reasonedit1.setRawInputType(InputType.TYPE_CLASS_TEXT);
            answer_reasonedit1.setTextIsSelectable(true)

            answer_comeditText1.setRawInputType(InputType.TYPE_CLASS_TEXT);
            answer_comeditText1.setTextIsSelectable(true)

            contact_editText1.setRawInputType(InputType.TYPE_CLASS_TEXT);
            contact_editText1.setTextIsSelectable(true)

            shop_bottun2.setOnClickListener(){
                addFragment1(xCycle.shop_ID)
            }

        }else if(type==5){
            answer_bottun2.setVisibility(View.GONE)
            shop_bottun2.setOnClickListener(){
                addFragment1(xCycle.shop_ID)
            }


        } else{

            shop_bottun2.setVisibility(View.GONE)
            answer_bottun2.setOnClickListener { view ->
                //Data取得
                setvalue()
                Toast.makeText(context, "回答を登録しました", Toast.LENGTH_LONG).show()
            }

            imageView7.setOnClickListener{view ->
                // パーミッションの許可状態を確認する
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(context!!,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // 許可されている
                        showChooser()
                    } else {
                        // 許可されていないので許可ダイアログを表示する
                        requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSIONS_REQUEST_CODE
                        )

                    }
                } else {
                    showChooser()
                }
            }


        }
    }
    fun setvalue(){

        var result=""
        var estimate=""
        var cause=""
        var com=""

        val data = java.util.HashMap<String, Any>()
        result=answer_resultedit1.text.toString()
         estimate=answer_yosanedit1.text.toString()
        cause=answer_reasonedit1.text.toString()
        com=answer_comeditText1.text.toString()
        val cycle_uid=xCycle.cycleUid
        val drawable = imageView7.drawable as? BitmapDrawable
        // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
        if (drawable != null) {
            val bitmap = drawable.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            data["image"] = bitmapString
        }

        data["result"]=result
        data["estimate"]=estimate
        data["cause"]=cause
        data["com"]=com
        data["question_cycleuid"]=cycle_uid
        data["contact"]=contact_editText1.text.toString()

        //回答登録先を設定
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        var user = FirebaseAuth.getInstance().currentUser!!.uid
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        val vGenreRef = mDatabaseReference.child("report").child("Answer").child(user).child(cycle_uid)
        vGenreRef.setValue(data)
    }

    fun shop_setvalue(){

        var result=""
        var estimate=""
        var cause=""
        var com=""

        val data = java.util.HashMap<String, Any>()
        result=answer_resultedit1.text.toString()
        estimate=answer_yosanedit1.text.toString()
        cause=answer_reasonedit1.text.toString()
        com=answer_comeditText1.text.toString()
        val cycle_uid=xCycle.cycleUid
        val drawable = imageView7.drawable as? BitmapDrawable
        // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
        if (drawable != null) {
            val bitmap = drawable.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            data["image"] = bitmapString
        }

        data["result"]=result
        data["estimate"]=estimate
        data["cause"]=cause
        data["com"]=com
        data["question_cycleuid"]=cycle_uid
        data["contact"]=contact_editText1.text.toString()

        //回答登録先を設定
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        var user = FirebaseAuth.getInstance().currentUser!!.uid
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        val vGenreRef = mDatabaseReference.child("report").child("Answer").child("shop").child(xCycle!!.shop_ID).child(xCycle!!.cycleUid)
        vGenreRef.setValue(data)
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
        mPictureUri = context!!.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, "画像を取得")

        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
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
                    context!!.contentResolver.delete(mPictureUri!!, null, null)
                    mPictureUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) mPictureUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = context!!.contentResolver
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
            imageView7.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }
}