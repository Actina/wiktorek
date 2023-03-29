package pl.gov.mf.etoll.front.notificationdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.iterator
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentNotificationDetailsBinding
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.notificationdetails.NotificationDetailsFragmentViewModel.*
import pl.gov.mf.mobile.utils.translate

class NotificationDetailsFragment :
    BaseDatabindingFragment<NotificationDetailsFragmentViewModel, NotificationDetailsFragmentComponent>() {

    private lateinit var binding: FragmentNotificationDetailsBinding
    private lateinit var notificationDetailsListToolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        viewModel.initialize(requireArguments().getLong("detailsId"), requireContext())
    }

    override fun createComponent(): NotificationDetailsFragmentComponent =
        (context as MainActivity).component.plus(
            NotificationDetailsFragmentModule(this, lifecycle)
        )

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_notification_details,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        setupView(binding.root)
        return binding.root
    }

    private fun setupView(view: View) {
        notificationDetailsListToolbar = view.findViewById(R.id.notification_details_list_toolbar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationDetailsListToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        handleToolbar()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.navigation.observe(viewLifecycleOwner) {
            when (it) {
                NavigationTarget.Back ->
                    findNavController().popBackStack()
                else -> return@observe
            }
        }
    }

    private fun handleToolbar() {
        notificationDetailsListToolbar.menu.iterator().forEach {
            it.title = it.title.toString().translate(requireContext())
        }

        notificationDetailsListToolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.notification_details_menu_delete -> {
                    viewModel.deleteItem()
                    true
                }
                else -> false
            }
        }
    }
}