package pl.gov.mf.etoll.front.critical

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.base.BaseFragment
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.front.MainActivityComponent
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationState
import pl.gov.mf.etoll.interfaces.CommonInterfacesUC
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.utils.popBackStack
import pl.gov.mf.mobile.utils.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CriticalErrorFragment : BaseFragment<MainActivityComponent>() {

    companion object {
        const val TYPE_SECURITY_ISSUE = 0
        const val TYPE_DEVICE_INCOMPATIBLE = 1
        const val TYPE_STARTUP_ISSUE = 2
        const val TYPE_APP_OUTDATED = 3

        //        const val TYPE_NO_GPS_PERMISSION = 4
        const val TYPE_GPS_DISABLED = 5
        const val TYPE_AIRPLANE_MODE = 6
    }

    private val args: CriticalErrorFragmentArgs by navArgs()
    private var compositeDisposable: CompositeDisposable? = null

//    @Inject
//    lateinit var askForGpsPermissionsUseCase: GpsPermissionsUC.AskForGpsPermissionsUseCase

    @Inject
    lateinit var checkGpsStateUseCase: DeviceCompatibilityUC.CheckGpsStateUseCase

    @Inject
    lateinit var addToHistoryUseCase: CommonInterfacesUC.AddNotificationToHistoryUseCase

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityComponent.inject(this)
        activityComponent.changeBottomNavigationStateUseCase().execute(BottomNavigationState(false))
        activityComponent.setStatusBarColorUseCase().execute(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable = CompositeDisposable()
        activityComponent.setStatusBarColorUseCase().execute(requireActivity())
        if (args.errorType == TYPE_GPS_DISABLED) {
            compositeDisposable.addSafe(Observable.interval(1, 3, TimeUnit.SECONDS).subscribe({
                // check gps enabled and airplane mode
                if (!AirplaneModeUtils.isAirplaneModeOn(requireContext()) && checkGpsStateUseCase.executeAlternativeCode()) {
                    // view is not needed anymore
                    findNavController().popBackStack(lifecycleScope, navigationJob)
                }
            }, {}))
        } else if (args.errorType == TYPE_AIRPLANE_MODE) {
            compositeDisposable.addSafe(Observable.interval(1, 3, TimeUnit.SECONDS).subscribe({
                // check airplane mode
                if (!AirplaneModeUtils.isAirplaneModeOn(requireContext())) {
                    // view is not needed anymore
                    findNavController().popBackStack(lifecycleScope, navigationJob)
                }
            }, {}))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            findNavController().popBackStack(lifecycleScope, navigationJob)
    }

    override fun onPause() {
        super.onPause()
        compositeDisposable.disposeSafe()
        compositeDisposable = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                closeApp()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_criticalerror, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val title: String = when (args.errorType) {
            TYPE_SECURITY_ISSUE -> "critical_error_default_title"
            TYPE_DEVICE_INCOMPATIBLE -> "critical_error_default_title"
            TYPE_STARTUP_ISSUE -> "critical_error_startup_issue_check_new_app_header_android" //TODO: We don't have correct tile
            TYPE_APP_OUTDATED -> "critical_error_deprecated_title"
//            TYPE_NO_GPS_PERMISSION -> "critical_error_no_gps_permission_title"
            TYPE_GPS_DISABLED -> "critical_error_gps_disabled_title_android"
            TYPE_AIRPLANE_MODE -> "critical_error_airplane_mode_title_android"
            else -> ""
        }
        val header: String = when (args.errorType) {
            TYPE_SECURITY_ISSUE -> "critical_error_banned_header"
            TYPE_DEVICE_INCOMPATIBLE -> "critical_error_compatibility_header_android"
            TYPE_STARTUP_ISSUE -> "critical_error_startup_issue_check_new_app_header_android"  //TODO: We don't have correct header
            TYPE_APP_OUTDATED -> "critical_error_deprecated_header"
//            TYPE_NO_GPS_PERMISSION -> "critical_error_no_gps_permission_header"
            TYPE_GPS_DISABLED -> "critical_error_gps_disabled_header_android"
            TYPE_AIRPLANE_MODE -> "critical_error_airplane_mode_header_android"
            else -> ""
        }
        val text: String = when (args.errorType) {
            TYPE_SECURITY_ISSUE -> "critical_error_security_content_android"
            TYPE_DEVICE_INCOMPATIBLE -> "critical_error_banned_content"
            TYPE_STARTUP_ISSUE -> "critical_error_startup_issue_check_new_app_content_android"
            TYPE_APP_OUTDATED -> "critical_error_deprecated_content"
//            TYPE_NO_GPS_PERMISSION -> "critical_error_no_gps_permission_content"
            TYPE_GPS_DISABLED -> "critical_error_gps_disabled_content_android"
            TYPE_AIRPLANE_MODE -> "critical_error_airplane_mode_content_android"
            else -> ""
        }

        val criticalErrorTitle = view.findViewById<TextView>(R.id.criticalErrorFragment_title)
        criticalErrorTitle.text = title.translate(requireContext())
        criticalErrorTitle.setTextColor(
            "criticalErrorTextColor".toColorInMode(
                requireContext()
            )
        )

        val criticalErrorHeader = view.findViewById<TextView>(R.id.criticalErrorFragment_header)
        criticalErrorHeader.text = header.translate(requireContext())
        criticalErrorHeader.setTextColor("criticalErrorTextColor".toColorInMode(requireContext()))

        val criticalErrorText = view.findViewById<TextView>(R.id.criticalErrorFragment_text)
        criticalErrorText.text = text.translate(requireContext())
        criticalErrorText.setTextColor(
            "criticalErrorTextColor".toColorInMode(
                requireContext()
            )
        )

        view.findViewById<ConstraintLayout>(R.id.criticalErrorFragment_background)
            .setBackgroundColor(
                "criticalErrorBackground".toColorInMode(
                    requireContext()
                )
            )

//        if (args.errorType == TYPE_NO_GPS_PERMISSION) {
        // ask for permissions
//            askForGpsPermissionsUseCase.execute(this)
//        }
        compositeDisposable.addSafe(
            addToHistoryUseCase.execute(
                NotificationHistoryController.Type.BAD,
                title,
                text
            ).subscribe()
        )
    }
}