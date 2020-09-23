package jp.cycle_check.ui.Fragment

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.*
import jp.cycle_check.Cycleinfo
import jp.cycle_check.R
import jp.cycle_check.RidePath
import jp.cycle_check.Task
import kotlinx.android.synthetic.main.activity_distance_grap.*
import kotlinx.android.synthetic.main.activity_distance_grap.bar_chart
import kotlinx.android.synthetic.main.app_header_main.*
import kotlinx.android.synthetic.main.fragment_analise.*
import kotlinx.android.synthetic.main.fragment_analise.toolbar
import java.text.SimpleDateFormat
import java.util.HashMap

class AnaliseFragment:Fragment(){
    private lateinit var mDatabaseReference: DatabaseReference
    private var vCycleRef: DatabaseReference? = null
    private var mListener: AnaliseFragment.FragmentListener? = null
    var mCycle:Cycleinfo?=null
    var timelist=ArrayList<String>()
    var dislist=ArrayList<Int>()
    var cycle_uid:String=""
    var datelist=ArrayList<String>()
    var avespeed=ArrayList<String>()
    var total_ave:Int=0
    var ridetime=0f
    var ridetime2=0f
    var ridetime3=""

    interface FragmentListener {

    }

    override fun onAttach(context: Context){
        super.onAttach(context)
        if (context is FragmentListener){
            mListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCycle = arguments!!.getSerializable("CycleInfo") as Cycleinfo
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return  inflater.inflate(R.layout.fragment_analise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle("走行実績")
        val chart = bar_chart
        chart.getDescription().setEnabled(false)
        chart.setBackgroundColor(Color.WHITE)
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setHighlightFullBarEnabled(false)
        chart.legend.isEnabled = true
        chart.legend.textSize=14f

        val l = chart.legend
        l.isWordWrapEnabled = true
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        val rightAxis = chart.axisRight
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        rightAxis.textSize=14f

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        leftAxis.textSize=14f

        //グラフ用データ取得
        lisner()


    }

    fun lisner(){
        cycle_uid = mCycle!!.cycleUid
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        vCycleRef = mDatabaseReference.child(RidePath).child(cycle_uid)
        vCycleRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value !== null) {
                    var map = snapshot.value as HashMap<String, HashMap<Any, Any>>
                    var sinfo = java.util.HashMap<Any, Any>()
                    var count: Int = 0

                    for ((k, v: HashMap<Any, Any>) in map.toSortedMap()) {
                        for ((j, l) in v) {
                            sinfo[j] = l
                        }
                        for ((m, n) in sinfo) {
                            if (m == "time") {
                                timelist.add(n.toString())
                            } else if (m == "date") {
                                val df = SimpleDateFormat("yyyy/MM/dd")
                                val dt = df.parse(n.toString()!!)
                                val df2 = SimpleDateFormat("MM/dd")
                                val date = df2.format(dt)
                                datelist.add(date.toString())
                            } else if (m == "distance") {
                                dislist.add(n.toString().toInt())
                            }
                        }
                        count = count + 1
                        sinfo.clear()
                    }

                    var size = dislist.size - 1
                    for (i in 0..size) {
                        var hour = timelist[i].substring(0, 2)
                        var min = timelist[i].substring(3, 5)
                        ridetime = ((hour.toString().toInt() * 60 + min.toString().toInt()) / 60).toInt().toFloat()
                        var speed: Float = dislist[i] / ridetime
                        avespeed.add(("%,.1f".format(speed)).toString())
                        ridetime2=(ridetime+ridetime2).toFloat()

                    }
                    chart()
                    val ave=dislist.sum()/ridetime2
                    val ave2f=("%,.1f".format(ave))
                    total_timeText2.text=ridetime2.toString()+"H"
                    total_runText2.text=dislist.sum().toString()+"Km"
                    total_AvespeedText2.text=ave2f.toString()+"Km/h"

                }
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
        chart.xAxis.textSize=15f
        chart.setExtraOffsets(0f,0f,20f,12f)
        chart.axisRight.textSize=15f
        chart.axisLeft.textSize=15f
        chart.setData(data)
        chart.setVisibleXRangeMaximum(6f)

        //X軸の設定
        val labels = datelist //最初の””は原点の値

        val datesize=datelist.size.toFloat()
        chart.xAxis.apply {

            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(true)
            textSize=14f
            setDrawGridLines(false)
            setDrawAxisLine(true)
            setAxisMinimum(-0.5f)
            setGranularity(1f)
        }
        chart.axisLeft.apply {
            textSize=14f
        }

        chart.axisRight.apply {
            textSize=14f
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
            setValueTextSize(15f)
            setAxisDependency(YAxis.AxisDependency.LEFT)
        }

        var bars= BarData(dataSet1)
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
            isHighlightEnabled = true
            setColor(Color.rgb(240, 238, 70));
            setLineWidth(2.5f);
            setCircleColor(Color.rgb(240, 238, 70));
            setCircleRadius(5f);
            setFillColor(Color.rgb(240, 238, 70));
            setMode(LineDataSet.Mode.CUBIC_BEZIER);
            setDrawValues(true);
            setValueTextSize(15f);
            setValueTextColor(Color.rgb(240, 238, 70));
            setAxisDependency(YAxis.AxisDependency.RIGHT)
        }

        var bars2= LineData(dataSet2)
        return bars2

    }
}
