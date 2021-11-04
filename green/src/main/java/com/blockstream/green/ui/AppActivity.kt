package com.blockstream.green.ui

import android.graphics.drawable.Drawable
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.blockstream.green.R
import com.blockstream.green.settings.SettingsManager
import com.blockstream.green.utils.isDebug
import com.blockstream.green.utils.isDevelopmentFlavor
import com.blockstream.green.utils.notifyDevelopmentFeature
import javax.inject.Inject

abstract class AppActivity : AppCompatActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    abstract fun isDrawerOpen(): Boolean
    abstract fun closeDrawer()
    abstract fun lockDrawer(isLocked: Boolean)
    abstract fun setToolbar(
        title: String?,
        subtitle: String? = null,
        drawable: Drawable? = null,
        button: CharSequence? = null,
        buttonListener: View.OnClickListener? = null
    )

    abstract fun setToolbarVisibility(isVisible: Boolean)

    internal lateinit var navController: NavController

    private var isWindowSecure: Boolean = false

    private val secureFragments = listOf(R.id.recoveryIntroFragment, R.id.recoveryCheckFragment, R.id.recoveryWordsFragment, R.id.recoveryPhraseFragment)

    internal fun setupSecureScreenListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // If enhancedPrivacy is turned off, secure only specific screens
            if(!settingsManager.getApplicationSettings().enhancedPrivacy) {
                if(secureFragments.contains(destination.id)) {
                    setSecureScreen(true)
                } else {
                    setSecureScreen(false)
                }
            }
        }

        settingsManager.getApplicationSettingsLiveData().observe(this){
            // Skip changing secure screen if we are on a secure fragment
            if(it.enhancedPrivacy || !secureFragments.contains(navController.currentDestination?.id)){
                setSecureScreen(it.enhancedPrivacy)
            }
        }
    }

    private fun setSecureScreen(isSecure: Boolean) {
        if (isSecure == isWindowSecure) return

        isWindowSecure = isSecure

        // In development flavor allow screen capturing
        if (isDevelopmentFlavor()) {
            notifyDevelopmentFeature("FLAG_SECURE = $isSecure")
            return
        }

        if (isWindowSecure) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}