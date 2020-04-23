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
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var cycle:String
    private lateinit var cycle_uid:String
    private var vCycleRef: DatabaseReference? = null
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
            mRealm.where(Task::class.java).equalTo("cycle_uid",cycle_uid).findAll().sort("date", Sort.DESCENDING)
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        // TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()

        mDatabaseReference = FirebaseDatabase.getInstance().reference
        vCycleRef = mDatabaseReference.child(RidePath).child(cycle_uid)
        vCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = mutableMapOf<String,MutableList<String>>()
                val map = snapshot.value as Map<String, HashMap<String,String>>

                for((k,v:HashMap<String,String>)in map){
                    for((j,l) in v){
                        if (j=="time"){
                            var cycletime=l
                        }

                    }

                }












            }
            override fun onCancelled(firebaseError: DatabaseError) {}
        })
    }


    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }
}
