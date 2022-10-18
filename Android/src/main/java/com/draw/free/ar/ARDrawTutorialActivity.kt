package com.draw.free.ar


import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.draw.free.R
import com.flask.colorpicker.ColorPickerView
import timber.log.Timber


class ARDrawTutorialActivity : AppCompatActivity() {

    private var viewPager: ViewPager? = null
    private var pagerAdapter: PagerAdapter? = null
    private var dotsLayout: LinearLayout? = null
    private lateinit var description: TextView

    private lateinit var dots: Array<TextView?>
    private lateinit var layouts: IntArray
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button

    private val viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)

            // 다음 / 시작 버튼 바꾸기
            if (position == layouts.size - 1) {
                // 마지막 페이지에서는 다음 버튼을 시작버튼으로 교체
                btnNext.text = getString(R.string.finish) // 다음 버튼을 시작버튼으로 글자 교체
                btnSkip.visibility = View.GONE
            } else {

                // 마지막 페이지가 아니라면 다음과 건너띄기 버튼 출력
                btnNext.text = getString(R.string.next)
                btnSkip.visibility = View.VISIBLE
            }
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_draw_tutorial)

        viewPager = findViewById(R.id.view_pager)
        dotsLayout = findViewById(R.id.layoutDots)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)
        description = findViewById(R.id.txt_description)

        // 변화될 레이아웃들 주소
        // 원하는 경우 레이아웃을 몇 개 더 추가
        layouts = intArrayOf(
            R.layout.activity_ar_draw_tutorial_0,
            R.layout.activity_ar_draw_tutorial_1,
            R.layout.activity_ar_draw_tutorial_2,
            R.layout.activity_ar_draw_tutorial_3,
            R.layout.activity_ar_draw_tutorial_4,
            R.layout.activity_ar_draw_tutorial_5,
            R.layout.activity_ar_draw_tutorial_6,
            R.layout.activity_ar_draw_tutorial_7,
            R.layout.activity_ar_draw_tutorial_8,
        )



        // 하단 점 추가
        addBottomDots(0)

        pagerAdapter = PagerAdapter()
        viewPager?.adapter = pagerAdapter
        viewPager?.addOnPageChangeListener(viewPagerPageChangeListener)

        // 건너띄기 버튼 클릭시 메인화면으로 이동
        btnSkip.setOnClickListener { finish() }

        // 조건문을 통해 버튼 하나로 두개의 상황을 실행

        // 조건문을 통해 버튼 하나로 두개의 상황을 실행
        btnNext.setOnClickListener {
            val current: Int = getItem(+1)
            if (current < layouts.size) {
                // 마지막 페이지가 아니라면 다음 페이지로 이동
                viewPager?.currentItem = current
            } else {
                finish()
            }
        }

    }

    private fun getItem(i: Int): Int {
        return viewPager!!.currentItem + i
    }

    fun onclick(v: View, itemType : String) {
        Timber.e("클릭함, ${v.id}")
    }

    // 하단 점(선택된 점, 선택되지 않은 점) 구현
    private fun addBottomDots(currentPage: Int) {
        dots = arrayOfNulls(layouts.size) // 레이아웃 크기만큼 하단 점 배열에 추가
        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)
        val descriptionStr = resources.getStringArray(R.array.array_description_tutorial_draw)


        dotsLayout!!.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226;", Html.FROM_HTML_MODE_LEGACY); //Html.fromHtml("&#8226;")
            dots[i]?.textSize = 35f
            dots[i]?.setTextColor(colorsInactive[0])
            dotsLayout!!.addView(dots[i])
        }
        if (dots.isNotEmpty()) dots[currentPage]?.setTextColor(colorsActive[0])

        description.text = descriptionStr[currentPage]
    }

    inner class PagerAdapter : androidx.viewpager.widget.PagerAdapter() {
        private var layoutInflater: LayoutInflater? = null
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val view: View = layoutInflater!!.inflate(layouts.get(position), container, false)

            // 시크바 막기
            if (position == 7) {
                view.findViewById<SeekBar>(R.id.seekBar).isEnabled = false
            } else if (position == 8) { // colorPicker 막기
                view.findViewById<ColorPickerView>(R.id.color_picker_view).setOnClickListener { }

            }



            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return layouts.size
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val view = `object` as View
            container.removeView(view)
        }
    }
}