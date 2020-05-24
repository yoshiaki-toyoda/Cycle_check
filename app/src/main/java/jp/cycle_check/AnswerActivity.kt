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
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ImageView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_answer.*
import kotlinx.android.synthetic.main.activity_answer.disText
import kotlinx.android.synthetic.main.activity_answer.shopText
import kotlinx.android.synthetic.main.activity_detail.*

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_header_main.*
import java.io.ByteArrayOutputStream

class AnswerActivity : AppCompatActivity(),View.OnClickListener {
    private lateinit var question_info: Questioninfo
    private lateinit var answer_info: Answer_question2
    private val PERMISSIONS_REQUEST_CODE = 100
    private val CHOOSER_REQUEST_CODE = 100
    private var mPictureUri: Uri? = null
    private lateinit var mAuth: FirebaseAuth
    private var vGenreRef: DatabaseReference? = null
    private lateinit var mDatabaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer)
        imageView5.setOnClickListener(this)
        answer_bottun.setOnClickListener(this)

        //リストからのデータ移動
        val extras = intent.extras
        question_info = extras.get("questioninfo") as Questioninfo

        val bytes = question_info.imageBytes
        val report_type = question_info.report_type
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

        val answer_type = question_info.answer_type
        var answer = ""
        if (answer_type == "perform") {
            answer = "パフォーマンス重視"
        } else if (answer_type == "cospa") {
            answer = "コスパ重視"
        } else if (answer_type == "otherchoise") {
            answer = "その他"
        }

        val old_type = question_info.old_type
        var old = ""
        if (old_type == "oldpart_yes") {
            old = "中古部品使用可能"
        } else if (old_type == "oldpart_no") {
            old = "中古部品使用不可"
        }

        //リスト内のデータをtextviewに入れる
        Titile_ditail.text = question_info.title
        user_name.text ="質問者名："+question_info.name
        area_name.text = "質問者地域："+question_info.area
        cycle_name.text = "自転車名："+question_info.cycle_name
        shopText.text = "購入店："+question_info.shop_ID
        disText.text = "走行距離："+question_info.distance+"km"
        Report_typetext.text ="回答タイプ："+ report
        part_name.text = "相談部品："+question_info.part
        problem_edit.text = question_info.problem
        reportAnsertext.text = answer
        type_otheretext.text = question_info.other_request
        yosan_text.text = question_info.yosan
        oldpart_text.text = old
        com_edit.text = question_info.com

        //画像データ貼り付け
        if (bytes.isNotEmpty()) {
            val image =
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = findViewById<View>(R.id.imageView4) as ImageView
                 imageView.setImageBitmap(image)
        }

        //回答者情報入力
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        val erea = sp.getString(AreaKEY, "")
            answer_name.text = "回答者名"+"\n"+name
            answerarea_name.text = "回答者地域\n"+erea

    }

    override fun onClick(v: View) {
        if (v === imageView5) {
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
        }else if(v==answer_bottun){
            //Data取得
            val data = java.util.HashMap<String, Any>()
            val result=answer_resultedit.text.toString()
            val estimate=answer_yosanedit.text.toString()
            val cause=answer_reasonedit.text.toString()
            val com=answer_comeditText.text.toString()
            val cycle_uid=question_info.cycleUid
            val drawable = imageView5.drawable as? BitmapDrawable
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
            data["contact"]=answer_answereditText.text.toString()

            //回答登録先を設定
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val user = FirebaseAuth.getInstance().currentUser!!.uid
            mAuth = FirebaseAuth.getInstance()
            mDatabaseReference = FirebaseDatabase.getInstance().reference
            val vGenreRef = mDatabaseReference.child("report").child("Answer").child(user).child(cycle_uid)
            vGenreRef.setValue(data)
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
            imageView5.setImageBitmap(resizedImage)

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
