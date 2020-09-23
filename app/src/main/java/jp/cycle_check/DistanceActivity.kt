package jp.cycle_check


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.*

import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import android.app.AlertDialog
import com.google.android.material.snackbar.Snackbar

import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_distance.*
import java.util.HashMap

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

        cycledisbutton.setOnClickListener { view ->
            if (mTaskAdapter!==null) {
                val intent = Intent(this@DistanceActivity, DistanceGrapActivity::class.java)
                intent.putExtra("cyclename", cycle)
                intent.putExtra("cycle_uid", cycle_uid)
                startActivity(intent)
                reloadListView()
            }
            Snackbar.make(view, "走行記録がありません", Snackbar.LENGTH_LONG).show()
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
        lisner()

    }

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }
    fun lisner(){

        var totaldis:Int=0
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        vCycleRef = mDatabaseReference.child(RidePath).child(cycle_uid)
        vCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var map = snapshot.value as HashMap<String, HashMap<Any, Any>>
                var sinfo = java.util.HashMap<Any, Any>()
                var count: Int = 0

                for ((k, v: HashMap<Any, Any>) in map.toSortedMap()) {
                    for ((j, l) in v) {
                        sinfo[j] = l
                    }
                    for((m,n) in sinfo){
                        if(m=="distance"){
                            totaldis=totaldis+n.toString().toInt()
                        }
                    }
                    count = count + 1
                    sinfo.clear()
                }
                val mCycleRef = mDatabaseReference.child(CyclePATH).child(cycle_uid)
                val dataC = HashMap<String, Any>()
                dataC.put("distance",totaldis.toString())
                mCycleRef.updateChildren(dataC)
                //distanceだけ変える
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

    }


}



