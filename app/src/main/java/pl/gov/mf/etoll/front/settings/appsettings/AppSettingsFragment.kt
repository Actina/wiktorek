package pl.gov.mf.etoll.front.settings.appsettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentAppSettingsBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.settings.appsettings.SettingsNavigationDestinations.*
import pl.gov.mf.etoll.front.settings.appsettings.adapteritem.*
import pl.gov.mf.etoll.ui.components.dialogs.OverlayPermissionsDialogFragment
import pl.gov.mf.etoll.utils.DynamicBindingAdapter
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment
import pl.gov.mf.mobile.utils.translate

class AppSettingsFragment :
    BaseDatabindingFragment<AppSettingsViewModel, AppSettingsFragmentComponent>() {

    lateinit var binding: FragmentAppSettingsBinding
    private lateinit var adapter: DynamicBindingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun onResume() {
        super.onResume()
        viewModel.onResume(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_app_settings,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = DynamicBindingAdapter(viewLifecycleOwner)
        binding.appSettingsRecyclerView.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedLanguage.observe(viewLifecycleOwner) {
            adapter.submitList(regenerateAdapterItems(it))
        }

        viewModel.requestedDestinations.observe(viewLifecycleOwner) { state ->
            when (state) {
                BACK -> {
                    findNavController().popBackStack()
                }
                LANGUAGE_SETTINGS -> {
                    viewModel.resetRequestedDestination()
                    findNavController().navigate(
                        AppSettingsFragmentDirections.actionShowLanguageSettingsV2(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                SettingsNavigationDestinations.OVERLAY_PERMISSIONS -> {
                    viewModel.resetRequestedDestination()
                    // show dialog with information what will happen
                    OverlayPermissionsDialogFragment.createAndShow(childFragmentManager)
                        ?.observe(viewLifecycleOwner) {
                            if (it == BasicTwoActionsDialogFragment.DialogResult.CONFIRMED) {
                                viewModel.showOverlayPermissionsRequest()
                            }
                        }
                }
                DARK_MODE -> {
                    viewModel.resetRequestedDestination()
                    findNavController().navigate(
                        AppSettingsFragmentDirections.actionShowDarkModeSettingsFragment(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                else -> {

                }
            }
        }

    }

    private fun regenerateAdapterItems(selectedLanguage: String): MutableList<DynamicBindingAdapter.Item> {
        val items = mutableListOf<DynamicBindingAdapter.Item>()
        items.add(LanguageItem(selectedLanguage, viewModel::onChangeLanguageClicked))
        items.add(OverlayItem(viewModel.overlayEnabled, viewModel::onOverlayModeSwitchRequested))

        items.add(DarkModeItem(header = "dark_mode_title".translate(requireContext())
                + " / " + "app_settings_dark_mode_header".translate(requireContext()),
            selectedMode = viewModel.appModeConfigValue,
            clicked = viewModel::onDarkModeSwitchRequested))
        if (viewModel.shouldShowSentSelector())
            items.add(SentModeItem(viewModel.sentMode, viewModel::onSentModeSwitched))
        items.add(
            NotificationsItem(
                viewModel.soundChecked,
                viewModel::onSoundClicked,
                viewModel.vibrationChecked,
                viewModel::onVibrationClicked
            )
        )
        return items
    }

    override fun createComponent(): AppSettingsFragmentComponent =
        activityComponent.plus(
            AppSettingsFragmentModule(this, lifecycle)
        )
}