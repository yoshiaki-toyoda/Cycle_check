package jp.cycle_check
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import android.R
import android.R.attr.name



class DisAdapter(context: Context): BaseAdapter() {
    private val mLayoutInflater: LayoutInflater
    var taskList = mutableListOf<Task>()
    var totaldis:Int=0

    init {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return taskList.size
    }

    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return taskList[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View = convertView ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null)
        val textView1 = view.findViewById<TextView>(android.R.id.text1)
        val textView2 = view.findViewById<TextView>(android.R.id.text2)
        val cycle_name = taskList[position].cycle_name
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.JAPANESE)
        val simpleDateFormat1 = SimpleDateFormat("HH:mm", Locale.JAPANESE)
        val date = taskList[position].date
        val dis =taskList[position].distance




        textView1.text = "Title:"+taskList[position].title+"  車両名："+cycle_name
        textView2.text ="走行日："+ simpleDateFormat.format(date)+" 走行距離："+dis+"km"+ "  走行時間："+simpleDateFormat1.format(date)
        return view
    }













}