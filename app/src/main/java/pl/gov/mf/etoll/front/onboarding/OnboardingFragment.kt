package pl.gov.mf.etoll.front.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentStartupOnboardingBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.onboarding.viewpager.OnboardingViewPagerAdapter
import pl.gov.mf.mobile.utils.toColorInMode
import javax.inject.Inject

class OnboardingFragment :
    BaseDatabindingFragment<OnboardingViewModel, OnboardingFragmentComponent>() {

    @Inject
    lateinit var viewPagerAdapter: OnboardingViewPagerAdapter

    private lateinit var binding: FragmentStartupOnboardingBinding
    private lateinit var startupOnoardingContinueButton: MaterialButton
    private lateinit var startupOnboardingNextButton: MaterialButton
    private lateinit var startupOnboardingSkipButton: MaterialTextView
    private lateinit var startupOnboardingDots: WormDotsIndicator
    private lateinit var startupOnboardingViewpager: ViewPager2
    private lateinit var toolBar: MaterialToolbar

    private val onPageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            if (position == viewPagerAdapter.itemCount - 1) {
                startupOnoardingContinueButton.visibility = View.VISIBLE
                startupOnboardingNextButton.visibility = View.INVISIBLE
            } else {
                startupOnoardingContinueButton.visibility = View.INVISIBLE
                startupOnboardingNextButton.visibility = View.VISIBLE
            }
        }
    }

    override fun createComponent(): OnboardingFragmentComponent =
        activityComponent.plus(OnboardingFragmentModule(this, lifecycle))

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        startupOnboardingDots.setDotIndicatorColor("wormDotIndicator".toColorInMode(requireContext()))
        startupOnboardingDots.setStrokeDotsIndicatorColor(
            "wormDotIndicatorBlueOpacity".toColorInMode(
                requireContext()
            )
        )
        viewPagerAdapter.notifyBindingsChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)

        requireActivity().onBackPressedDispatcher.addCallback(this, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (startupOnboardingViewpager.currentItem == 0) {
                    findNavController().popBackStack()
                } else
                    startupOnboardingViewpager.setCurrentItem(
                        kotlin.math.max(
                            startupOnboardingViewpager.currentItem - 1,
                            0
                        ),
                        true
                    )
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_startup_onboarding,
            container,
            false
        )
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        setupView(binding.root)

        return binding.root
    }

    private fun setupView(view: View) {
        startupOnoardingContinueButton = view.findViewById(R.id.startup_onboarding_continueButton)
        startupOnboardingNextButton = view.findViewById(R.id.startup_onboarding_nextButton)
        startupOnboardingSkipButton = view.findViewById(R.id.startup_onboarding_skipButton)
        startupOnboardingDots = view.findViewById(R.id.startup_onboarding_dots)
        startupOnboardingViewpager = view.findViewById(R.id.startup_onboarding_viewpager)
        toolBar = view.findViewById(R.id.toolBar)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        toolBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        viewPagerAdapter.pages = viewModel.getPages(arguments?.getBoolean("sent"))
        startupOnboardingViewpager.adapter = viewPagerAdapter
        startupOnboardingDots.setViewPager2(startupOnboardingViewpager)
        startupOnboardingNextButton.setOnClickListener {
            startupOnboardingViewpager.setCurrentItem(
                kotlin.math.min(
                    startupOnboardingViewpager.currentItem + 1,
                    viewPagerAdapter.itemCount
                ),
                true
            )
        }

        startupOnoardingContinueButton.setOnClickListener {
            navigateForward()
        }

        startupOnboardingSkipButton.setOnClickListener {
            navigateForward()
        }
    }

    override fun onPause() {
        super.onPause()
        startupOnboardingViewpager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

    override fun onResume() {
        super.onResume()
        startupOnboardingViewpager.registerOnPageChangeCallback(onPageChangeCallback)
    }

    private fun navigateForward() {
        with(findNavController()) {
            if (previousBackStackEntry!!.destination.id == R.id.helpFragment) {
                popBackStack()
            } else {
                navigate(
                    OnboardingFragmentDirections.actionRegistrationRegulations(
                        signComponentUseCase.executeCustomSign()
                    )
                )
            }
        }
    }
}

