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
import kotlin.collections.ArrayList


class GpsActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val PERMISSIONS_REQUEST_CODE = 100
    var locationService: LocationService? = null
    var alldistance:Float=0f
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

        locationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val newLocation = intent.getParcelableExtra<Location>("location")
                val speed = intent.extras.get("speed") as Float//m/s
                val runtime = intent.extras.get("runtime")as Float
                val distance:Float = intent.extras?.getFloat("distance") ?:0f
                var Acc=0f
                val textView6 = findViewById<TextView>(R.id.text_view6)
                speedlist.add(speed*3.6f)//km/hに変換
                timelist.add(runtime)

                //25km/h 7m/s～　5km/h 1.4m/s間の時間を測定　前の値を速度が下回ったら打ち切り
                if(speed>1.4&&speed<10){
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
                    if(accspeedlist.size>3) {
                        speedmin = (acceleratlist.min())!!.toFloat()
                        speedmax = acceleratlist.max()!!.toFloat()

                        actimemin = acctimelist.min()!!.toFloat()
                        actimemax = acctimelist.max()!!.toFloat()

                        val acc=(speedmax-speedmin)/(actimemax-actimemin)


                        val str6 = acc.toString()
                        textView6.text = str6

                    }
                }




                if(acceleratlist.size>=2) {
                    for (i in 0 until acceleratlist.size - 1) {
                         Acc = (speedlist[i+1]-speedlist[i])/(timelist[i+1]-timelist[i])

                    }
                }else{
                     Acc=0f
                }
                acceleratlist.add(Acc)

                val text_view1=findViewById<TextView>(R.id.text_view1)
                val str1 = "Latitude:" + newLocation.getLatitude()
                text_view1.text = str1

                val textView2 = findViewById<TextView>(R.id.text_view2)
                val str2 = "Longtude:" + newLocation.getLongitude()
                textView2.text = str2

                val textView3 = findViewById<TextView>(R.id.text_view3)
                val str3 = speed.toString()
                textView3.text = str3

                val textView4 = findViewById<TextView>(R.id.text_view4)
                val str4 = runtime.toString()
                textView4.text = str4

                val textView5 = findViewById<TextView>(R.id.text_view5)
                val str5 = distance.toString()
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