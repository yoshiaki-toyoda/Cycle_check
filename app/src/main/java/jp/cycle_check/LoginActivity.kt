package jp.cycle_check

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import java.util.HashMap

class LoginActivity : AppCompatActivity() {
    //Firebase観点
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference
    private var mIsCreateAccount = false// アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    var login_state:Int=0
    var loginsave:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Firebase関連
        mDataBaseReference = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        //端末にmailとパスが保存されていれば、自動入力する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val email = sp.getString(MailKEY, "")
        val pass = sp.getString(PassKEY, "")
        if(email !==""&&pass !=="") {
            emailText.setText(email)
            passwordText.setText(pass)
        }

        // ログイン処理のリスナー
        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) { // 成功した場合
                val user = mAuth.currentUser
                val userRef = mDataBaseReference.child(UsersPATH).child(user!!.uid)

                login_state=1//ログイン成功フラグ
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {

                        }
                        override fun onCancelled(firebaseError: DatabaseError) {}
                    })
                progressBar.visibility = View.GONE// プログレスバーを非表示にする
                finish() // Activityを閉じる
            } else { // 失敗した場合
                val view = findViewById<View>(android.R.id.content) // エラーを表示する
                Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show()
                // プログレスバーを非表示にする
                progressBar.visibility = View.GONE
            }
        }

        loginButton.setOnClickListener { v ->//loginボタン押下時の処理
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            if (email.length != 0 && password.length >= 6) { // フラグを落としておく
                mIsCreateAccount = false
                login(email, password)
                //チェックが入っている場合、IDとPassをpreferenceに保存する
                if(loginsave==1){
                    saveID(email, password)
                }
            } else { // エラーを表示する
                Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
            }
        }
        //アカウント作成ボタン押下時
        createButton.setOnClickListener { v ->
            // キーボードが出てたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            //アカウント作成Activityに移動
            val intent = Intent(applicationContext,UserresisterActivity::class.java)
            startActivity(intent)

        }

        //チェック押下時
        checkBox2.setOnClickListener(View.OnClickListener {
            val check = checkBox2.isChecked()
            if (check) {//チェックが入っていた場合、フラグを立てる
                loginsave=1
            }
        })
    }

    private fun login(email: String, password: String) {
        progressBar.visibility = View.VISIBLE// プログレスバーを表示する
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(mLoginListener)// ログインする
    }

    private fun saveID(mail:String,pass:String) {
        // Preferenceに保存する
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(MailKEY, mail)
        editor.putString(PassKEY, pass)
        editor.commit()

    }



}

