package pl.gov.mf.etoll.front.about

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.BuildConfig
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentAboutBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.about.adapteritem.AppUpdateItem
import pl.gov.mf.etoll.front.about.adapteritem.RegulationsAndConsentsItem
import pl.gov.mf.etoll.front.about.adapteritem.ShareBusinessIdItem
import pl.gov.mf.etoll.front.about.adapteritem.SystemInfoItem
import pl.gov.mf.etoll.utils.DynamicBindingAdapter
import pl.gov.mf.mobile.utils.GooglePlayStoreUtils
import pl.gov.mf.mobile.utils.PredefiniedIntents

class AboutFragment : BaseDatabindingFragment<AboutViewModel, AboutFragmentComponent>() {

    lateinit var binding: FragmentAboutBinding
    private lateinit var adapter: DynamicBindingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        adapter.notifyBindingsExpressionsChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_about,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        adapter = DynamicBindingAdapter(viewLifecycleOwner)
        binding.aboutRecyclerView.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.requestedNavigation.observe(viewLifecycleOwner) { goBack ->
            if (goBack) {
                findNavController().popBackStack()
            }
        }
        adapter.submitList(regenerateAdapterItems())
    }

    private fun regenerateAdapterItems(): MutableList<DynamicBindingAdapter.Item> {
        val items = mutableListOf<DynamicBindingAdapter.Item>()
        items.add(AppUpdateItem(BuildConfig.VERSION_NAME) {
            GooglePlayStoreUtils.showStoreForCurrentApp(requireActivity())
        })
        items.add(ShareBusinessIdItem(viewModel.businessNumber) {
            startActivity(Intent.createChooser(PredefiniedIntents.shareIntent(it), null))
        })
        items.add(RegulationsAndConsentsItem {
            findNavController().navigate(
                AboutFragmentDirections.actionRegistrationRegulations(
                    signComponentUseCase.executeCustomSign()
                )
            )
        })
        items.add(SystemInfoItem(viewModel.systemInfoData))
        return items
    }

    override fun createComponent(): AboutFragmentComponent =
        activityComponent.plus(
            AboutFragmentModule(this, lifecycle)
        )
}