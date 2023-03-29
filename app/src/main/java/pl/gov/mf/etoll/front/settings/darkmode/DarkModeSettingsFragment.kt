package pl.gov.mf.etoll.front.settings.darkmode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.appmode.AppMode
import pl.gov.mf.etoll.databinding.FragmentDarkModeSettingsBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.settings.darkmode.adapteritem.DarkModeSettingsHeader
import pl.gov.mf.etoll.front.settings.darkmode.adapteritem.DarkModeSettingsItem
import pl.gov.mf.etoll.utils.DynamicBindingAdapter

class DarkModeSettingsFragment :
    BaseDatabindingFragment<DarkModeSettingsViewModel, DarkModeSettingsFragmentComponent>() {

    lateinit var binding: FragmentDarkModeSettingsBinding

    private lateinit var adapter: DynamicBindingAdapter
    private lateinit var darkModeSettingsToolbar: MaterialToolbar

    private val consHeaders: HashMap<DarkModeSettingsConstHeaders, DynamicBindingAdapter.Item> =
        hashMapOf(
            DarkModeSettingsConstHeaders.TOP to DarkModeSettingsHeader()
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_dark_mode_settings,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = DynamicBindingAdapter(viewLifecycleOwner)
        binding.darkModeSettingsRecyclerView.adapter = adapter
        darkModeSettingsToolbar = binding.root.findViewById(R.id.dark_mode_settings_toolbar)
        return binding.root
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        adapter.notifyBindingsExpressionsChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewData.observe(viewLifecycleOwner) {
            adapter.submitList(regenerateAdapterItems(it))
        }
        viewModel.navigation.observe(viewLifecycleOwner) {
            if (it == DarkModeSettingsViewModel.DarkModeSettingsNavigation.CHANGE_LAYOUT_BACK) {
                findNavController().popBackStack()
                viewModel.resetNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        darkModeSettingsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun regenerateAdapterItems(viewData: ThemeSettingsViewData): MutableList<DynamicBindingAdapter.Item> {
        val items = mutableListOf<DynamicBindingAdapter.Item>()
        consHeaders[DarkModeSettingsConstHeaders.TOP]?.let {
            items.add(it)
        }
        items.add(DarkModeSettingsItem(
            modeName = "dark_mode_system_option", selected = viewData.selectedTheme == null
        ) {
            viewModel.onModeSelected(dark = false, bySystem = true)
        })
        items.add(DarkModeSettingsItem(
            modeName = "dark_mode_dark_mode_option",
            selected = viewData.selectedTheme == AppMode.DARK_MODE
        ) {
            viewModel.onModeSelected(dark = true, bySystem = false)
        })
        items.add(DarkModeSettingsItem(
            modeName = "dark_mode_light_mode_option",
            selected = viewData.selectedTheme == AppMode.LIGHT_MODE
        ) {
            viewModel.onModeSelected(dark = false, bySystem = false)
        })
        return items
    }

    override fun createComponent(): DarkModeSettingsFragmentComponent =
        activityComponent.plus(
            DarkModeSettingsFragmentModule(this, lifecycle)
        )

    enum class DarkModeSettingsConstHeaders {
        TOP
    }

    data class ThemeSettingsViewData(
        val selectedTheme: AppMode?,
    )
}