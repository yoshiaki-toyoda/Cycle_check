package jp.cycle_check


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ListView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.app_header_main.*
import java.util.HashMap

class Question_AnswerAll_Activity : AppCompatActivity() {
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mCycleArrayList: ArrayList<Cycleinfo>
    private lateinit var vCycleArrayList: ArrayList<Questioninfo>
    private lateinit var xCycleArrayList: ArrayList<Answer_question2>
    private lateinit var yCycleArrayList: ArrayList<Answer>
    private lateinit var mAdapter: CycleListAdapter
    private lateinit var  vAdapter:QuestionAdapter
    private lateinit var  xAdapter:AnswerAdapter2
    private lateinit var  mCycle:Cycleinfo
    private lateinit var mAuth: FirebaseAuth
    private var mCycleRef: DatabaseReference? = null
    private var vCycleRef: DatabaseReference? = null
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_header_main)

        toolbar.setTitle("回答一覧")
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        fab.setVisibility(View.INVISIBLE)
        val extras = intent.extras
        mCycle = extras.get("cycleinfo") as Cycleinfo
        // ListViewの準備
        mListView = findViewById(R.id.listView)
        xAdapter=AnswerAdapter2(this)
        mCycleArrayList = ArrayList<Cycleinfo>()
        vCycleArrayList=ArrayList<Questioninfo>()
        xCycleArrayList=ArrayList<Answer_question2>()
        yCycleArrayList=ArrayList<Answer>()

        mListView.adapter = xAdapter
        xAdapter.setQuestionArrayList(yCycleArrayList)
        xAdapter.notifyDataSetChanged()


        var user = FirebaseAuth.getInstance().currentUser!!.uid
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mCycleRef = mDatabaseReference.child("report").child("Answer")
        mCycleRef!!.addChildEventListener(xEventListener)


        // リスト押下で詳細画面を起動する
        mListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, TabLayoutActivity::class.java)
            intent.putExtra("Q_Ainfo", yCycleArrayList[position])
            intent.putExtra("Q_type", 2)
            intent.putExtra("A_type", 0)
            startActivity(intent)
        }


    }
    override fun onResume() {
        super.onResume()
        mCycleRef = mDatabaseReference.child(CyclePATH)
        //mCycleRef!!.addChildEventListener(vEventListener)

    }

    //child(key)を消してcycleuidを探す
    private val xEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            if(dataSnapshot.value!==null) {
                val map = dataSnapshot.value as HashMap<String, HashMap<String, String>>

                for ((k, v: HashMap<String, String>) in map.toSortedMap()) {
                    if(k== mCycle.cycleUid){
                        var cause = v["cause"] ?: ""
                        var com2 = v["com"] ?: ""
                        var contact = v["contact"] ?: ""
                        var estimate = v["estimate"] ?: ""
                        var question_cycleuid = v["question_cycleuid"] ?: ""
                        var result = v["result"] ?: ""
                        val imageString=v["image"]?:""

                        var Anbytes =
                            if (imageString.isNotEmpty()) {
                                Base64.decode(imageString, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }

                        val answer =Answer(cause,com2,contact,estimate,question_cycleuid,result,Anbytes)
                        yCycleArrayList.add(answer)
                        }
                }
                xAdapter.setQuestionArrayList(yCycleArrayList)
                xAdapter.notifyDataSetChanged()
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }
}