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

import java.util.ArrayList

class CycleListAdapter(context: Context) : BaseAdapter(){
    private var mLayoutInflater: LayoutInflater
    private var mCycleinfoArrayList = ArrayList<Cycleinfo>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mCycleinfoArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mCycleinfoArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_cycle, parent, false)
        }

        val titleText = convertView!!.findViewById<View>(R.id.titleTextView) as TextView
        titleText.text = mCycleinfoArrayList[position].cycle_name

        val nameText = convertView.findViewById<View>(R.id.nameTextView) as TextView
        // Preferenceから表示名を取得してEditTextに反映させる

        nameText.text = "ユーザー名:\n" +mCycleinfoArrayList[position].name

        val resText = convertView.findViewById<View>(R.id.resTextView) as TextView
        resText.text ="走行距離："+mCycleinfoArrayList[position].distance+"Km"

        val reportText = convertView.findViewById<View>(R.id.ReportTextView) as TextView
        reportText.text = "購入店："+mCycleinfoArrayList[position].shop_ID

        val bytes =mCycleinfoArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.findViewById<View>(R.id.imageView2) as ImageView
            imageView.setImageBitmap(image)
        }

        return convertView
    }

    fun setQuestionArrayList(cycleArrayList: ArrayList<Cycleinfo>) {
        mCycleinfoArrayList= cycleArrayList
    }
}