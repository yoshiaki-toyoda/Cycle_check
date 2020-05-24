package jp.cycle_check

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.emailText
import kotlinx.android.synthetic.main.activity_login.passwordText
import kotlinx.android.synthetic.main.activity_login.progressBar
import kotlinx.android.synthetic.main.activity_resister.*
import java.util.HashMap
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*


class UserresisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference
    var login_state:Int=0
    var loginsave:Int=0
    var sex:Int=0


    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    private var mIsCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resister)

        //Firebase関連
        mDataBaseReference = FirebaseDatabase.getInstance().reference
        // FirebaseAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance()
        // アカウント作成処理のリスナー


        mCreateAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) { // 成功した場合 ログインを行う
                val email = emailText.text.toString()
                val password = passwordText.text.toString()
                login(email, password)
            } else { // 失敗した場合 エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE// プログレスバーを非表示にする
            }
        }
        // ログイン処理のリスナー
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) { // 成功した場合
                val user = mAuth.currentUser
                val userRef = mDataBaseReference.child(UsersPATH).child(user!!.uid)
                login_state=1//ログイン成功フラグ
                if (mIsCreateAccount) { // アカウント作成の時は表示名をFirebaseに保存する
                    val name = userNameText.text.toString()//アカウント作成画面からニックネーム取得
                    val age=ageText.text.toString()
                    val exp=expText.text.toString()
                    val area=userereaText.text.toString()
                    val data = HashMap<String, String>()
                    data["name"] = name
                    data["age"]=age
                    data["exp"]=exp
                    data["area"]=area

                    if(sex==0){
                        data["sex"]="Male"
                    }else{
                        data["sex"]="Female"
                    }
                    userRef.setValue(data)// Firebaseにアップロード
                    saveName(name,age, data!!["sex"]as String,exp,area)  // 表示名をpreferenceに保存する
                } else {
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            saveName(data!!["name"] as String,data!!["age"] as String,data!!["sex"] as String,data!!["exp"] as String,data!!["area"]as String)
                        }
                        override fun onCancelled(firebaseError: DatabaseError) {}
                    })
                }
                progressBar.visibility = View.GONE// プログレスバーを非表示にする
                finish()// Activityを閉じる

            } else { // 失敗した場合 エラーを表示する
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()
                progressBar.visibility = View.GONE // プログレスバーを非表示にする
            }
        }
        //登録ボタン押下時
        startresisterbutton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = userNameText.text.toString()
            val sex_r:String
            val age=ageText.text.toString()

            if(sex==0){
               sex_r = "Male"
            }else{
               sex_r="Female"
            }

            if (email.length != 0 && password.length >= 6 && name.length != 0 &&sex_r.length >3 && age !=null) {
                // ログイン時に表示名を保存するようにフラグを立てる
                mIsCreateAccount = true
                createAccount(email, password)
            } else {
                // エラーを表示する
                Snackbar.make(v, "全項目正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }
        //ラジオボタン押下時の操作
        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        // 選択項目変更のイベント追加
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // checkedIdから、選択されたRadioButtonを取得
            val radioButton = findViewById<RadioButton>(checkedId)
            if(radioButton==man){
                sex=0
            }
            else{
                sex=1
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        // プログレスバーを表示する
        progressBar.visibility = View.VISIBLE
        // アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)
    }
    private fun saveName(name: String,age:String,sex:String,exp:String,area:String) {
        // Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.putString(AgeKey, age)
        editor.putString(SexKey, sex)
        editor.putString(ExpKey, exp)
        editor.putString(AreaKEY, area)
        editor.commit()
    }
    private fun login(email: String, password: String) {
        progressBar.visibility = View.VISIBLE// プログレスバーを表示する
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)// ログインする
    }


}