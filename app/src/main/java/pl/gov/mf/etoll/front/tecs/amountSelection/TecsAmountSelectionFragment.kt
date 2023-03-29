package pl.gov.mf.etoll.front.tecs.amountSelection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.commons.CurrencyUtils.format
import pl.gov.mf.etoll.databinding.FragmentTecsAmountSelectionBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.networking.api.model.ApiTecsOpenTransactionResponseError.*
import pl.gov.mf.etoll.ui.components.dialogs.TecsBadAmountDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.TecsErrorDialogFragment
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import javax.inject.Inject

class TecsAmountSelectionFragment :
    BaseDatabindingFragment<TecsAmountSelectionViewModel, TecsAmountSelectionFragmentComponent>(),
    TecsAmountSelectionAdapterCallbacks {

    private lateinit var binding: FragmentTecsAmountSelectionBinding

    @Inject
    lateinit var tecsAmountSelectionAdapter: TecsAmountSelectionAdapter

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        lockBackPress { viewModel.onToolbarCrossClick() }
        viewModel.loadingStatus.observe(this) { loading ->
            if (loading)
                showLoading()
            else
                hideLoading()
        }
        viewModel.navigationTarget.observe(this) { target ->
            when (target) {
                TecsAmountSelectionViewModel.NavigationTarget.NONE -> {
                }
                TecsAmountSelectionViewModel.NavigationTarget.BACK -> {
                    viewModel.resetNavigation()
                    findNavController().popBackStack()
                }
                TecsAmountSelectionViewModel.NavigationTarget.FORWARD -> {
                    viewModel.resetNavigation()
                    findNavController().navigate(
                        TecsAmountSelectionFragmentDirections.actionStartTransaction(
                            viewModel.browserUrl!!,
                            viewModel.selectedAmount.value!!.format(2),
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                is TecsAmountSelectionViewModel.NavigationTarget.ERROR -> {
                    viewModel.resetNavigation()
                    when (target.errorType) {
                        INCORRECT_MAX_TOP_UP_AMOUNT,
                        INCORRECT_MAX_ACCOUNT_BALANCE,
                        ->
                            TecsBadAmountDialogFragment.createAndShow(
                                target.title,
                                target.content,
                                R.drawable.ic_tecs_amount_too_high_light,
                                childFragmentManager
                            )
                        INCORRECT_MIN_TOP_UP_AMOUNT ->
                            TecsBadAmountDialogFragment.createAndShow(
                                target.title,
                                target.content,
                                R.drawable.ic_tecs_amount_too_low_light,
                                childFragmentManager
                            )
                        else ->
                            TecsErrorDialogFragment.showGenericError(
                                target.title, target.content,
                                childFragmentManager
                            )
                    }
                }
                is TecsAmountSelectionViewModel.NavigationTarget.USER_CANCEL_REQUEST -> {
                    viewModel.resetNavigation()
                    findNavController().popBackStack()
                }
                else -> {
                    viewModel.resetNavigation()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_tecs_amount_selection,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        tecsAmountSelectionAdapter.initialize(
            viewModel.accountInfo,
            viewModel.possibleAmountValues
        )
        binding.tecsAmountSelectionList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = tecsAmountSelectionAdapter
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        tecsAmountSelectionAdapter.setCallbacks(this)
    }

    override fun onPause() {
        super.onPause()
        tecsAmountSelectionAdapter.clearCallbacks()
    }

    override fun createComponent(): TecsAmountSelectionFragmentComponent =
        activityComponent.plus(
            TecsAmountSelectionFragmentModule(this, lifecycle, this)
        )

    override fun onCellSelected(amount: String) {
        viewModel.updateSelectedAmount(amount)
    }

    override fun onImeActionDone() {
        viewModel.onContinueClick()
    }

}