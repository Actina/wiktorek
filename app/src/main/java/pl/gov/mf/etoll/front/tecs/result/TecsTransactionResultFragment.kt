package pl.gov.mf.etoll.front.tecs.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentTecsTransactionResultBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment

class TecsTransactionResultFragment :
    BaseDatabindingFragment<TecsTransactionResultViewModel, TecsTransactionResultFragmentComponent>() {

    private lateinit var binding: FragmentTecsTransactionResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        lockBackPress { viewModel.onToolbarCrossClick() }
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun createComponent(): TecsTransactionResultFragmentComponent = activityComponent.plus(
        TecsTransactionResultFragmentModule(this, lifecycle)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_tecs_transaction_result,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.interpretInput(requireArguments(), requireContext())
        viewModel.navigationTarget.observe(viewLifecycleOwner) { target ->
            when (target) {
                TecsTransactionResultViewModel.NavigationTarget.NONE -> {
                }
                TecsTransactionResultViewModel.NavigationTarget.DASHBOARD -> {
                    viewModel.resetNavigation()
                    // navigate to dashboard
                    findNavController().popBackStack(R.id.dashboardFragment, false)
                }
                TecsTransactionResultViewModel.NavigationTarget.AMOUNT_SELECTION -> {
                    viewModel.resetNavigation()
                    findNavController().popBackStack(R.id.tecsAmountSelectionFragment, false)
                }
                else -> {
                    viewModel.resetNavigation()
                }
            }
        }
    }
}