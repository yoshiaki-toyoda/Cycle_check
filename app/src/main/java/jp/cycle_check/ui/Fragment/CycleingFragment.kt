package jp.cycle_check.ui.Fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import jp.cycle_check.*
import jp.cycle_check.R
import kotlinx.android.synthetic.main.activity_distance.*
import kotlinx.android.synthetic.main.app_header_main.*
import java.util.HashMap

class CyclingFragment : Fragment() {
    private var mListener: CyclingFragment.FragmentListener? = null
    private lateinit var mTaskAdapter: DisAdapter
    private lateinit var mRealm: Realm
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var cycle: String
    private lateinit var cycle_uid: String
    private var vCycleRef: DatabaseReference? = null
    var mCycle: Cycleinfo? = null

    interface FragmentListener {
        fun add_distance()
        fun intent_graph()
        fun list_tap(task: Task)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentListener) {
            mListener = context
        }
    }

    fun addFragment() {
        mListener?.add_distance()
    }

    fun addFragment1() {
        mListener?.intent_graph()
    }

    fun addFragment2(task: Task) {
        mListener?.list_tap(task)
    }

    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(element: Realm) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCycle = arguments!!.getSerializable("CycleInfo") as Cycleinfo
        cycle = mCycle!!.cycle_name
        cycle_uid = mCycle!!.cycleUid

        // Realmの設定
        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)
        // ListViewの設定
        mTaskAdapter = DisAdapter(context!!)
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults =
            mRealm.where(Task::class.java).equalTo("cycle_uid", cycle_uid).findAll()
                .sort("date", Sort.DESCENDING)
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cycling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle("走行記録")
        floatingActionButton3.setOnClickListener { view ->
            addFragment()
        }

        listView1.setOnItemClickListener { parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            addFragment2(task)
        }

        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task = parent.adapter.getItem(position) as Task
            // ダイアログを表示する
            val builder = AlertDialog.Builder(context)
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

    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
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

    fun lisner() {
        var totaldis: Int = 0
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        vCycleRef = mDatabaseReference.child(RidePath).child(cycle_uid)
        vCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null) {
                    var map = snapshot.value as HashMap<String, HashMap<Any, Any>>
                    var sinfo = java.util.HashMap<Any, Any>()
                    var count: Int = 0

                    for ((k, v: HashMap<Any, Any>) in map.toSortedMap()) {
                        for ((j, l) in v) {
                            sinfo[j] = l
                        }
                        for ((m, n) in sinfo) {
                            if (m == "distance") {
                                totaldis = totaldis + n.toString().toInt()
                            }
                        }
                        count = count + 1
                        sinfo.clear()
                    }
                    val mCycleRef = mDatabaseReference.child(CyclePATH).child(cycle_uid)
                    val dataC = HashMap<String, Any>()
                    dataC.put("distance", totaldis.toString())
                    mCycleRef.updateChildren(dataC)
                    //distanceだけ変える
                }
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

    }
}