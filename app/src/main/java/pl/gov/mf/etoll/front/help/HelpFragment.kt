package pl.gov.mf.etoll.front.help

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentHelpBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.mobile.utils.PredefiniedIntents
import pl.gov.mf.mobile.utils.translate
import javax.inject.Inject

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class HelpFragment :
    BaseDatabindingFragment<HelpViewModel, HelpFragmentComponent>(), HelpAdapterCallbacks {

    @Inject
    lateinit var adapter: HelpFragmentViewPagerAdapter

    lateinit var binding: FragmentHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        adapter.notifyBindingsChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_help,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        adapter.callbacks = null
    }

    override fun onResume() {
        super.onResume()
        adapter.callbacks = this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.shouldFinish.observe(viewLifecycleOwner) { shouldFinish ->
            if (shouldFinish) {
                findNavController().popBackStack()
            }
        }

        viewModel.helpConfiguration.observe(viewLifecycleOwner) { configuration ->
            adapter.initializePages(configuration)
            val tabLayout = binding.helpTabLayout
            binding.helpPager.adapter = adapter
            TabLayoutMediator(tabLayout, binding.helpPager) { tab, position ->
                val stringToTranslate = if (position == 0) "help_tolled" else "help_sent"
                tab.text = stringToTranslate.translate(requireContext())
            }.attach()
        }
    }

    override fun createComponent(): HelpFragmentComponent =
        activityComponent.plus(
            HelpFragmentModule(this, lifecycle)
        )

    override fun onCallPhoneRequested(number: String) {
        startActivity(
            Intent.createChooser(
                PredefiniedIntents.callPhoneIntent(number),
                null
            )
        )
    }

    override fun onShowTutorialRequested(sent: Boolean) {
        findNavController().navigate(
            HelpFragmentDirections.actionShowTutorial(
                signComponentUseCase.executeCustomSign(),
                sent
            )
        )
    }

    override fun onSendEmailRequested(email: String) {
        startActivity(
            Intent.createChooser(
                PredefiniedIntents.helpEmailIntent(email),
                null
            )
        )
    }

    override fun onShowWebsiteRequested(website: String) {
        startActivity(
            Intent.createChooser(
                PredefiniedIntents.openWebsiteIntent(website),
                null
            )
        )
    }
}
