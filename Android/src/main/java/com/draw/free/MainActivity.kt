package com.draw.free

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.draw.free.ar.ARDrawActivity
import com.draw.free.databinding.ActivityMainBinding
import timber.log.Timber
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.draw.free.ar.ARDrawTutorialActivity
import com.draw.free.dialog.CameraPermission
import com.draw.free.dialog.ConfirmDialog
import com.draw.free.fragment.BaseContainerFragment
import com.draw.free.fragment.FragmentType
import com.draw.free.interfaceaction.ToNextWork
import com.draw.free.network.RetrofitClient
import com.draw.free.util.BackStack
import com.draw.free.viewmodel.ConfirmDialogModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.RuntimeException

class MainActivity : AppCompatActivity() {
    interface ShowBottomNav {
        fun show(isShowed: Boolean, lightColor: Boolean = true)
    }


    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNav: BottomNavigationView
    private val backStack = BackStack(false)
    private var bottomNavIsLight = false
    private var bottomNavIsShow = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (backStack.getSize() == 0) {
            backStack.pushStack(0)
        }


        // bottomNavigation 설정
        bottomNav = binding.bottomNavigation
        bottomNav.setOnItemSelectedListener {
            changeFragment(
                when (it.itemId) {
                    R.id.btn_home -> FragmentType.Home
                    R.id.btn_nft -> FragmentType.NFT
                    R.id.btn_search -> FragmentType.Search
                    R.id.btn_profile -> FragmentType.Profile
                    else -> {
                        FragmentType.Home
                    }
                }
            )
            true
        }

        val activity = this

        // AR 그리기 탭 onClickListener 연결
        binding.btnDraw.setOnClickListener {
            if (!CameraPermission.hasCameraPermission()) {
                val dialogModelForCloseActivity = ConfirmDialogModel("AR 드로잉을 위해서는 카메라 권한이 필요합니다")
                dialogModelForCloseActivity.clickYes = object : ToNextWork {
                    override fun next() {
                        if (CameraPermission.shouldShowRequestPermissionRationale(activity)) {
                            CameraPermission.requestCameraPermission(activity);
                        } else {
                            CameraPermission.launchPermissionSettings(activity);
                        }
                    }
                }

                val dialogForClose = ConfirmDialog(this, dialogModelForCloseActivity);
                dialogForClose.show()
            } else {

                val intent = Intent(baseContext, ARDrawActivity::class.java)
                intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                Global.getContext().startActivity(intent)

                val prefs = Global.prefs.getPrefs()
                if (prefs!!.getBoolean("ar_draw_tutorial", true)) {
                    prefs.edit().putBoolean("ar_draw_tutorial", false).apply()
                    val intent = Intent(baseContext, ARDrawTutorialActivity::class.java)
                    intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    Global.getContext().startActivity(intent)
                }
            }
        }

        bottomNav.selectedItemId = R.id.btn_home
    }

    // BottomNav 작동과 BackStack 으로 화면 전환 간의 차이를 만들기 위해 있는 변수

    private var changeForBackStack = false

    override fun onBackPressed() {
        Timber.e("뒤로 가기")
        if (lastFragment != null && lastFragment!!.childFragmentManager.backStackEntryCount > 0) {
            lastFragment!!.childFragmentManager.popBackStackImmediate()
            return
        }

        changeForBackStack = true

        if (backStack.getSize() != 0) {
            when (backStack.peek()) {
                0 -> {
                    bottomNav.selectedItemId = R.id.btn_home
                }
                1 -> {
                    bottomNav.selectedItemId = R.id.btn_home
                }
                2 -> {
                    bottomNav.selectedItemId = R.id.btn_nft
                }
                3 -> {
                    bottomNav.selectedItemId = R.id.btn_search
                }
                4 -> {
                    bottomNav.selectedItemId = R.id.btn_profile
                }
                else -> {
                    Timber.d("잘못된 스택")
                }
            }

            backStack.pop()
        } else {
            super.onBackPressed()
        }
    }

    val showBottomNav = object : ShowBottomNav {
        override fun show(isShowed: Boolean, lightColor: Boolean) {
            if (lightColor) {
                binding.bottomNavigation.setBackgroundColor(
                    ContextCompat.getColor(
                        baseContext,
                        R.color.white
                    )
                )
            } else {
                binding.bottomNavigation.setBackgroundColor(
                    ContextCompat.getColor(
                        baseContext,
                        R.color.black
                    )
                )
            }

            bottomNavIsShow = isShowed
            bottomNavIsLight = lightColor


            if (isShowed) {
                binding.bottomNavigation.visibility = View.VISIBLE
                binding.btnDraw.visibility = View.VISIBLE
            } else {
                binding.bottomNavigation.visibility = View.GONE
                binding.btnDraw.visibility = View.GONE
            }
        }
    }

    private val containerFragments: Array<BaseContainerFragment> by lazy {
        arrayOf(
            BaseContainerFragment.createHomeFragment(),
            BaseContainerFragment.createNftFragment(),
            BaseContainerFragment.createSearchFragment(),
            BaseContainerFragment.createProfileFragment()
        )
    }

    private var lastFragment: BaseContainerFragment? = null

    private fun changeFragment(type: FragmentType) {

        val addFragment: Fragment by lazy {
            when (type) {
                FragmentType.Home -> {
                    binding.bottomNavigation.setBackgroundColor(
                        ContextCompat.getColor(
                            baseContext,
                            R.color.white
                        )
                    )
                    return@lazy containerFragments[0]
                }
                FragmentType.NFT -> {
                    binding.bottomNavigation.setBackgroundColor(
                        ContextCompat.getColor(
                            baseContext,
                            R.color.white
                        )
                    )
                    return@lazy containerFragments[1]
                }
                FragmentType.Search -> {
                    binding.bottomNavigation.setBackgroundColor(
                        ContextCompat.getColor(
                            baseContext,
                            R.color.white
                        )
                    )
                    return@lazy containerFragments[2]
                }
                FragmentType.Profile -> {
                    return@lazy containerFragments[3]
                }
            }
        }

        lastFragment = (addFragment as BaseContainerFragment)

        val transaction = supportFragmentManager.beginTransaction();

        if (!addFragment.isAdded) {
            transaction.add(R.id.fragmentContainerView, addFragment);
        } else {
            transaction.show(addFragment)
        }

        supportFragmentManager.fragments.forEach {
            if (it != addFragment && it.isAdded) {
                transaction.hide(it)
            }
        }

        transaction.commit()
        changeForBackStack = false
    }

    override fun onStart() {
        super.onStart()
        showBottomNav.show(bottomNavIsShow, bottomNavIsLight)
    }
}
