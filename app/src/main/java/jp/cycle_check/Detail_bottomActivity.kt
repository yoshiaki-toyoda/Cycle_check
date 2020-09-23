package jp.cycle_check


import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import jp.cycle_check.ui.Fragment.AnaliseFragment
import jp.cycle_check.ui.Fragment.CycleFragment
import jp.cycle_check.ui.Fragment.CyclingFragment
import jp.cycle_check.ui.Fragment.ReportFragment
import android.util.Base64
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*




private lateinit var mToolbar: Toolbar
lateinit var mCycle: Cycleinfo

class Detail_bottomActivity : AppCompatActivity(),CycleFragment.FragmentListener,CyclingFragment.FragmentListener,AnaliseFragment.FragmentListener,ReportFragment.FragmentListener{
    private lateinit var vCycleArrayList: ArrayList<Questioninfo>
    private var mCycleRef: DatabaseReference? = null
    private var vCycleRef: DatabaseReference? = null
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mCycleArrayList: ArrayList<Cycleinfo>
    private lateinit var xCycleArrayList: ArrayList<Answer_question2>
    private lateinit var yCycleArrayList: ArrayList<Answer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_detail_bottom)
        vCycleArrayList=ArrayList<Questioninfo>()
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        //基本情報取得
        val extras = intent.extras
        mCycle = extras.get("cycleinfo") as Cycleinfo
        yCycleArrayList=ArrayList<Answer>()
        vCycleArrayList=ArrayList<Questioninfo>()
        xCycleArrayList=ArrayList<Answer_question2>()

        var bottomNavigationView =findViewById(R.id.nav_view) as BottomNavigationView
        val mOnNavigationItemSelectedListener =bottomNavigationView.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {
                R.id.navigation_cycle -> {
                    supportFragmentManager.beginTransaction()
                    //Fragmentに基本情報を渡す
                    val args = Bundle()
                    args.putSerializable("CycleInfo", mCycle)
                    // FragmentTransactionを生成。
                    val transaction = supportFragmentManager.beginTransaction()
                    // TestFragmentを生成。
                    val fragment = CycleFragment()
                    //Fragmetnに渡す変数をセット
                    fragment.setArguments(args)
                    // FragmentTransactionに、TestFragmentをセット
                    transaction.replace(R.id.frameLayout, fragment)
                    // FragmentTransactionをコミット
                    transaction.commit()
                    //ツールバー表示

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_cycling -> {
                    supportFragmentManager.beginTransaction()
                    //Fragmentに基本情報を渡す
                    val args = Bundle()
                    args.putSerializable("CycleInfo", mCycle)
                    // FragmentTransactionを生成。
                    val transaction = supportFragmentManager.beginTransaction()
                    // TestFragmentを生成。
                    val fragment = CyclingFragment()
                    //Fragmetnに渡す変数をセット
                    fragment.setArguments(args)
                    // FragmentTransactionに、TestFragmentをセット
                    transaction.replace(R.id.frameLayout, fragment)
                    // FragmentTransactionをコミット
                    transaction.commit()
                        //ツールバー表示
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_analise -> {
                    supportFragmentManager.beginTransaction()
                    //Fragmentに基本情報を渡す
                    val args = Bundle()
                    args.putSerializable("CycleInfo", mCycle)
                    // FragmentTransactionを生成。
                    val transaction = supportFragmentManager.beginTransaction()
                    // TestFragmentを生成。
                    val fragment = AnaliseFragment()
                    //Fragmetnに渡す変数をセット
                    fragment.setArguments(args)
                    // FragmentTransactionに、TestFragmentをセット
                    transaction.replace(R.id.frameLayout, fragment)
                    // FragmentTransactionをコミット
                    transaction.commit()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_report -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout,ReportFragment())
                        .commit()
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
        supportFragmentManager.beginTransaction().apply {
            supportFragmentManager.primaryNavigationFragment?.let {
                // すでに primaryNavigationFragment が set されている場合は detach
                // detach された Fragment の View は破棄されるが childFragmentManager の backStack などは保持される
                detach(it)
            }
        }
    }

    //パーツ確認ボタン押下時のアクション
    override fun onClickButton() {
        val intent = Intent(applicationContext, PartresisterActivity::class.java)
        intent.putExtra("cycle", mCycle.uid)
        startActivity(intent)
    }

    override fun onDelete() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }

    override fun add_distance() {
        val intent = Intent(applicationContext, InputActivity::class.java)
        intent.putExtra("cyclename", mCycle.cycle_name)
        intent.putExtra("cycle_uid", mCycle.cycleUid)
        startActivity(intent)
    }

    override fun intent_graph() {

        val intent = Intent(applicationContext, DistanceGrapActivity::class.java)
        intent.putExtra("cyclename", mCycle.cycle_name)
        intent.putExtra("cycle_uid", mCycle.cycleUid)
        startActivity(intent)
    }

    override fun list_tap(task: Task) {

        val intent = Intent(applicationContext, InputActivity::class.java)
        intent.putExtra("cyclename", mCycle.cycle_name)
        intent.putExtra("cycle_uid", mCycle.cycleUid)
        intent.putExtra(EXTRA_TASK, task.id)
        startActivity(intent)
    }

    override fun shop_question() {
        val intent = Intent(applicationContext,QuestionActivity::class.java)
        intent.putExtra("cycleinfo", mCycle)
        startActivity(intent)
    }

    override fun question_all() {
        val intent = Intent(applicationContext,QuestionAllActivity::class.java)
        intent.putExtra("cycleinfo", mCycle)
        startActivity(intent)
    }

    override fun answer_shop() {
        var user = FirebaseAuth.getInstance().currentUser!!.uid
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mCycleRef = mDatabaseReference.child("report").child("Answer").child(user)
        mCycleRef!!.addChildEventListener(xEventListener)

        getshopQuestion(mCycle.shop_ID, mCycle.cycleUid)
    }

    override fun answer_all() {
        val intent = Intent(applicationContext,Question_AnswerAll_Activity::class.java)
        intent.putExtra("cycleinfo", mCycle)
        startActivity(intent)
    }

    override fun change_shop() {
        val intent = Intent(applicationContext,ShopChange_Activity::class.java)
        intent.putExtra("cycleinfo", mCycle)
        startActivity(intent)
    }

    override fun shop_info() {
        val intent = Intent(applicationContext,Shop_info2_Activity::class.java)
        intent.putExtra("cycleinfo", mCycle)
        startActivity(intent)
    }

    fun getshopQuestion(key:String,cycleuid:String){
        vCycleArrayList.clear()
        vCycleRef = mDatabaseReference.child("report").child("shop").child(key).child(cycleuid)
        vCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    val map = snapshot.value as Map<String, String>
                    var cycle_name = map["cycle_name"] ?: ""
                    var shop_ID = map["shop_ID"] ?: ""
                    var name = map["name"] ?: ""
                    var uid = map["uid"] ?: ""
                    var com: String = map["com"] ?: ""
                    var imageString = map["image"] ?: ""
                    var distance = map["distance"] ?: "0"
                    var answer_type = map["answer_type"] ?: ""
                    var old_type = map["old_type"] ?: ""
                    var other_request = map["other_request"] ?: ""
                    var part = map["part"] ?: ""
                    var problem = map["problem"] ?: ""
                    var report_type = map["report_type"] ?: ""
                    var title = map["title"] ?: ""
                    var yosan = map["yosan"] ?: ""
                    var cycleUid = map["cycle_uid"] ?: ""
                    var area = map["area"] ?: ""
                    var alert = ""
                    var day=map["day"]?:""
                    var report = report_type
                    var bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    val question = Questioninfo(
                        cycle_name,
                        shop_ID,
                        name,
                        uid,
                        com,
                        distance,
                        cycleUid,
                        answer_type,
                        old_type,
                        other_request,
                        part,
                        problem,
                        report_type,
                        title,
                        yosan,
                        area,
                        day,
                        bytes
                    )

                    val intent = Intent(applicationContext, TabLayoutActivity::class.java)
                    intent.putExtra("Q_Ainfo", question)
                    intent.putExtra("Q_type", 5)
                    startActivity(intent)
                }
            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })
    }
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
                val imageString=map["image"]?:""
                var Anbytes =
                    if (imageString.isNotEmpty()) {
                        Base64.decode(imageString, Base64.DEFAULT)
                    } else {
                        byteArrayOf()
                    }

                val answer =Answer(cause,com2,contact,estimate,question_cycleuid,result,Anbytes)

                yCycleArrayList.add(answer)

                //回答情報から質問を読み込む

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
