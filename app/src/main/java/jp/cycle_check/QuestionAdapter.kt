package jp.cycle_check


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

class QuestionAdapter(context: Context) : BaseAdapter() {
    private var vLayoutInflater: LayoutInflater
    private var vCycleinfoArrayList = ArrayList<Questioninfo>()

    init {
        vLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return vCycleinfoArrayList.size

    }

    override fun getItem(position: Int): Any {
        return vCycleinfoArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView

        if (convertView == null) {
            convertView = vLayoutInflater.inflate(R.layout.list_cycle, parent, false)
        }



        val daytext= convertView!!.findViewById<View>(R.id.TypeTextView) as TextView
        daytext.text=vCycleinfoArrayList[position].date

        val titleText = convertView!!.findViewById<View>(R.id.titleTextView) as TextView
        titleText.text = vCycleinfoArrayList[position].title

        val repot = vCycleinfoArrayList[position].report_type

        var type = ""
        if (repot == "defect") {
            type = "不具合相談"
        } else if (repot == "update") {
            type = "アップデート(部品交換)相談"
        } else if (repot == "mitsumori") {
            type = "見積相談"
        } else if (repot == "other") {
            type = "その他相談"
        }


        val resText = convertView.findViewById<View>(R.id.resTextView) as TextView
        resText.text = "登録店：" + vCycleinfoArrayList[position].shop_ID + "\nレポートタイプ：" + type
        val bytes = vCycleinfoArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.findViewById<View>(R.id.imageView2) as ImageView
            imageView.setImageBitmap(image)
        }

        return convertView
    }

    fun setQuestionArrayList(questionArrayList: ArrayList<Questioninfo>) {
        vCycleinfoArrayList = questionArrayList
    }
}