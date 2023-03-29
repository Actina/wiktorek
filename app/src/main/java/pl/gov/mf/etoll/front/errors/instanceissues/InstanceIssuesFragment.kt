package pl.gov.mf.etoll.front.errors.instanceissues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentInstanceissuesBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.mobile.utils.GooglePlayStoreUtils

class InstanceIssuesFragment :
    BaseDatabindingFragment<InstanceIssuesViewModel, InstanceIssuesFragmentComponent>() {

    lateinit var binding: FragmentInstanceissuesBinding

    override fun createComponent(): InstanceIssuesFragmentComponent =
        activityComponent.plus(InstanceIssuesFragmentModule(this, lifecycle))

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        viewModel.setDismissableParam(requireArguments().getBoolean("dismissable"))

        requireActivity().onBackPressedDispatcher.addCallback(this, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onOkClick()
            }
        })

        viewModel.shouldClose.observe(this) { close ->
            if (close) {
                findNavController().popBackStack()
            }
        }

        viewModel.shouldShowShop.observe(this) { show ->
            if (show) {
                viewModel.onShopShowed()
                GooglePlayStoreUtils.showStoreForCurrentApp(requireActivity())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_instanceissues,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}