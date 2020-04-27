package jp.cycle_check

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.formatter.PercentFormatter
import java.util.Arrays.asList
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_distance_grap.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DistanceGrapActivity : AppCompatActivity() {


    private lateinit var mDatabaseReference: DatabaseReference
    private var vCycleRef: DatabaseReference? = null
    var timelist=ArrayList<String>()
    var dislist=ArrayList<Int>()
    var cycle_uid:String=""
    var datelist=ArrayList<String>()
    var avespeed=ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance_grap)

        //グラフ用データ取得
        lisner()
}


    fun lisner(){
        var extras = intent.extras
        cycle_uid = extras.get("cycle_uid") .toString()
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
                        if(m=="time"){
                            timelist.add(n.toString())
                        }else if(m=="date"){
                            datelist.add(n.toString())
                        }else if(m=="distance"){
                            dislist.add(n.toString().toInt())
                        }
                    }
                    count = count + 1
                    sinfo.clear()
                }

                var size=dislist.size-1
                for(i in 0..size){
                    var hour=timelist[i].substring(0,2)
                    var min=timelist[i].substring(3,5)
                    var ridetime= ((hour.toString().toInt()*60+min.toString().toInt())/60).toInt().toFloat()
                    var speed:Float =dislist[i]/ridetime
                    avespeed.add(("%,.2f".format(speed)).toString())
                }



                chart()
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

    }

    fun chart() {
        val chart = bar_chart
        //表示データ取得
        chart.data = BarData(getBarData() as List<IBarDataSet>?)

        //Y軸(左)の設定
        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 150f
            labelCount = 5
            setDrawTopYLabelEntry(true)
            setValueFormatter { value, axis -> "" + value.toInt()}
        }

        //Y軸(右)の設定
        chart.axisRight.apply {
            setDrawLabels(false)
            setDrawGridLines(false)
            setDrawZeroLine(false)
            setDrawTopYLabelEntry(true)
        }

        //X軸の設定
        val labels = datelist //最初の””は原点の値
        val datesize=datelist.size
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            labelCount = datesize //表示させるラベル数
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            setDrawGridLines(false)
            setDrawAxisLine(true)
        }

        //グラフ上の表示
        chart.apply {
            setDrawValueAboveBar(true)
            description.isEnabled = false
            isClickable = false
            legend.isEnabled = false //凡例
            setScaleEnabled(false)
            animateY(1200, Easing.EasingOption.Linear)
        }
    }
    private fun getBarData(): ArrayList<IBarDataSet> {
        //表示させるデータ
        val entries1 = ArrayList<BarEntry>().apply {
            var x :Float=0f
            var dis:Float=0f
            var i:Float=0f
            var size=dislist.size-1

            for (i in 0..size){
                x=i.toFloat()
                dis=dislist[i].toFloat()
                add(BarEntry(x,dis))
            }

        }

        val entries2 = ArrayList<BarEntry>().apply {
            var x :Float=0f
            var ave:Float=0f
            var i:Float=0f
            var size=dislist.size-1

            for (i in 0..size){
                x=i.toFloat()
                ave=avespeed[i].toFloat()
                add(BarEntry(x,ave))
            }

        }

        val dataSet1 = BarDataSet(entries1, "bar").apply {
            //整数で表示
            valueFormatter = IValueFormatter { value, _, _, _ -> "" + value.toInt() }
            //ハイライトさせない
            isHighlightEnabled = false
            //Barの色をセット
            setColors(intArrayOf(R.color.material_blue, R.color.material_green, R.color.material_yellow), this@DistanceGrapActivity)
        }

        val dataSet2 = BarDataSet(entries2, "bar").apply {
            //整数で表示
            valueFormatter = IValueFormatter { value, _, _, _ -> "" + value.toInt() }
            //ハイライトさせない
            isHighlightEnabled = false
            //Barの色をセット
            setColors(intArrayOf(R.color.material_blue, R.color.material_green, R.color.material_yellow), this@DistanceGrapActivity)
        }



        val bars = ArrayList<IBarDataSet>()
        bars.add(dataSet1)
        bars.add(dataSet2)
        return bars
    }


}
