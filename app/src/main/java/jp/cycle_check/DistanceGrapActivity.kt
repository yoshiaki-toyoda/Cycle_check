package jp.cycle_check

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class DistanceGrapActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distance_grap)
    }
    var extras = intent.extras
    var cycle = extras.get("cyclename") .toString()
    var cycle_uid = extras.get("cycle_uid") .toString()
    var mapsize=extras.get("mapsize")

    var disrecord=mutableListOf<Int>()
    var daterecord=mutableListOf<String>()
    var timerecord=mutableListOf<String>()

    //for文カウント用




}
