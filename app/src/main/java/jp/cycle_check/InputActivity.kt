package jp.cycle_check
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_input.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class InputActivity : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null
    var key:String=""
    var distance:String="0"
    private lateinit var mCycle: Cycleinfo
    private lateinit var mCycleArrayList: ArrayList<Cycleinfo>
    private lateinit var cycle:String
    private lateinit var cycle_uid:String
    private var vGenreRef: DatabaseReference? = null
    private lateinit var mDatabaseReference: DatabaseReference
    var totaldistance:Int=0
    var dateString:String=""
    var timeString:String=""
    var input_type:Int=0


    private val mOnDateClickListener = View.OnClickListener {v ->
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    private val mOnTimeClickListener = View.OnClickListener { v ->

        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, true)
        timePickerDialog.show()
    }

    private val mOnDoneClickListener = View.OnClickListener {v ->
        if ( dis_edit.text.toString()!="" && times_button.text!=null) {
            val data = java.util.HashMap<String, Any>()
            data["date"] = date_button.text.toString()
            data["time"] = times_button.text.toString()
            data["distance"] = dis_edit.text.toString()
            mDatabaseReference = FirebaseDatabase.getInstance().reference
            //Firebaseに走行距離を入れる
            vGenreRef = mDatabaseReference.child(RidePath).child(cycle_uid)
            if (input_type==1) {
                key = vGenreRef!!.push().key.toString()
                vGenreRef!!.child(key).setValue(data)
            } else if(input_type==2) {
                vGenreRef!!.child(mTask!!.key).setValue(data)
            }
            addTask()
            finish()
        }else{
            Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show()
        }
    }
    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_input)
        var extras = intent.extras
        cycle = extras.get("cyclename") .toString()
        cycle_uid = extras.get("cycle_uid") .toString()
        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()

        realm.close()

        //test
        button.setOnClickListener{v: View? ->
            val intent = Intent(applicationContext, GpsActivity::class.java)
            startActivity(intent)
        }


        //初期入力

        if (mTask == null) {
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
            cycle_nameText.text=cycle
            input_type=1


        } else {
            // 更新の場合
            content_edit_text.setText(mTask!!.contents)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
            input_type=2
            key=mTask!!.key

            title_edit_text.setText(mTask!!.title)
            dis_edit.setText(mTask!!.distance.toString())
            cycle_nameText.text=mTask!!.cycle_name
            date_button.text = dateString
            times_button.text = timeString

        }
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier

        }

        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val distext=dis_edit.text.toString()
        mTask!!.key=key
        mTask!!.cycle_name=cycle
        mTask!!.cycle_uid=cycle_uid
        mTask!!.title = title
        mTask!!.contents = content
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        var time = mHour.toInt()*60 + mMinute.toInt()
        mTask!!.cycle_time=time
        mTask!!.date = date
        mTask!!.distance = distext.toInt()

        realm.copyToRealmOrUpdate(mTask!!)
        realm.commitTransaction()

        realm.close()

    }


}