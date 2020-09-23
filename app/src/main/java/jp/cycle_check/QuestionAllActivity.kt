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
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question.Titile_ditail
import kotlinx.android.synthetic.main.activity_question.area_name
import kotlinx.android.synthetic.main.activity_question.com_edit
import kotlinx.android.synthetic.main.activity_question.cospa
import kotlinx.android.synthetic.main.activity_question.cycle_name
import kotlinx.android.synthetic.main.activity_question.defect
import kotlinx.android.synthetic.main.activity_question.disText
import kotlinx.android.synthetic.main.activity_question.imageView4
import kotlinx.android.synthetic.main.activity_question.mitsumori
import kotlinx.android.synthetic.main.activity_question.oldpart_no
import kotlinx.android.synthetic.main.activity_question.oldpart_yes
import kotlinx.android.synthetic.main.activity_question.other
import kotlinx.android.synthetic.main.activity_question.otherchoise
import kotlinx.android.synthetic.main.activity_question.perform
import kotlinx.android.synthetic.main.activity_question.problem_edit
import kotlinx.android.synthetic.main.activity_question.resisterbutton
import kotlinx.android.synthetic.main.activity_question.shopText
import kotlinx.android.synthetic.main.activity_question.type_otheredit
import kotlinx.android.synthetic.main.activity_question.update
import kotlinx.android.synthetic.main.activity_question.user_name
import kotlinx.android.synthetic.main.activity_question.yosan_edit
import kotlinx.android.synthetic.main.activity_question_all.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class QuestionAllActivity : AppCompatActivity(),View.OnClickListener,DatabaseReference.CompletionListener{
    private lateinit var mCycle: Cycleinfo
    private val spinnerItems= arrayListOf<String>("新規購入","フレーム","フロントディレイラー","リアディレイラー","スプロケット","ホイール","タイヤ","チェーンリング","STIレバー","サドル","その他")
    private val PERMISSIONS_REQUEST_CODE = 100
    private val CHOOSER_REQUEST_CODE = 100
    private var mPictureUri: Uri? = null
    private lateinit var mDatabaseReference: DatabaseReference
    var report_type=""
    var answer_type=""
    var old_type=""
    var part=""
    var create_day=""
    private lateinit var mAuth: FirebaseAuth
    private var mReportRef: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_all)
        imageView4.setOnClickListener(this)
        val extras = intent.extras
        mCycle = extras.get("cycleinfo") as Cycleinfo
        //area情報取得
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val area = sp.getString(AreaKEY, "")
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        mReportRef = mDatabaseReference.child("report").child("All").child(mCycle.cycleUid)
        mReportRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value!== null) {
                    val map = snapshot.value as Map<String?, String?>
                    report_type = map["report_type"] ?: ""
                    answer_type = map["answer_type"] ?: ""
                    old_type = map["old_type"] ?: ""
                    part = map["part"] ?: ""
                    val other_request = map["other_request"] ?: ""
                    val problem = map["problem"] ?: ""
                    val com = map["com"] ?: ""
                    val title = map["title"] ?: ""
                    val yosan = map["yosan"] ?: ""
                    var imageString = map["image"] ?: ""
                    var bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        .copy(Bitmap.Config.ARGB_8888, true)
                    val imageView = findViewById<View>(R.id.imageView4) as ImageView
                    imageView.setImageBitmap(image)


                    Titile_ditail.setText(title)
                    problem_edit.setText(problem)
                    type_otheredit.setText(other_request)
                    yosan_edit.setText(yosan)
                    com_edit.setText(com)
                    val radioGroup1 = findViewById<RadioGroup>(R.id.group1)
                    // 選択項目変更のイベント追加
                    if (report_type == "defect") {
                        radioGroup1.check(defect.id)
                    }
                    if (report_type == "update") {
                        radioGroup1.check(update.id)
                    }
                    if (report_type == "mitsumori") {
                        radioGroup1.check(mitsumori.id)
                    }
                    if (report_type == "other") {
                        radioGroup1.check(other.id)
                    }

                    val radioGroup2 = findViewById<RadioGroup>(R.id.group2)
                    // 選択項目変更のイベント追加
                    // キーボードが出てたら閉じる

                    // checkedIdから、選択されたRadioButtonを取得

                    // 選択項目変更のイベント追加
                    if (answer_type == "perform") {
                        radioGroup2.check(perform.id)
                    }
                    if (answer_type == "cospa") {
                        radioGroup2.check(cospa.id)
                    }
                    if (answer_type == "otherchoise") {
                        radioGroup2.check(otherchoise.id)
                    }

                    val radioGroup3 = findViewById<RadioGroup>(R.id.group3)
                    if (old_type == "oldpart_yes") {
                        radioGroup3.check(oldpart_yes.id)
                    }
                    if (old_type == "oldpart_no") {
                        radioGroup3.check(oldpart_no.id)
                    }
                }

            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })


        //レポートに入力
        user_name.text=mCycle.name.toString()
        cycle_name.text=mCycle.cycle_name.toString()
        shopText.text=mCycle.shop_ID.toString()
        disText.text=mCycle.distance+"km"
        area_name.text=area
        val date = Date()
        val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        create_day=format.format(date)
        dayText2.text=create_day

        //ラジオボタン押下時の操作
        val radioGroup1 = findViewById<RadioGroup>(R.id.group1)

        radioGroup1.setOnCheckedChangeListener { group, checkedId ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(group.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            // checkedIdから、選択されたRadioButtonを取得
            val radioButton = findViewById<RadioButton>(checkedId)
            if(radioButton==defect){
                radioButton.isChecked=true
                update.isChecked=false
                mitsumori.isChecked=false
                other.isChecked=false
                report_type="defect"
            }
            else if(radioButton==update){
                radioButton.isChecked=true
                defect.isChecked=false
                mitsumori.isChecked=false
                other.isChecked=false
                report_type="update"
            }
            else if(radioButton==mitsumori){
                radioButton.isChecked=true
                defect.isChecked=false
                update.isChecked=false
                other.isChecked=false
                report_type="mitsumori"
            }
            else if(radioButton==other){
                radioButton.isChecked=true
                defect.isChecked=false
                update.isChecked=false
                mitsumori.isChecked=false
                report_type="other"
            }
        }

        val radioGroup2 = findViewById<RadioGroup>(R.id.group2)
        // 選択項目変更のイベント追加
        // キーボードが出てたら閉じる

        // checkedIdから、選択されたRadioButtonを取得

        // 選択項目変更のイベント追加
        if(answer_type=="perform"){
            radioGroup2.check(defect.id)
        }
        if(answer_type=="cospa"){
            radioGroup2.check(update.id)
        }
        if(answer_type=="otherchoise"){
            radioGroup2.check(mitsumori.id)
        }

        radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(group.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            if(radioButton==perform){
                radioButton.isChecked=true
                cospa.isChecked=false
                otherchoise.isChecked=false
                answer_type="perform"
            }
            else if(radioButton==cospa){
                radioButton.isChecked=true
                perform.isChecked=false
                otherchoise.isChecked=false
                answer_type="cospa"
            }
            else if(radioButton==otherchoise){
                radioButton.isChecked=true
                perform.isChecked=false
                cospa.isChecked=false
                answer_type="otherchoise"
            }
        }

        val radioGroup3 = findViewById<RadioGroup>(R.id.group3)
        // 選択項目変更のイベント追加
        radioGroup3.setOnCheckedChangeListener { group, checkedId ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(group.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            // checkedIdから、選択されたRadioButtonを取得
            val radioButton = findViewById<RadioButton>(checkedId)
            if(old_type=="oldpart_yes"){
                radioGroup3.check(oldpart_yes.id)
            }
            if(old_type=="oldpart_no"){
                radioGroup3.check(oldpart_no.id)
            }
            if(radioButton==oldpart_yes){
                radioButton.isChecked=true
                oldpart_no.isChecked=false
                old_type="oldpart_yes"
            }
            else if(radioButton==oldpart_no){
                radioButton.isChecked=true
                oldpart_yes.isChecked=false
                old_type="oldpart_no"
            }
        }
        //ドロップリスト設定
        val spinner = findViewById<Spinner>(R.id.spinner)
        val index=spinnerItems.binarySearch(part)
        spinner.setSelection(index)
        val adapter = ArrayAdapter(applicationContext,
            android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setPrompt("診断部位一覧")
        // spinner に adapter をセット
        // Kotlin Android Extensions

        spinner.adapter = adapter
        // リスナーを登録
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            //　アイテムが選択された時
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, position: Int, id: Long) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                part=item
            }

            //　アイテムが選択されなかった
            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }
        }
        resisterbutton.setOnClickListener { v->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)


            // タイトルと本文を取得する
            val title = Titile_ditail.text.toString()
            val problem = problem_edit.text.toString()
            val com = com_edit.text.toString()
            val answer_other = type_otheredit.text.toString()
            val yosan = yosan_edit.text.toString()

            val user_name = mCycle.name
            val cycle_name = mCycle.cycle_name
            val shop_code = mCycle.shop_ID
            val cycle_uid = mCycle.cycleUid
            val user_uid = mCycle.uid


            //空欄チェック
            if (title.isEmpty()) {
                // タイトルが入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "タイトルを入力して下さい", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (problem.isEmpty()) {
                // 質問が入力されていない時はエラーを表示するだけ
                Snackbar.make(v, "相談内容を入力して下さい", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (answer_type == "") {
                Snackbar.make(v, "回答タイプを選択して下さい", Snackbar.LENGTH_LONG).show()

                return@setOnClickListener
            }

            if (answer_type == "otherchoise") {
                if (answer_other == "") {
                    Snackbar.make(v, "回答タイプを入力して下さい", Snackbar.LENGTH_LONG).show()
                    return@setOnClickListener
                }

            }

            if (report_type == "") {
                Snackbar.make(v, "レポート内容を選択して下さい", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (part == "") {
                Snackbar.make(v, "相談部位を選択して下さい", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (yosan == "") {
                Snackbar.make(v, "予算を入力して下さい", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Preferenceから名前を取る
            val sp = PreferenceManager.getDefaultSharedPreferences(this)
            val name = sp.getString(NameKEY, "")
            val data = HashMap<String, String>()
            val dataBaseReference = FirebaseDatabase.getInstance().reference
            val genreRef = dataBaseReference.child("report").child("All").child(cycle_uid)

            data["uid"] = user_uid
            data["cycle_name"] = cycle_name
            data["shop_ID"] = shop_code
            data["name"] = name
            data["report_type"] = report_type.toString()
            data["answer_type"] = answer_type.toString()
            data["old_type"] = old_type.toString()
            data["part"] = part
            data["other_request"] = answer_other
            data["problem"] = problem
            data["cycle_uid"] = mCycle.cycleUid
            data["com"] = com
            data["title"] = title
            data["yosan"] = yosan
            data["distance"]=mCycle.distance
            data["area"]=area
            data["day"]=create_day

            // 添付画像を取得する
            val drawable = imageView4.drawable as? BitmapDrawable
            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                data["image"] = bitmapString
            }

            genreRef.setValue(data)
            Toast.makeText(applicationContext, "質問を登録しました", Toast.LENGTH_LONG).show()
            //progressBar.visibility = View.VISIBLE
        }

        finishbutton.setOnClickListener { v ->
            mDatabaseReference = FirebaseDatabase.getInstance().reference
            mAuth = FirebaseAuth.getInstance()
            mReportRef = mDatabaseReference.child("report").child("All").child(mCycle.cycleUid)
            mReportRef!!.removeValue()



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
            imageView4.setImageBitmap(resizedImage)

            mPictureUri = null
        }
    }
    override fun onClick(v: View) {
        if (v === imageView4) {
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
        //progressBar.visibility = View.GONE
        if (databaseError == null) {
            finish()
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show()
        }
    }
}