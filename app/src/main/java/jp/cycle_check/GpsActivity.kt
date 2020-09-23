package jp.cycle_check

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder

import androidx.appcompat.app.AppCompatActivity

import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.*

import android.content.Intent

import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_gps.*
import kotlin.collections.ArrayList


class GpsActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    var locationService: LocationService? = null
    var alldistance:Float=0f
    var buttom_type=0
    //boolean isZooming;
    //boolean isBlockingAutoZoom;

    private var locationUpdateReceiver: BroadcastReceiver? = null
    private var predictedLocationReceiver: BroadcastReceiver? = null
    private var handlerOnUIThread: Handler? = null
    internal var zoomBlockingTimer: Timer? = null
    var speedlist= ArrayList<Float>()
    var accspeedlist= ArrayList<Float>()
    var timelist= ArrayList<Float>()
    var acctimelist= ArrayList<Float>()
    var acceleratlist=ArrayList<Float>()
    var acclist=ArrayList<Float>()
    var speedmax=0f
    var speedmin=0f
    var actimemax=0f
    var actimemin=0f

    /* Filter */

    internal val handler = Handler()

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val serviceStart = Intent(this.application, LocationService::class.java)
        this.application.startService(serviceStart)
        this.application.bindService(serviceStart, serviceConnection, Context.BIND_AUTO_CREATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
               setup()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_CODE)
                return
            }
        } else {
            setup()
        }
        Gpsstart_button.setOnClickListener() {
            if(buttom_type==0){
                this.locationService?.startLogging()
                Gpsstart_button.text="一時停止"
                buttom_type=1

            }else{
                this.locationService?.stopLogging()
                Gpsstart_button.text="再開"
                buttom_type=0
            }


            }


        Gpskill_button.setOnClickListener(){
            AlertDialog.Builder(this) // FragmentではActivityを取得して生成
                .setTitle("確認")
                .setMessage("終了しますか？")
                .setPositiveButton("OK", { dialog, which ->
                    // TODO:Yesが押された時の挙動
                    this@GpsActivity.locationService?.stopUpdatingLocation()
                    locationService = null
                })
                .setNegativeButton("No", { dialog, which ->
                    // TODO:Noが押された時の挙動
                })
                .show()
        }


        locationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val newLocation = intent.getParcelableExtra<Location>("location")
                val speed = intent.extras.get("speed") as Float//m/s
                val runtime = intent.extras.get("runtime")as Float
                val distance:Float = intent.extras?.getFloat("distance") ?:0f
                var Acc=0f
                val textView6 = findViewById<TextView>(R.id.text_view6)
                speedlist.add(speed)//km/hに変換
                timelist.add(runtime)

                //25km/h 7m/s～　5km/h 1.4m/s間の時間を測定　前の値を速度が下回ったら打ち切り
                if(speed>5&&speed<25){
                    accspeedlist.add(speed)
                    if(accspeedlist.size>=2) {
                        for (i in 0 until accspeedlist.size - 1) {
                            if(accspeedlist[i+1]>accspeedlist[i]){
                                acceleratlist.add(speed)
                                acctimelist.add(runtime)

                            }else{
                               accspeedlist.clear()
                                acctimelist.clear()
                            }
                        }
                    }
                    if(accspeedlist.size>4) {
                        speedmin = (acceleratlist.min())!!.toFloat()
                        speedmax = acceleratlist.max()!!.toFloat()

                        actimemin = acctimelist.min()!!.toFloat()
                        actimemax = acctimelist.max()!!.toFloat()

                        val acc=(speedmax-speedmin)/(actimemax-actimemin)
                        acctimelist.clear()
                        accspeedlist.clear()
                        val str6 = acc.toString()
                        textView6.text = str6+"[m/s^2]"
                        acclist.add(Acc)
                    }
                }




                val textView3 = findViewById<TextView>(R.id.speed)
                var str3=0.0f
                       str3 = speed
                textView3.text = str3.toString()



                val textView4 = findViewById<TextView>(R.id.text_view4)
                val runtime_text=runtime.toInt()
                val str4 = runtime_text.toString()+"[sec]"
                textView4.text = str4

                val textView5 = findViewById<TextView>(R.id.text_view5)
                val distance_text=distance.toInt()
                val str5 = distance_text.toString()+"[m]"
                textView5.text = str5


            }
        }

        locationUpdateReceiver?.let{
            LocalBroadcastManager.getInstance(this).registerReceiver(
                it,
                IntentFilter("LocationUpdated")
            )
        }

      graph()





    }


    fun setup() {
        handlerOnUIThread = Handler()

        val task = object : TimerTask() {
            override fun run() {
                handlerOnUIThread?.post {
                    zoomBlockingTimer = null

                }
            }
        }
            val locationService = Intent(this.application, LocationService::class.java)
            this@GpsActivity.locationService?.startLogging()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.application.startForegroundService(locationService)
        } else {
            this.application.startService(locationService)
        }
        this.application.bindService(locationService, serviceConnection, Context.BIND_AUTO_CREATE)

    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val name = className.className
            if (name.endsWith("LocationService")) {
                locationService = (service as LocationService.LocationServiceBinder).service
                this@GpsActivity.locationService?.startUpdatingLocation()
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            if (className.className == "LocationService") {
                this@GpsActivity.locationService?.stopUpdatingLocation()
                locationService = null
            }
        }
    }


    public override fun onDestroy() {
        try {
            if (locationUpdateReceiver != null) {
                unregisterReceiver(locationUpdateReceiver)
            }

            if (predictedLocationReceiver != null) {
                unregisterReceiver(predictedLocationReceiver)
            }
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
        super.onDestroy()

    }







fun graph(){



}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    setup()
                }
                return
            }
        }
    }





}