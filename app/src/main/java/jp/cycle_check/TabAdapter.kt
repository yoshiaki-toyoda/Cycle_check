package jp.cycle_check

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import jp.cycle_check.ui.Fragment.Tab01Fragment
import jp.cycle_check.ui.Fragment.Tab02Fragment
import kotlinx.android.synthetic.main.activity_answer.*
import java.io.ByteArrayOutputStream

class TabAdapter(fm: FragmentManager, private val xCycle: Answer_question2,private val type:Int): FragmentPagerAdapter(fm) {
    private val PERMISSIONS_REQUEST_CODE = 100
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {

                return Tab01Fragment(xCycle)
            }
            else -> {
                return Tab02Fragment(xCycle,type)
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> {
                return "質問"
            }
            else -> {
                return "回答"
            }
        }
    }



    override fun getCount(): Int {
        return 2
    }



}