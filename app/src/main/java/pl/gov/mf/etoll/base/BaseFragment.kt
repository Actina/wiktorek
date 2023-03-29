package pl.gov.mf.etoll.base

import android.content.Intent
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import pl.gov.mf.mobile.ui.components.utils.hideKeyboard


abstract class BaseFragment<ACTIVITY_COMPONENT : BaseComponent<*>> :
    Fragment() {

    @Suppress("UNCHECKED_CAST")
    protected val activityComponent: ACTIVITY_COMPONENT
        get() = (requireActivity() as BaseActivity<*, ACTIVITY_COMPONENT>).component

    protected val navigationJob: Job = SupervisorJob()

    override fun onResume() {
        super.onResume()
        Log.d("FRAGMENTS_LOG", "SHOWING VIEW ${this::class.java.simpleName}")
    }

    override fun onPause() {
        super.onPause()
        navigationJob.cancelChildren()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().currentFocus?.hideKeyboard()
    }

    fun closeApp() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }
}