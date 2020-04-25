package jp.cycle_check

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_distance.*


const val EXTRA_TASK = "jp.cycleapp.Cycle"


class DistanceActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private lateinit var sinfo:HashMap<String,String>
    private lateinit var disinfo:HashMap<Int,HashMap<String,String>>
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var cycle:String
    private var mapsize:Int=0
    private lateinit var cycle_uid:String
    private var vCycleRef: DatabaseReference? = null
    var datelist=mutableListOf<String>()
    var timelist=mutableListOf<String>()
    var dislist=mutableListOf<Int>()

    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {

        }
    }

    private lateinit var mTaskAdapter: DisAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance)
        var extras = intent.extras
        cycle = extras.get("cyclename") .toString()
        cycle_uid = extras.get("cycle_uid") .toString()


        floatingActionButton3.setOnClickListener { view ->
            val intent = Intent(this@DistanceActivity, InputActivity::class.java)
            intent.putExtra("cyclename", cycle)
            intent.putExtra("cycle_uid", cycle_uid)
            startActivity(intent)
            reloadListView()
        }

        cycledisbutton.setOnClickListener { view ->
            val intent = Intent(this@DistanceActivity,DistanceGrapActivity::class.java)
            intent.putExtra("cyclename", cycle)
            intent.putExtra("cycle_uid", cycle_uid)
            intent.putExtra("datelist", arrayOf(datelist))
            intent.putExtra("timelist", arrayOf(timelist))
            intent.putExtra("dislist", arrayOf(dislist))
            startActivity(intent)
            reloadListView()

        }

        // Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)
        // ListViewの設定
        mTaskAdapter = DisAdapter(this@DistanceActivity)
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults =
            mRealm.where(Task::class.java).equalTo("cycle_uid",cycle_uid).findAll().sort("date", Sort.DESCENDING)
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()

        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@DistanceActivity,InputActivity::class.java)
            intent.putExtra("cyclename", cycle)
            intent.putExtra("cycle_uid", cycle_uid)
            intent.putExtra(EXTRA_TASK, task.id)

            startActivity(intent)
        }
        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            // ダイアログを表示する
            val builder = AlertDialog.Builder(this)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")
            builder.setPositiveButton("OK") { _, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                reloadListView()
            }
            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

    }

    override fun onResume() {
        super.onResume()
        reloadListView()
    }

    private fun reloadListView() {
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults =
            mRealm.where(Task::class.java).equalTo("cycle_uid", cycle_uid).findAll()
                .sort("date", Sort.DESCENDING)
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
        if (taskRealmResults.size != 0) {
            mDatabaseReference = FirebaseDatabase.getInstance().reference
            vCycleRef = mDatabaseReference.child(RidePath).child(cycle_uid)
            vCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = mutableMapOf<String, MutableList<String>>()
                    var map = snapshot.value as Map<String, HashMap<String, String>>
                    sinfo = HashMap<String, String>()
                    disinfo = HashMap<Int, HashMap<String, String>>()
                    var count: Int = 0

                    for ((k, v: HashMap<String, String>) in map) {
                        for ((j, l) in v) {
                            sinfo[j] = l

                        }
                        for((m,n) in sinfo){
                                    if(m=="time"){
                                        timelist.add(n.toString())
                                    }else if(m=="date"){
                                        datelist.add(n.toString())
                                    }else if(m=="distance"){
                                        dislist.add(n.toInt())
                        }
                    }

                       // datelist.add(disinfo.[count].["date"].toString())
                        //timelist.add(n["time"].toString())
                        //dislist.add(n["distance"]!!.toInt())



                        count = count + 1
                        sinfo.clear()
                    }


                    for ((m:Int,n:HashMap<String, String>) in disinfo) {

                    }

                }



                override fun onCancelled(firebaseError: DatabaseError) {}
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }
        }

