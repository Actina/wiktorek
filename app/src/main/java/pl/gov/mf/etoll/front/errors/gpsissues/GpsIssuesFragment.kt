package pl.gov.mf.etoll.front.errors.gpsissues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentGpsissuesBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.mobile.utils.toObject

class GpsIssuesFragment : BaseDatabindingFragment<GpsIssuesViewModel, GpsIssuesFragmentComponent>() {

    lateinit var binding: FragmentGpsissuesBinding

    override fun createComponent(): GpsIssuesFragmentComponent =
        activityComponent.plus(GpsIssuesFragmentModule(this, lifecycle))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        requireActivity().onBackPressedDispatcher.addCallback(this, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onConfirmClick()
            }
        })
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_gpsissues,
            container,
            false
        )
        viewModel.setModel(requireArguments().getString("issues")!!.toObject())
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.shouldGoBack.observe(viewLifecycleOwner) { finish ->
            if (finish) {
                findNavController().popBackStack()
            }
        }
    }

}