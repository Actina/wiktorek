package pl.gov.mf.etoll.ui.components.dialogs.crm

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.FragmentManager
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import pl.gov.mf.etoll.app.BaseApplication
import pl.gov.mf.etoll.interfaces.NotificationHistoryController
import pl.gov.mf.etoll.translations.Translatable
import pl.gov.mf.etoll.translations.TranslationKeys
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.compose.combined.button.EtollButtonWithDefaultPadding
import pl.gov.mf.etoll.ui.components.compose.combined.buttonisle.EtollButtonIsle
import pl.gov.mf.etoll.ui.components.compose.combined.dialogs.EtollDialogDefault
import pl.gov.mf.etoll.ui.components.compose.combined.dialogs.BlurableWithSoundComposeDialogFragment
import pl.gov.mf.etoll.ui.components.compose.helpers.getIconInMode
import pl.gov.mf.etoll.ui.components.compose.theme.Dimens
import pl.gov.mf.etoll.ui.components.compose.theme.EtollTheme
import pl.gov.mf.etoll.ui.components.compose.theme.h2bold
import pl.gov.mf.etoll.ui.components.compose.theme.p1Normal
import pl.gov.mf.mobile.utils.JsonConvertible
import pl.gov.mf.mobile.utils.toObject
import java.util.*

class CrmDialogFragment : BlurableWithSoundComposeDialogFragment() {

    val dialogResult: BehaviorSubject<CrmDialogViewModel.DialogResult> = BehaviorSubject.create()

    private lateinit var viewModel: CrmDialogViewModel

    lateinit var crmMessageModel: CrmMessageModel

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_MODEL, crmMessageModel.toJSON())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            if (it.containsKey(KEY_MODEL))
                crmMessageModel = it.getString(KEY_MODEL)!!.toObject()
        }
        viewModel = CrmDialogViewModel(
            (requireContext().applicationContext as BaseApplication).getApplicationComponent()
                .useCaseGetCurrentLanguage()
        ).apply { refreshUiState(crmMessageModel) }
        super.onCreate(savedInstanceState)

        if (!crmMessageModel.alreadyAdded) {
            with((requireContext().applicationContext as BaseApplication).getApplicationComponent()) {
                useCaseCheckIfShouldAddNotificationToHistory().execute(crmMessageModel.apiMessageId)
                    .subscribe { messageShouldBeAdded ->
                        if (messageShouldBeAdded) {
                            useCaseAddNotificationToHistory().execute(
                                NotificationHistoryController.Type.CRM,
                                titleResource = "",
                                contentResource = "",
                                iconResource = crmMessageModel.type.iconResLight,
                                payloadJson = crmMessageModel.toJSON(),
                                apiMessageId = crmMessageModel.apiMessageId
                            ).subscribe()
                        }
                    }
            }
            crmMessageModel.alreadyAdded = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            EtollThemeWithModeAndTranslations {
                EtollDialogDefault(
                    header = {
                        Image(
                            painter = painterResource(
                                id = getIconInMode(
                                    icLightRes = viewModel.messageType.value!!.iconResLight,
                                    icDarkRes = viewModel.messageType.value!!.iconResDark,
                                )
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            contentDescription = ""
                        )
                    },
                    title = {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.XL_SPACING)
                                .padding(top = Dimens.XL_SPACING),
                            text = viewModel.headerText.value!!,
                            textAlign = TextAlign.Center,
                            style = EtollTheme.typography.h2bold,
                            color = when (viewModel.messageType.value) {
                                MessageType.INFO -> EtollTheme.colors.dialogInfoHeader
                                MessageType.WARNING -> EtollTheme.colors.dialogWarningHeader
                                MessageType.CRITICAL -> EtollTheme.colors.dialogCriticalHeader
                                else -> EtollTheme.colors.dialogInfoHeader
                            }
                        )
                    },
                    content = {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.XL_SPACING)
                                .padding(top = Dimens.M_SPACING),
                            text = viewModel.contentText.value!!,
                            textAlign = TextAlign.Center,
                            style = EtollTheme.typography.p1Normal,
                            color = EtollTheme.colors.textPrimary
                        )
                    },
                    buttonsIsle = {
                        EtollButtonIsle(button = {
                            EtollButtonWithDefaultPadding(text = composeTranslations[TranslationKeys.MISC_OK_BUTTON]) {
                                dismiss()
                            }
                        })
                    },
                    onDismiss = {
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crmMessageModel.let { viewModel.refreshUiState(it) }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialogResult.onNext(CrmDialogViewModel.DialogResult.CONFIRMED)
    }

    override fun getViewModel(): CrmDialogViewModel = viewModel

    data class CrmMessageModel(
        @SerializedName("contents") val contents: Translatable?,
        @SerializedName("headers") val headers: Translatable?,
        @SerializedName("dateTimestamp") val dateTimestamp: Long,
        @SerializedName("type") val type: MessageType,
        @SerializedName("alreadyAdded") var alreadyAdded: Boolean = false,
        @SerializedName("messageId") val apiMessageId: String = "",
    ) : JsonConvertible


    enum class MessageType(val apiName: String, val iconResLight: Int, val iconResDark: Int) {
        @Keep
        INFO(
            apiName = "INFO",
            iconResLight = R.drawable.ic_crm_info_light,
            iconResDark = R.drawable.ic_crm_info_dark
        ),

        @Keep
        WARNING(
            apiName = "WARNING",
            iconResLight = R.drawable.ic_crm_warning_orange_light,
            iconResDark = R.drawable.ic_crm_warning_orange_dark
        ),

        @Keep
        CRITICAL(
            apiName = "CRITICAL",
            iconResLight = R.drawable.ic_crm_warning_red_light,
            iconResDark = R.drawable.ic_crm_warning_red_dark
        );

        companion object {
            fun from(name: String): MessageType {
                for (it in values())
                    if (it.apiName.uppercase(Locale.getDefault())
                            .contentEquals(name.uppercase(Locale.getDefault()))
                    )
                        return it
                return INFO
            }
        }
    }

    companion object {
        const val TAG = "crmDialog"
        private const val KEY_MODEL: String = "MODEL"

        fun create(
            crmMessageModel: CrmMessageModel,
            fm: FragmentManager,
        ): Single<CrmDialogViewModel.DialogResult>? {
            if (fm.findFragmentByTag(TAG) == null) {
                val dialog = CrmDialogFragment()
                dialog.crmMessageModel = crmMessageModel
                dialog.show(fm, TAG)
                return dialog.dialogResult.firstOrError()
            }
            return null
        }
    }
}