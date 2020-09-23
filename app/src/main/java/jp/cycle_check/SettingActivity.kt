package jp.cycle_check

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_setting.*
import kotlin.collections.HashMap

class SettingActivity : AppCompatActivity() {
    private lateinit var mDataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)


        // Preferenceから表示名を取得してEditTextに反映させる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY, "")
        val area = sp.getString(AreaKEY, "")
        val exp = sp.getString(ExpKey, "0")

        userNameText.setText(name)
        userereaText.setText(area)
        //expText.setText(exp.toInt())


        mDataBaseReference = FirebaseDatabase.getInstance().reference


        changeButton.setOnClickListener{v ->
            // キーボードが出ていたら閉じる
            val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていない場合は何もしない
                Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show()
            } else {
                // 変更した表示名をFirebaseに保存する
                val name = userNameText.text.toString()
                val place=userereaText.text.toString()
                val exp=expText.text.toString()
                val userRef = mDataBaseReference.child(UsersPATH).child(user!!.uid)
                val data = HashMap<String, Any>()
                    data.put("name",name)
                    data.put("area",place)
                    data.put("exp",exp)
                    userRef.updateChildren(data)

                // 変更した表示名をPreferenceに保存する
                val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = sp.edit()
                editor.putString(NameKEY, name)
                editor.putString(AreaKEY, place)
                editor.commit()

                Snackbar.make(v, "登録情報を変更しました", Snackbar.LENGTH_LONG).show()
            }
        }

        logoutButton.setOnClickListener { v ->
            FirebaseAuth.getInstance().signOut()
            userNameText.setText("")
            Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show()
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
