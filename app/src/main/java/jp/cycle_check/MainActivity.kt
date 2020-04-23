package jp.cycle_check

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.solver.widgets.Snapshot
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mCycleArrayList: ArrayList<Cycleinfo>
    private lateinit var mAdapter: CycleListAdapter

    private lateinit var mAuth: FirebaseAuth
    private var mCycleRef: DatabaseReference? = null
    private var vCycleRef: DatabaseReference? = null
    private lateinit var mToolbar: Toolbar
    private var list_type:Int =0

    //自分の自転車リストを取得
    private val vEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            var key = dataSnapshot.key.toString()
            val user = FirebaseAuth.getInstance().currentUser!!.uid
            val cycle_user =map["uid"]?: ""

            if(cycle_user==user.toString()){
                getcycle(key)
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


    //Firebaseのリスナー 全てのデータ取得
    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            var cycle_name = map["cycle_name"] ?: ""
            var shop_ID = map["shop_ID"] ?: ""
            var name = map["name"] ?: ""
            var uid = map["uid"] ?: ""
            var com:String=map["com"]?:""
            var alert = map["alert"] ?: ""
            var report = map["report"] ?: ""
            var imageString = map["image"] ?: ""
            var distance = map["distance"] ?: "0"
            var bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            /* val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }   class Cycleinfo(val cycle_name: String, val shop_ID: String, val name: String, val uid: String, val alert:String, val report:String,val distance:Int, val cycleUid:String,  bytes: ByteArray)
*/
            val cycle = Cycleinfo(cycle_name, shop_ID, name, uid,com, alert, report, distance, dataSnapshot.key ?: "", bytes)
            mCycleArrayList.add(cycle)
            mAdapter.notifyDataSetChanged()
            cycle_name = "None"
            shop_ID ="None"
            name =  "None"
            uid =  "None"
            com="None"
            alert = "None"
            report = "None"
            imageString = "None"
            distance = "None"

            bytes =byteArrayOf()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            /* for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }*/
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //mToolbar = findViewById(R.id.toolbar)
        //setSupportActionBar(mToolbar)

        //Firebase情報
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        var user = FirebaseAuth.getInstance().currentUser

        //ログイン状態を確認
        if (user == null) {
            //Firebaseにログインをしていない場合、ログイン画面に移動
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
        }

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = CycleListAdapter(this)
        mCycleArrayList = ArrayList<Cycleinfo>()
        mAdapter.notifyDataSetChanged()

        // リスト押下で詳細画面を起動する
        mListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(applicationContext, DetailActivity::class.java)
            intent.putExtra("cycleinfo", mCycleArrayList[position])
            list_type == 0
            startActivity(intent)

        }

        //アクションボタン押下で車体登録ページに遷移
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        fab.setOnClickListener { _ ->
            val intent = Intent(applicationContext, CycleresisterActivity::class.java)
            startActivity(intent)
        }

        // ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        navigationView.setOnClickListener() {
            var user = FirebaseAuth.getInstance().currentUser
            val navigationView = findViewById<NavigationView>(R.id.nav_view)
                navigationView.getMenu().clear();
                navigationView.inflateMenu(R.menu.activity_main_drawer)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        mAdapter.setQuestionArrayList(mCycleArrayList)
        mListView.adapter = mAdapter

        mCycleRef = mDatabaseReference.child(CyclePATH)
        //mCycleRef!!.addChildEventListener(vEventListener)
        // 1:自分のページを既定の選択とする
        if(list_type == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }

    }

    @SuppressLint("RestrictedApi")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val user = FirebaseAuth.getInstance().currentUser

        if (id == R.id.nav_own) {
            list_type=1
            val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
            fab.setVisibility(View.VISIBLE)
        } else if (id == R.id.nav_all) {
            list_type=2
            val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
                fab.setVisibility(View.INVISIBLE)

        } else if (id == R.id.nav_shop) {
            list_type=3
            val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
            fab.setVisibility(View.INVISIBLE)
        } else if (id == R.id.nav_board) {
            list_type=4
            val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
            fab.setVisibility(View.INVISIBLE)
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        mCycleArrayList.clear()
        mAdapter.setQuestionArrayList(mCycleArrayList)
        mListView.adapter = mAdapter

        if(user!=null) {
            if (list_type == 2) {
                mCycleRef!!.removeEventListener(vEventListener)
                mAdapter.setQuestionArrayList(mCycleArrayList)
                mListView.adapter = mAdapter

                mCycleRef = mDatabaseReference.child(CyclePATH)
                mCycleRef!!.addChildEventListener(mEventListener)

            }

            if (list_type == 1) {
                mCycleRef!!.removeEventListener(mEventListener)
                mCycleRef = mDatabaseReference.child(CyclePATH)
                mCycleRef!!.addChildEventListener(vEventListener)
            }

        }
        return true
    }

    fun getcycle(key:String){
        mCycleArrayList.clear()
        vCycleRef = mDatabaseReference.child(CyclePATH).child(key)
        vCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.value as Map<String, String>
                var cycle_name = map["cycle_name"] ?: ""
                var shop_ID = map["shop_ID"] ?: ""
                var name = map["name"] ?: ""
                var uid = map["uid"] ?: ""
                var com:String=map["com"]?:""
                var alert = map["alert"] ?: ""
                var report = map["report"] ?: ""
                var imageString = map["image"] ?: ""
                var distance = map["distance"] ?: "0"
                var bytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

                val cycle = Cycleinfo(cycle_name, shop_ID, name, uid,com, alert, report, distance, snapshot.key ?: "", bytes)
                mCycleArrayList.add(cycle)
                mAdapter.notifyDataSetChanged()
                cycle_name = "None"
                shop_ID ="None"
                name =  "None"
                uid =  "None"
                com="None"
                alert = "None"
                report = "None"
                imageString = "None"
                distance = "None"
                bytes =byteArrayOf()


            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })
    }



}




