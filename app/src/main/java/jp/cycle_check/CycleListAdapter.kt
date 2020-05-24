package jp.cycle_check

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class CycleListAdapter(context: Context) : BaseAdapter(){
    private var mLayoutInflater: LayoutInflater
    private var mCycleinfoArrayList = ArrayList<Cycleinfo>()
    private var vCycleinfoArrayList=ArrayList<Questioninfo>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
            return mCycleinfoArrayList.size
            return vCycleinfoArrayList.size


    }

    override fun getItem(position: Int): Any {
        return mCycleinfoArrayList[position]
        return vCycleinfoArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_cycle, parent, false)
        }

        if(mCycleinfoArrayList!==null) {


            val titleText = convertView!!.findViewById<View>(R.id.titleTextView) as TextView
            titleText.text ="名称"+ mCycleinfoArrayList[position].cycle_name

            val nameText = convertView.findViewById<View>(R.id.nameTextView) as TextView
            nameText.text = "ユーザー名:" + mCycleinfoArrayList[position].name

            val resText = convertView.findViewById<View>(R.id.resTextView) as TextView
            resText.text = "走行距離：" + mCycleinfoArrayList[position].distance + "Km"

            val shopText = convertView.findViewById<View>(R.id.ReportTextView) as TextView
            shopText.text = "登録店："+mCycleinfoArrayList[position].shop_ID

            val dateText = convertView.findViewById<View>(R.id.ReportdayTextView) as TextView
            dateText.text="登録日:"+mCycleinfoArrayList[position].date

            val typeText = convertView.findViewById<View>(R.id.TypeTextView) as TextView
            typeText.text="タイプ:"+mCycleinfoArrayList[position].type


            val bytes = mCycleinfoArrayList[position].imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView2) as ImageView
                imageView.setImageBitmap(image)
            }


        }

        return convertView
    }
    fun setCycleArrayList(cycleArrayList: ArrayList<Cycleinfo>) {
        mCycleinfoArrayList= cycleArrayList
    }
    fun getNowDate(): String {
        val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date = Date(System.currentTimeMillis())
        return df.format(date)
    }
}