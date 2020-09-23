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
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.solver.widgets.Snapshot
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.app_header_main.toolbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mCycleArrayList: ArrayList<Cycleinfo>
    private lateinit var vCycleArrayList: ArrayList<Questioninfo>
    private lateinit var xCycleArrayList: ArrayList<Answer_question2>
    private lateinit var yCycleArrayList: ArrayList<Answer>
    private lateinit var mAdapter: CycleListAdapter
    private lateinit var  vAdapter:QuestionAdapter
    private lateinit var  yAdapter:AnswerAdapter2
    private lateinit var  xAdapter:AnswerAdapter
    private lateinit var mAuth: FirebaseAuth
    private var mCycleRef: DatabaseReference? = null
    private var vCycleRef: DatabaseReference? = null
    private lateinit var mToolbar: Toolbar
    private var list_type:Int =0

    private val tEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            if(dataSnapshot.value!==null) {
                val map = dataSnapshot.value as Map<String, String>
                var cycle_name = map["cycle_name"] ?: ""
                var shop_ID = map["shop_ID"] ?: ""
                var name = map["name"] ?: ""
                var uid = map["uid"] ?: ""
                var com:String=map["com"]?:""
                var imageString = map["image"] ?: ""
                var distance = map["distance"] ?: "0"
                var answer_type=map["answer_type"]?:""
                var old_type=map["old_type"]?:""
                var other_request=map["other_request"]?:""
                var part=map["part"]?:""
                var problem=map["problem"]?:""
                var report_type=map["report_type"]?:""
                var title=map["title"]?:""
                var yosan=map["yosan"]?:""
                var cycleUid=map["cycle_uid"]?:""
                var area=map["area"]?:""
                var alert=""
                var day=map["day"]?:""
                var report=report_type
                var bytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

                val question =Questioninfo( cycle_name,  shop_ID,  name, uid, com,distance,cycleUid,answer_type, old_type,other_request,part,problem,report_type, title,yosan,area,day,bytes)
                vCycleArrayList.add(question)
                vAdapter.notifyDataSetChanged()
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

    //会頭リストを取得
    private val xEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            if(dataSnapshot.value!==null) {
                val map = dataSnapshot.value as kotlin.collections.Map<String, String>
                var cause = map["cause"] ?: ""
                var com2 = map["com"] ?: ""
                var contact = map["contact"] ?: ""
                var estimate = map["estimate"] ?: ""
                var question_cycleuid = map["question_cycleuid"] ?: ""
                var result = map["result"] ?: ""
                var imageString = map["image"] ?: ""
                var Anbytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }
                val answer =Answer(cause,com2,contact,estimate,question_cycleuid,result,Anbytes)
                yCycleArrayList.add(answer)
                //回答情報から質問を読み込む
                yAdapter.setQuestionArrayList(yCycleArrayList)
                yAdapter.notifyDataSetChanged()
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



    private val sEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            if(dataSnapshot.value!==null) {
                val map = dataSnapshot.value as Map<String, String>
                var key = dataSnapshot.key.toString()
                val user = FirebaseAuth.getInstance().currentUser!!.uid
                val cycle_user = map["uid"] ?: ""

                if (cycle_user == user.toString()) {
                    getQuestion(key)
                }
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

    //自分の自転車リストを取得
    private val vEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            if(dataSnapshot.value!==null) {
                val map = dataSnapshot.value as Map<String, String>
                var key = dataSnapshot.key.toString()
                val user = FirebaseAuth.getInstance().currentUser!!.uid
                val cycle_user = map["uid"] ?: ""

                if (cycle_user == user.toString()) {
                    getcycle(key)
                }
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
            var date:String=map["date"]?:""
            var type:String = map["type"]?:""
            var bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }
            if (date==""){
                date=getNowDate()
            }


            val cycle = Cycleinfo(cycle_name, shop_ID, name, uid,com, alert, report, distance,dataSnapshot.key ?: "",type, date,bytes)
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

        toolbar.setTitle("Main画面")
        setSupportActionBar(toolbar)


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
        vAdapter = QuestionAdapter(this)
        yAdapter = AnswerAdapter2(this)
        xAdapter=AnswerAdapter(this)
        mCycleArrayList = ArrayList<Cycleinfo>()
        vCycleArrayList=ArrayList<Questioninfo>()
        xCycleArrayList=ArrayList<Answer_question2>()
        yCycleArrayList=ArrayList<Answer>()
        mAdapter.notifyDataSetChanged()
        vAdapter.notifyDataSetChanged()
        yAdapter.notifyDataSetChanged()
        // リスト押下で詳細画面を起動する
                mListView.setOnItemClickListener { parent, view, position, id ->
                    if (list_type==3 || list_type==4){
                        val intent = Intent(applicationContext, TabLayoutActivity::class.java)
                        if(list_type==4){
                            intent.putExtra("Q_type",5)
                        }else{
                            intent.putExtra("Q_type",0)
                        }
                        intent.putExtra("Q_Ainfo", vCycleArrayList[position])
                        startActivity(intent)

                    }else if(list_type==5) {
                        val intent = Intent(applicationContext, TabLayoutActivity::class.java)
                        intent.putExtra("Q_type",2)
                        intent.putExtra("A_type",1)
                        intent.putExtra("Q_Ainfo", yCycleArrayList[position])
                        startActivity(intent)

                    }else{
                        val intent = Intent(applicationContext, Detail_bottomActivity::class.java)//DetailActivity
                        intent.putExtra("list_type", list_type)
                        intent.putExtra("cycleinfo", mCycleArrayList[position])
                        startActivity(intent)
                        list_type = 0
                    }
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

        if (id == R.id.action_shop) {
            val intent = Intent(applicationContext, Shop_Info::class.java)
            startActivity(intent)
            return true
        }



        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        mCycleArrayList.clear()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
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
            mAdapter.setCycleArrayList(mCycleArrayList)
            mListView.adapter = mAdapter
            toolbar.setTitle("所有バイク一覧")
            setSupportActionBar(toolbar)
            val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
            fab.setVisibility(View.VISIBLE)

        } else if (id == R.id.nav_q_all) {
            toolbar.setTitle("皆への質問")
            setSupportActionBar(toolbar)
            list_type=3
            vAdapter.setQuestionArrayList(vCycleArrayList)
            mListView.adapter = mAdapter
            val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
            fab.setVisibility(View.INVISIBLE)


        } else if (id == R.id.nav_q_shop) {
            list_type=4
            toolbar.setTitle("お店への質問")
            val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
            fab.setVisibility(View.INVISIBLE)
        }
        else if (id == R.id.nav_a_all) {
            list_type=5
            toolbar.setTitle("自分の回答")

            val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
            fab.setVisibility(View.INVISIBLE)
        }


        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        mToolbar = findViewById(R.id.toolbar)
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        drawer.closeDrawer(GravityCompat.START)
        mCycleArrayList.clear()
        mAdapter.setCycleArrayList(mCycleArrayList)
        vAdapter.setQuestionArrayList(vCycleArrayList)
        mListView.adapter = mAdapter

        if(user!=null) {
            if (list_type == 2) {
                mCycleRef!!.removeEventListener(vEventListener)
                mAdapter.setCycleArrayList(mCycleArrayList)
                mListView.adapter = mAdapter
                mCycleRef = mDatabaseReference.child(CyclePATH)
                mCycleRef!!.addChildEventListener(mEventListener)

            }

            if (list_type == 1) {
                mCycleRef!!.removeEventListener(mEventListener)
                mCycleRef = mDatabaseReference.child(CyclePATH)
                mCycleRef!!.addChildEventListener(vEventListener)
            }
            //皆への質問
            if (list_type == 3) {
                mCycleRef!!.removeEventListener(mEventListener)
                mCycleRef = mDatabaseReference.child("report").child("All")
                mListView.adapter=vAdapter
                mCycleRef!!.addChildEventListener(sEventListener)

            }

            if (list_type == 4) {
                mCycleRef!!.removeEventListener(mEventListener)
                mListView.adapter=vAdapter
                getshopID()

            }

            if (list_type == 5) {
                mCycleRef!!.removeEventListener(mEventListener)
                yCycleArrayList.clear()
                var user = FirebaseAuth.getInstance().currentUser!!.uid
                mCycleRef = mDatabaseReference.child("report").child("Answer").child(user)
                mListView.adapter=yAdapter
                mCycleRef!!.addChildEventListener(xEventListener)
            }

        }
        return true
    }
    fun getshopID(){
        mCycleArrayList.clear()
        vCycleArrayList.clear()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        vCycleRef = mDatabaseReference.child("users").child(uid)
        vCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.value as kotlin.collections.Map<String, String>
                val shop_ID=map["shop_ID"]?:""
                if (shop_ID!=""){
                    var user = FirebaseAuth.getInstance().currentUser!!.uid
                    mCycleRef = mDatabaseReference.child("report").child("shop").child(shop_ID)
                    mCycleRef!!.addChildEventListener(tEventListener)
                }

            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })
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
                var type:String = map["type"]?:""
                var date:String=map["date"]?:""
                var bytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }
                if (date==""){
                    date=getNowDate()
                }

                val cycle = Cycleinfo(cycle_name, shop_ID, name, uid,com, alert, report, distance,snapshot.key ?: "",type, date,bytes)
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

    fun getQuestion(key:String){
        vCycleArrayList.clear()
        vCycleRef = mDatabaseReference.child("report").child("All").child(key)
        vCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = snapshot.value as Map<String, String>
                var cycle_name = map["cycle_name"] ?: ""
                var shop_ID = map["shop_ID"] ?: ""
                var name = map["name"] ?: ""
                var uid = map["uid"] ?: ""
                var com:String=map["com"]?:""
                var imageString = map["image"] ?: ""
                var distance = map["distance"] ?: "0"
                var answer_type=map["answer_type"]?:""
                var old_type=map["old_type"]?:""
                var other_request=map["other_request"]?:""
                var part=map["part"]?:""
                var problem=map["problem"]?:""
                var report_type=map["report_type"]?:""
                var title=map["title"]?:""
                var yosan=map["yosan"]?:""
                var cycleUid=map["cycle_uid"]?:""
                var area=map["area"]?:""
                var alert=""
                var day=map["day"]?:""
                var report=report_type
                var bytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

                val question =Questioninfo( cycle_name,  shop_ID,  name, uid, com,distance,cycleUid,answer_type, old_type,other_request,part,problem,report_type, title,yosan,area,day,bytes)
                vCycleArrayList.add(question)
                vAdapter.notifyDataSetChanged()

            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })
    }
    fun getNowDate(): String {
        val df = SimpleDateFormat("yyyy/MM/dd")
        val date = Date(System.currentTimeMillis())
        return df.format(date)
    }

}




