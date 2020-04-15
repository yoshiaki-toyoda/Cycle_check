package jp.cycle_check

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

private lateinit var mDatabaseReference: DatabaseReference
private lateinit var mListView: ListView
private lateinit var mQuestionArrayList: ArrayList<Question>
private lateinit var vQuestionArrayList: ArrayList<Question>
var yQuestionArrayList= ArrayList<Question>()
//private lateinit var mAdapter: QuestionsListAdapter

private lateinit var mAuth: FirebaseAuth
private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
private lateinit var mLoginListener: OnCompleteListener<AuthResult>
private lateinit var mDataBaseReference: DatabaseReference



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Firebase情報
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        var user=FirebaseAuth.getInstance().currentUser

        //ログイン状態を確認
        if(user==null){
            //Firebaseにログインをしていない場合、ログイン画面に移動
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }



    }
}
