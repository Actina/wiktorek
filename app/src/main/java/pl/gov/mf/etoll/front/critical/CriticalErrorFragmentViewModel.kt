package pl.gov.mf.etoll.front.critical

// FYI: this is left to be fixed after rush - there is small error somewhere in DI option

//class CriticalErrorFragmentViewModel : BaseViewModel() {
//    var errorType: Int = 0
//
//    private val _titleText = MutableLiveData<String>()
//    val titleText: LiveData<String>
//        get() = _titleText
//
//    private val _headerText = MutableLiveData<String>()
//    val headerText: LiveData<String>
//        get() = _headerText
//
//    private val _contentText = MutableLiveData<String>()
//    val contentText: LiveData<String>
//        get() = _contentText
//
//    private val _askForPermissionsAction = MutableLiveData<Boolean>(false)
//    val askForPermissionsAction: LiveData<Boolean>
//        get() = _askForPermissionsAction
//
//    override fun onResume() {
//        super.onResume()
//        _titleText.postValue(
//            when (errorType) {
//                CriticalErrorFragment.TYPE_SECURITY_ISSUE -> "critical_error_default_title"
//                CriticalErrorFragment.TYPE_DEVICE_INCOMPATIBLE -> "critical_error_default_title"
//                CriticalErrorFragment.TYPE_STARTUP_ISSUE -> "dialog_criticalerror_title_code"
//                CriticalErrorFragment.TYPE_APP_OUTDATED -> "critical_error_deprecated_title"
//                CriticalErrorFragment.TYPE_NO_GPS_PERMISSION -> "critical_error_no_gps_permission_title"
//                CriticalErrorFragment.TYPE_GPS_DISABLED -> "critical_error_no_gps_permission_title"
//                else -> ""
//            }
//        )
//        _headerText.postValue(
//            when (errorType) {
//                CriticalErrorFragment.TYPE_SECURITY_ISSUE -> "dialog_criticalerror_title_security"
//                CriticalErrorFragment.TYPE_DEVICE_INCOMPATIBLE -> "critical_error_compatibility_header_android"
//                CriticalErrorFragment.TYPE_STARTUP_ISSUE -> "dialog_criticalerror_title_code"
//                CriticalErrorFragment.TYPE_APP_OUTDATED -> "critical_error_deprecated_header"
//                CriticalErrorFragment.TYPE_NO_GPS_PERMISSION -> "critical_error_no_gps_permission_header"
//                CriticalErrorFragment.TYPE_GPS_DISABLED -> "critical_error_no_gps_permission_header"
//                else -> ""
//            }
//        )
//        _contentText.postValue(
//            when (errorType) {
//                CriticalErrorFragment.TYPE_SECURITY_ISSUE -> "dialog_criticalerror_text_security"
//                CriticalErrorFragment.TYPE_DEVICE_INCOMPATIBLE -> "critical_error_banned_content"
//                CriticalErrorFragment.TYPE_STARTUP_ISSUE -> "dialog_criticalerror_text_code"
//                CriticalErrorFragment.TYPE_APP_OUTDATED -> "critical_error_deprecated_content"
//                CriticalErrorFragment.TYPE_NO_GPS_PERMISSION -> "critical_error_no_gps_permission_content"
//                CriticalErrorFragment.TYPE_GPS_DISABLED -> "critical_error_no_gps_permission_content"
//                else -> ""
//            }
//        )
//
//        if (errorType == CriticalErrorFragment.TYPE_NO_GPS_PERMISSION) {
//            // ask for permissions
//            _askForPermissionsAction.postValue(true)
//            // and reset to enable it again with next onResume
//            _askForPermissionsAction.postValue(false)
//        } else if (errorType == CriticalErrorFragment.TYPE_GPS_DISABLED) {
//            // TODO: check gps enabled/disabled state & airplane mode state
//        }
//    }
//
//    override fun shouldCheckSecuritySanityOnThisView(): Boolean = false
//}