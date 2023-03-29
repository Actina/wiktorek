package pl.gov.mf.etoll.front

import android.annotation.SuppressLint
import android.os.Build
import pl.gov.mf.etoll.base.BaseDatabindingViewModel
import pl.gov.mf.etoll.core.androidsettings.AndroidSettingsManagerUC
import pl.gov.mf.etoll.core.devicecompatibility.DeviceCompatibilityUC
import pl.gov.mf.etoll.core.devicecompatibility.types.BatteryOptimisationInfo
import pl.gov.mf.etoll.core.permissions.GpsPermissionsUC
import pl.gov.mf.etoll.ui.components.dialogs.CancellableTwoButtonDialog
import pl.gov.mf.etoll.ui.components.dialogs.GenericOneButtonDialog
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment
import javax.inject.Inject

abstract class BaseDatabindingFragmentWithPermissionsSupport<VIEWMODEL : BaseDatabindingViewModel, COMPONENT> :
    BaseDatabindingFragment<VIEWMODEL, COMPONENT>() {

    @Inject
    lateinit var checkGpsPermissionsUseCase: GpsPermissionsUC.CheckGpsPermissionsUseCase

    @Inject
    lateinit var askForGpsPermissionsUseCase: GpsPermissionsUC.AskForGpsPermissionsUseCase

    @Inject
    lateinit var openAppDetailsSettingsUseCase: AndroidSettingsManagerUC.OpenAppDetailsSettingsUseCase

    @Inject
    lateinit var batteryOptimisationUseCase: DeviceCompatibilityUC.CheckBatteryOptimisationUseCase

    @Inject
    lateinit var getBatteryOptimizationIntentUseCase: DeviceCompatibilityUC.GetManufacturerBatteryOptimizationIntent

    private var askedForGpsPermissions = false
    private var askedForBatteryOptimization = false

    @SuppressLint("CheckResult")
    fun askForGpsPermissions() {
        // check if we even need to ask for those permissions
        if (checkGpsPermissionsUseCase.execute()) {
            onGpsPermissionsGranted()
            return
        }
        askedForGpsPermissions = true
        when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.R -> {
                val (title, body, button) = getGpsPermissionsAndroid12RequestDialogText()
                CancellableTwoButtonDialog.createAndShow(
                    parentFragmentManager,
                    title, body, button
                )?.observe(viewLifecycleOwner) {
                    if (it == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED)
                        openAppDetailsSettingsUseCase.execute()
                    else onGpsPermissionsDenied()
                }
            }
            Build.VERSION.SDK_INT < Build.VERSION_CODES.R -> {
                val (title, body, button) = getGpsPermissionsRequestDialogText()
                CancellableTwoButtonDialog.createAndShow(
                    parentFragmentManager,
                    title, body, button
                )?.observe(viewLifecycleOwner) {
                    if (it == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED)
                        askForGpsPermissionsUseCase.execute(this)
                    else
                        onGpsPermissionsDenied()
                }
            }
            else -> {
                val (title, body, button) = getGpsPermissionsRequestDialogTextForManualAction()
                CancellableTwoButtonDialog.createAndShow(
                    parentFragmentManager,
                    title, body, button
                )?.observe(viewLifecycleOwner) {
                    if (it == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED)
                        openAppDetailsSettingsUseCase.execute()
                    else
                        onGpsPermissionsDenied()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    fun askForBatteryOptimization() {
        // check if we need to ask for those permissions
        if (batteryOptimisationUseCase.execute() != BatteryOptimisationInfo.OPTIMISATION_ENABLED) {
            onBatteryOptimizationDisabled()
            return
        }
        val (title, body, button) = getBatteryOptimizationRequestDialogText()
        CancellableTwoButtonDialog.createAndShow(
            parentFragmentManager,
            title, body, button, "ic_warning"
        )?.observe(viewLifecycleOwner) {
            if (it == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED) {
                askedForBatteryOptimization = true
                startActivity(getBatteryOptimizationIntentUseCase.execute())
            } else
                onBatteryOptimizationDenied()
        }
    }

    /**
     * This function will be called when returning from permission call
     */
    abstract fun onGpsPermissionsGranted()

    /**
     * This function will be called when user cancelled or rejected gps permissions
     */
    open fun onGpsPermissionsDenied() {
    }

    /**
     * This function will be called when returning from battery optimization call
     */
    abstract fun onBatteryOptimizationDisabled()

    /**
     * This function will be called when battery optimization wasn't changed at all
     */
    open fun onBatteryOptimizationDenied() {

    }

    @SuppressLint("CheckResult")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (askedForGpsPermissions) {
            askedForGpsPermissions = false
            if (checkGpsPermissionsUseCase.execute()) {
                onGpsPermissionsGranted()
            } else {
                // we're on android < 11, so let's ask if user wants to do it alone?
                val (title, body, button) = getGpsPermissionsRejectedDialogTextForManualActions()
                CancellableTwoButtonDialog.createAndShow(
                    parentFragmentManager,
                    title,
                    body,
                    button
                )?.observe(viewLifecycleOwner) {
                    if (it == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED) {
                        askedForGpsPermissions = true
                        openAppDetailsSettingsUseCase.execute()
                    } else
                        onGpsPermissionsDenied()
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onStart() {
        super.onStart()
        if (askedForBatteryOptimization) {
            askedForBatteryOptimization = false
            if (batteryOptimisationUseCase.execute() != BatteryOptimisationInfo.OPTIMISATION_ENABLED) {
                onBatteryOptimizationDisabled()
            } else {
                val (title, body, button) = getBatteryOptimizationRejectedDialogText()
                GenericOneButtonDialog.createAndShow(
                    parentFragmentManager,
                    title,
                    body,
                    button
                )?.observe(viewLifecycleOwner) {
                    onBatteryOptimizationDenied()
                }
            }
        } else
            if (askedForGpsPermissions) {
                askedForGpsPermissions = false
                if (checkGpsPermissionsUseCase.execute()) {
                    onGpsPermissionsGranted()
                } else {
                    val (title, body, button) = getGpsPermissionsRejectedDialogText()
                    GenericOneButtonDialog.createAndShow(
                        parentFragmentManager,
                        title,
                        body,
                        button
                    )?.observe(viewLifecycleOwner) {
                        onGpsPermissionsDenied()
                    }
                }
            }
    }

    private fun getBatteryOptimizationRequestDialogText(): Triple<String, String, String> =
        Triple(
            "battery_optimization_request_title_android",
            "battery_optimization_request_body_android",
            "battery_optimization_request_button_android"
        )

    open fun getBatteryOptimizationRejectedDialogText(): Triple<String, String, String> =
        Triple(
            "battery_optimization_rejected_title_android",
            "battery_optimization_rejected_body_android",
            "battery_optimization_rejected_button_android"
        )

    open fun getGpsPermissionsRejectedDialogText(): Triple<String, String, String> = Triple(
        "gps_permission_rejected_title_android",
        "gps_permission_rejected_body_android",
        "gps_permission_rejected_button_android"
    )

    private fun getGpsPermissionsRejectedDialogTextForManualActions(): Triple<String, String, String> =
        Triple(
            "gps_permission_rejected_title_android",
            "gps_permission_rejected_body_system_less_than_11_android",
            "gps_permission_request_button_android"
        )

    open fun getGpsPermissionsAndroid12RequestDialogText(): Triple<String, String, String> = Triple(
        "gps_permission_request_title_android",
        "gps_permission_request_body_a12_android",
        "gps_permission_request_button_android"
    )

    open fun getGpsPermissionsRequestDialogText(): Triple<String, String, String> = Triple(
        "gps_permission_request_title_android",
        "gps_permission_request_body_android",
        "gps_permission_request_button_android"
    )

    open fun getGpsPermissionsRequestDialogTextForManualAction(): Triple<String, String, String> =
        Triple(
            "gps_permission_request_title_android",
            "gps_permission_request_body_system_above_10_android",
            "gps_permission_request_button_android"
        )
}