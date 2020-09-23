package jp.cycle_check


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_distance_grap.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.github.mikephil.charting.data.CombinedData

import com.github.mikephil.charting.components.YAxis



class DistanceGrapActivity : AppCompatActivity() {


    private lateinit var mDatabaseReference: DatabaseReference
    private var vCycleRef: DatabaseReference? = null
    var timelist=ArrayList<String>()
    var dislist=ArrayList<Int>()
    var cycle_uid:String=""
    var datelist=ArrayList<String>()
    var avespeed=ArrayList<String>()
    var total_ave:Int=0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance_grap)

        val chart = bar_chart
        chart.getDescription().setEnabled(false)
        chart.setBackgroundColor(Color.WHITE)
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setHighlightFullBarEnabled(false)
        chart.legend.isEnabled = true

        val l = chart.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        val rightAxis = chart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        rightAxis.textSize=16f

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        leftAxis.textSize=16f

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
                            val df = SimpleDateFormat("yyyy/MM/dd")
                            val dt=df.parse(n.toString()!!)
                            val df2=SimpleDateFormat("MM/dd")
                            val date=df2.format(dt)
                            datelist.add(date.toString())
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
                    avespeed.add(("%,.1f".format(speed)).toString())
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
        val data = CombinedData()
        //表示データ取得
         data.setData(getLineData())
         data.setData(getBarData())

        chart.xAxis.setAxisMaximum(data.getXMax() + 0.5f)
        chart.xAxis.setAxisMinimum(data.getXMin() - 0.5f)
        chart.xAxis.textSize=13f
        chart.setExtraOffsets(0f,0f,20f,12f)
        chart.axisRight.textSize=13f
        chart.axisLeft.textSize=13f
        chart.setData(data)
        chart.setVisibleXRangeMaximum(6f)

        //X軸の設定
        val labels = datelist //最初の””は原点の値
        val datesize=datelist.size.toFloat()
        chart.xAxis.apply {

            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            textSize=16f
            setDrawGridLines(false)
            setDrawAxisLine(true)
            setAxisMinimum(-0.5f)
            setGranularity(1f)
        }
        chart.axisLeft.apply {
            textSize=16f
        }

        chart.axisRight.apply {
            textSize=16f
        }
        //グラフ上の表示
        chart.apply {
            setDrawValueAboveBar(true)
            description.isEnabled = false
            isClickable = false
            setScaleEnabled(false)
            animateY(1200, Easing.EasingOption.Linear)
        }



        chart.invalidate()
    }
    private fun getBarData(): BarData {
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

        val dataSet1 = BarDataSet(entries1, "走行距離[Km]").apply {
            //整数で表示
            valueFormatter = IValueFormatter { value, _, _, _ -> "" + value.toInt() }
            //ハイライトさせない
            isHighlightEnabled = true
            setStackLabels(arrayOf("Ave Speed"))
            setColors(Color.rgb(61, 165, 255))
            setValueTextColor(Color.rgb(61, 165, 255))
            setValueTextSize(13f)
            setAxisDependency(YAxis.AxisDependency.LEFT)
        }

        var bars=BarData(dataSet1)
        return bars
    }

    private fun getLineData(): LineData {
        val entries2 = ArrayList<Entry>().apply {
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
        val dataSet2 = LineDataSet(entries2, "Ave Speed[Km/h]").apply {
            valueFormatter = IValueFormatter { value, _, _, _ -> "" + value.toFloat() }
            //ハイライトさせない
            isHighlightEnabled = true
            setColor(Color.rgb(240, 238, 70));
            setLineWidth(2.5f);
            setCircleColor(Color.rgb(240, 238, 70));
            setCircleRadius(5f);
            setFillColor(Color.rgb(240, 238, 70));
            setMode(LineDataSet.Mode.CUBIC_BEZIER);
            setDrawValues(true);
            setValueTextSize(13f);
            setValueTextColor(Color.rgb(240, 238, 70));
            setAxisDependency(YAxis.AxisDependency.RIGHT)
        }

        var bars2=LineData(dataSet2)
        return bars2

    }

}
