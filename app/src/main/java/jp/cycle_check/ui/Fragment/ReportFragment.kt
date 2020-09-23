package jp.cycle_check.ui.Fragment

import android.content.Context

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import jp.cycle_check.Cycleinfo
import jp.cycle_check.R
import kotlinx.android.synthetic.main.fragment_report.*
import kotlinx.android.synthetic.main.fragment_report.toolbar

class ReportFragment : Fragment() {
    var mCycle: Cycleinfo? = null
    private var mListener: ReportFragment.FragmentListener? = null

    interface FragmentListener {
        fun shop_question()
        fun question_all()
        fun answer_shop()
        fun answer_all()
        fun change_shop()
        fun shop_info()
    }

    fun addFragment(){
        mListener?.shop_question()
    }

    fun addFragment1(){
        mListener?.question_all()
    }

    fun addFragment2(){
        mListener?.answer_shop()
    }

    fun addFragment3(){
        mListener?.answer_all()
    }

    fun addFragment4(){
        mListener?.change_shop()
    }

    fun addFragment5(){
        mListener?.shop_info()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ReportFragment.FragmentListener) {
            mListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // mCycle = arguments!!.getSerializable("CycleInfo") as Cycleinfo
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setTitle("相談")
        shop_question.setOnClickListener{
            addFragment()
        }

        all_question.setOnClickListener{
            addFragment1()
        }

        action_Answer.setOnClickListener{
            addFragment2()
        }

        all_answer.setOnClickListener{
            addFragment3()
        }
        action_settings.setOnClickListener{
            addFragment4()
        }

        shop_info.setOnClickListener(){
            addFragment5()
        }
    }


}