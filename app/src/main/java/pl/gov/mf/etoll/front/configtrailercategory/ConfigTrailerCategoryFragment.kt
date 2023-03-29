package pl.gov.mf.etoll.front.configtrailercategory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentConfigTrailerWeightCategoryBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment

class ConfigTrailerCategoryFragment :
    BaseDatabindingFragment<ConfigTrailerCategoryFragmentViewModel, ConfigTrailerCategoryFragmentComponent>() {

    private lateinit var binding: FragmentConfigTrailerWeightCategoryBinding

    override fun createComponent(): ConfigTrailerCategoryFragmentComponent =
        activityComponent.plus(
            ConfigTrailerCategoryFragmentModule(this, lifecycle)
        )

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_config_trailer_weight_category,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel.init(findNavController().previousBackStackEntry!!.destination.id)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.config_trailer_weight_category_title)
            .setOnClickListener {
                findNavController().popBackStack()
            }

        viewModel.navigate.observe(viewLifecycleOwner) { navigation ->
            if (navigation != ConfigTrailerCategoryFragmentViewModel.TrailerCategoryNavigation.IDLE)
                viewModel.resetNavigation()
            when (navigation) {
                ConfigTrailerCategoryFragmentViewModel.TrailerCategoryNavigation.IDLE -> {
                }
                ConfigTrailerCategoryFragmentViewModel.TrailerCategoryNavigation.MONITORING_DEVICE -> {
                    findNavController().navigate(
                        ConfigTrailerCategoryFragmentDirections.actionShowMonitoringDevice(
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                ConfigTrailerCategoryFragmentViewModel.TrailerCategoryNavigation.DASHBOARD -> {
                    findNavController().popBackStack(R.id.dashboardFragment, false)

                }
                ConfigTrailerCategoryFragmentViewModel.TrailerCategoryNavigation.FINISH -> {
                    findNavController().popBackStack(R.id.dashboardFragment, false)
                }
                else -> {
                }
            }
        }
    }


}
