package com.hustlers.fintrack

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.hustlers.fintrack.activities.AddTransactionActivity
import com.hustlers.fintrack.activities.LockActivity
import com.hustlers.fintrack.activities.SettingsActivity
import com.hustlers.fintrack.databinding.ActivityMainBinding
import com.hustlers.fintrack.dataclass.NavItem
import com.hustlers.fintrack.fragments.GoalsFragment
import com.hustlers.fintrack.fragments.HomeFragment
import com.hustlers.fintrack.fragments.InsightsFragment
import com.hustlers.fintrack.fragments.TransactionsFragment
import com.hustlers.fintrack.utils.BiometricLockManager

/**
 * MainActivity hosts the primary navigation for the FinTrack app:
 * - Home (summary)
 * - Transactions
 * - Insights
 * - Goals
 *
 * This activity also coordinates biometric locking and the add-transaction flow.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navItems: List<NavItem>
    private var selectedIndex = 0

    companion object {
        private var isUnlocked = false
        // Intent extra key for when the activity is started after unlocking
        private const val EXTRA_FROM_UNLOCK = "from_unlock"
        // Delay before checking biometric lock again when returning to the activity (ms)
        private const val LOCK_DELAY_MS = 100L
        // Semi-transparent white used for inactive icons/labels
        private val SEMI_TRANSPARENT_WHITE = Color.parseColor("#99FFFFFF")
        // Animation durations (ms)
        private const val NAV_SCALE_DURATION = 250L
        private const val NAV_ALPHA_DURATION = 200L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fromUnlock = intent.getBooleanExtra(EXTRA_FROM_UNLOCK, false)

        if (fromUnlock) {
            isUnlocked = true
        }

        val biometricManager = BiometricLockManager(this)

        if (biometricManager.isBiometricEnabled() && !isUnlocked && !fromUnlock) {
            startActivity(Intent(this, LockActivity::class.java))
            finish()
            return
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavBar()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), 0)
        }
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            val biometricManager = BiometricLockManager(this)
            if (biometricManager.isBiometricEnabled() && !isUnlocked) {
                isUnlocked = false
                startActivity(Intent(this, LockActivity::class.java))
                finish()
            }
        }, LOCK_DELAY_MS)
    }

    override fun onPause() {
        super.onPause()
        isUnlocked = false
    }

    private fun setupNavBar() {
        navItems = listOf(
            NavItem(binding.navHome, binding.iconHome, binding.labelHome),
            NavItem(binding.navTransactions, binding.iconTransactions, binding.labelTransactions),
            NavItem(binding.navInsights, binding.iconInsights, binding.labelInsights),
            NavItem(binding.navGoals, binding.iconGoals, binding.labelGoals)
        )

        navItems.forEachIndexed { index, item ->
            item.container.setOnClickListener {
                selectTab(index)
                loadFragmentForIndex(index)
            }
        }

        binding.navAdd.setOnClickListener {
            animateAddButton()
            handleAddAction()
        }

        selectTab(0)
    }

    private fun loadFragmentForIndex(index: Int) {
        val fragment = when (index) {
            0 -> HomeFragment()
            1 -> TransactionsFragment()
            2 -> InsightsFragment()
            3 -> GoalsFragment()
            else -> HomeFragment()
        }
        loadFragment(fragment, index)
    }

    private fun loadFragment(fragment: Fragment, index: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .commit()
    }

    private fun handleAddAction() {
        startActivity(Intent(this, AddTransactionActivity::class.java))
    }

    fun selectTab(index: Int) {
        navItems.forEachIndexed { i, item ->
            val isSelected = i == index
            animateNavItem(item, isSelected)
        }
        selectedIndex = index
    }

    private fun animateNavItem(item: NavItem, selected: Boolean) {
        val targetAlpha = if (selected) 1f else 0.5f
        val targetBg = if (selected) {
            ContextCompat.getDrawable(this, R.drawable.nav_item_selected_bg)
        } else null

        val scaleX = ObjectAnimator.ofFloat(item.container, "scaleX", if (selected) 0.85f else 1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(item.container, "scaleY", if (selected) 0.85f else 1f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = NAV_SCALE_DURATION
            interpolator = OvershootInterpolator(1.5f)
            start()
        }

        item.icon.animate().alpha(targetAlpha).setDuration(NAV_ALPHA_DURATION).start()
        item.label.animate().alpha(targetAlpha).setDuration(NAV_ALPHA_DURATION).start()

        val tintColor = if (selected) Color.WHITE else SEMI_TRANSPARENT_WHITE
        item.icon.imageTintList = ColorStateList.valueOf(tintColor)
        item.label.setTextColor(tintColor)
        item.label.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)

        item.container.background = targetBg
    }

    private fun animateAddButton() {
        binding.navAdd.animate()
            .scaleX(0.85f).scaleY(0.85f)
            .setDuration(100)
            .withEndAction {
                binding.navAdd.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(OvershootInterpolator(2f))
                    .start()
            }.start()
    }
}
