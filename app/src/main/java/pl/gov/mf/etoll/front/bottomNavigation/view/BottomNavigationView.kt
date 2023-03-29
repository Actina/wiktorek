package pl.gov.mf.etoll.front.bottomNavigation.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.ViewBottommenuBinding
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationState
import pl.gov.mf.mobile.utils.disposeSafe
import pl.gov.mf.mobile.utils.toColorListInMode
import pl.gov.mf.mobile.utils.translate

class BottomNavigationView : ConstraintLayout, LifecycleObserver {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var drawerVisible: Boolean = false
    private lateinit var bottomMenuViewObserver: BottomNavigationViewObserver
    private val actionSubject = MutableLiveData<BottomNavigationAction>()
    private val stateSubject = BehaviorSubject.create<BottomNavigationState>()
    private var compositeDisposable: CompositeDisposable? = null

    private var lastSelectionBeforeMenuOpen = 1

    private val binding: ViewBottommenuBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_bottommenu, this, true)

    init {
        binding.run {
            bottomMenu.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.mainmenu_activities -> {
                        postNewAction(BottomNavigationAction.ACTIVITY)
                    }
                    R.id.mainmenu_ride -> {
                        postNewAction(BottomNavigationAction.RIDE)
                    }
                    R.id.mainmenu_more -> {
                        openDrawer()
                    }
                    else -> {

                    }
                }
                return@setOnNavigationItemSelectedListener false
            }
            mainMenuHelp.setOnClickListener {
                postNewAction(BottomNavigationAction.HELP)
            }
            mainMenuSettings.setOnClickListener {
                postNewAction(BottomNavigationAction.SETTINGS)
            }
            mainMenuSecurity.setOnClickListener {
                postNewAction(BottomNavigationAction.SECURITY_SETTINGS)
            }
            mainMenuAbout.setOnClickListener {
                postNewAction(BottomNavigationAction.ABOUT)
            }
            mainMenuHistory.setOnClickListener {
                postNewAction(BottomNavigationAction.RIDES_HISTORY)
            }
            loadingArea.setOnClickListener {
                closeDrawer()
            }
        }
        translate()
    }

    fun invalidateBottomMenuInMode() {
        // TODO: for later refinment
        binding.run {
            bottomMenu.apply {
                val bottomNavSelector = "bottom_nav_selector".toColorListInMode(context)
                itemIconTintList = bottomNavSelector
                itemTextColor = bottomNavSelector
            }
        }
        binding.invalidateAll()
    }

    private fun postNewAction(action: BottomNavigationAction) {
        actionSubject.value = action
        actionSubject.postValue(BottomNavigationAction.NONE)
        closeDrawer()
    }

    private fun openDrawer() {
        binding.run {
            if (drawerVisible && bottomMenuArea.visibility == View.VISIBLE) return
            drawerVisible = true
            bottomMenu.setSelected(2)
            bottomMenuArea.visibility = View.VISIBLE
            loadingArea.visibility = View.VISIBLE
        }
    }

    fun initialize(bottomMenuStubView: BottomNavigationViewObserver, blurRoot: ViewGroup) {
        this.bottomMenuViewObserver = bottomMenuStubView
    }

    fun onNewState(state: BottomNavigationState) {
        stateSubject.onNext(state)
        showState(state)
    }

    fun invalidateView() {
        stateSubject.value?.let { showState(it) }
    }

    fun observeNavigationActions(): LiveData<BottomNavigationAction> = actionSubject

    private fun showState(state: BottomNavigationState) {
        binding.run {
            if (state.shouldBeVisible) {
                bottomMenu.visibility = View.VISIBLE
                if (visibility != View.VISIBLE) {
                    visibility = View.VISIBLE
                }
                translate()
                closeDrawer()
                bottomMenuViewObserver.onMenuIsVisible()
            } else {
                bottomMenu.visibility = View.GONE
                if (visibility != View.GONE) {
                    visibility = View.GONE
                }
                bottomMenuViewObserver.onMenuIsHidden()
                closeDrawer()
            }
            bottomMenu.setSelected(state.selectedState.ordinal)
        }
        lastSelectionBeforeMenuOpen = state.selectedState.ordinal
    }

    fun closeDrawer() {
        binding.run {
            if (!drawerVisible && bottomMenuArea.visibility != View.VISIBLE) return
            drawerVisible = false
            compositeDisposable.disposeSafe()
            compositeDisposable = CompositeDisposable()
            bottomMenuArea.visibility = View.GONE
            loadingArea.visibility = View.GONE
            bottomMenu.setSelected(lastSelectionBeforeMenuOpen)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun translate() {
        binding.run {
            bottomMenu.translate()
            mainMenuHelpCaption.text = "more_menu_help".translate(context)
            mainMenuAboutCaption.text = "more_menu_about_app".translate(context)
            mainMenuSettingsCaption.text = "more_menu_settings".translate(context)
            mainMenuSecurityCaption.text = "more_menu_security".translate(context)
            mainMenuHistoryCaption.text = "more_menu_ride_history".translate(context)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume() {
        compositeDisposable = CompositeDisposable()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        closeDrawer()
        compositeDisposable.disposeSafe()
        compositeDisposable = null
    }

    fun isMenuOpened(): Boolean = drawerVisible

}

enum class BottomNavigationAction {
    NONE,
    ACTIVITY,
    RIDE,
    HELP,
    ABOUT,
    SETTINGS,
    SECURITY_SETTINGS,
    RIDES_HISTORY
}

interface BottomNavigationViewObserver {
    fun onMenuIsVisible()
    fun onMenuIsHidden()
}