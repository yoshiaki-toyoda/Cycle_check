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

class AnswerAdapter2(context: Context) : BaseAdapter(){
    private var vLayoutInflater: LayoutInflater
    private var yCycleinfoArrayList=ArrayList<Answer>()

    init {
        vLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return yCycleinfoArrayList.size
    }

    override fun getItem(position: Int): Any {
        return yCycleinfoArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView

        if (convertView == null) {
            convertView = vLayoutInflater.inflate(R.layout.list_cycle, parent, false)
        }


        val titleText = convertView!!.findViewById<View>(R.id.titleTextView) as TextView
        titleText.text = yCycleinfoArrayList[position].result

        val resText = convertView.findViewById<View>(R.id.resTextView) as TextView
        resText.text = "費用：" + yCycleinfoArrayList[position].estimate+"円"
        val bytes = yCycleinfoArrayList[position].Anbytes

        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                .copy(Bitmap.Config.ARGB_8888, true)
            val imageView = convertView.findViewById<View>(R.id.imageView2) as ImageView
            imageView.setImageBitmap(image)
        }
        return convertView
    }

    fun setQuestionArrayList(questionArrayList: ArrayList<Answer>) {
        yCycleinfoArrayList= questionArrayList
    }
}