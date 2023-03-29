package pl.gov.mf.etoll.front.errors.timeissues

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_DATE_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentTimeIssuesBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.errors.timeissues.TimeIssuesViewModel.NavigationTargets.*

class TimeIssuesFragment : BaseDatabindingFragment<TimeIssuesViewModel, TimeIssuesFragmentComponent>() {

    override fun createComponent(): TimeIssuesFragmentComponent = activityComponent.plus(
        TimeIssuesFragmentModule(this, lifecycle)
    )

    private lateinit var binding: FragmentTimeIssuesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        lockBackPress { viewModel.onFinishClicked() }
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_time_issues,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigationTarget.observe(viewLifecycleOwner) { target ->
            when (target) {
                NONE -> {}
                SHOW_SETTINGS -> {
                    viewModel.resetNavigationTarget()
                    startActivity(Intent(ACTION_DATE_SETTINGS))
                }
                FINISH -> {
                    viewModel.resetNavigationTarget()
                    findNavController().popBackStack()
                }
                else -> {
                    viewModel.resetNavigationTarget()
                }
            }
        }
    }
}