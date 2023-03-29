package pl.gov.mf.etoll.front.notificationhistory

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentNotificationHistoryBinding
import pl.gov.mf.etoll.front.MainActivity
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationSelectedSection
import pl.gov.mf.etoll.front.bottomNavigation.ds.BottomNavigationState
import pl.gov.mf.etoll.front.notificationhistory.NotificationHistoryFragmentViewModel.NavigationTarget
import pl.gov.mf.etoll.front.notificationhistory.list.NotificationHistoryAdapter
import pl.gov.mf.mobile.ui.components.adapters.UiAdapters.setNavigationIconVisible
import pl.gov.mf.mobile.utils.translate
import javax.inject.Inject


class NotificationHistoryFragment :
    BaseDatabindingFragment<NotificationHistoryFragmentViewModel, NotificationHistoryFragmentComponent>() {

    private lateinit var binding: FragmentNotificationHistoryBinding
    private lateinit var notificationHistoryList: RecyclerView
    private lateinit var notificationHistoryListToolbar: MaterialToolbar

    @Inject
    lateinit var adapter: NotificationHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)

        lockBackPress {
            viewModel.navigationIconClick()
        }
    }

    override fun getBindings(): ViewDataBinding? = binding

    override fun invalidateViewAfterModeChange() {
        super.invalidateViewAfterModeChange()
        adapter.notifyBindingsChanged()
    }

    override fun getBottomNavigationState(): BottomNavigationState =
        BottomNavigationState(true, BottomNavigationSelectedSection.LEFT)

    override fun createComponent(): NotificationHistoryFragmentComponent =
        (context as MainActivity).component.plus(
            NotificationHistoryFragmentModule(this, lifecycle)
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_notification_history,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        adapter.lifecycleOwner = viewLifecycleOwner
        setupView(binding.root)

        return binding.root
    }

    private fun setupView(view: View) {
        notificationHistoryList = view.findViewById(R.id.notification_history_list)
        notificationHistoryListToolbar = view.findViewById(R.id.notification_history_list_toolbar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationHistoryList.adapter = adapter

        handleToolbar()

        viewModel.navigation.observe(viewLifecycleOwner) { target ->
            when (target) {
                is NavigationTarget.ItemDetails -> {
                    viewModel.resetNavigation()
                    findNavController().navigate(
                        NotificationHistoryFragmentDirections.actionShowDetails(
                            signComponentUseCase.executeCustomSign(),
                            target.data.id!!
                        )
                    )
                }

                is NavigationTarget.Back -> findNavController().popBackStack()

                else -> return@observe
            }
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)

            if (items.isEmpty())
                hideToolbarMenu()
            else
                toolbarMenuItemsVisibilityChange(false)
        }

        viewModel.selectedMode.observe(viewLifecycleOwner) { enabled ->
            if (enabled) {
                notificationHistoryListToolbar.setNavigationIconVisible(true)
                setToolbarNavigationIcon(R.drawable.ic_cross_light)
                toolbarMenuItemsVisibilityChange(true)
            } else {
                notificationHistoryListToolbar.setNavigationIconVisible(false)
                toolbarMenuItemsVisibilityChange(false)
            }
        }
    }

    private fun handleToolbar() {
        notificationHistoryListToolbar.setNavigationOnClickListener {
            viewModel.navigationIconClick()
        }

        notificationHistoryListToolbar.menu.iterator().forEach {
            it.title = it.title.toString().translate(requireContext())
        }

        notificationHistoryListToolbar.setOnMenuItemClickListener { item ->
            return@setOnMenuItemClickListener when (item.itemId) {
                R.id.notifications_history_menu_select -> {
                    viewModel.enableSelectMode(true)
                    true
                }
                R.id.notifications_history_menu_delete_all -> {
                    viewModel.deleteAllItems()
                    true
                }
                R.id.notifications_history_menu_delete_selected -> {
                    viewModel.deleteSelectedItems()
                    true
                }
                else -> false
            }
        }
    }

    private fun setToolbarNavigationIcon(@DrawableRes id: Int) {
        notificationHistoryListToolbar.navigationIcon =
            ContextCompat.getDrawable(requireContext(), id)?.apply {
                setTint(Color.WHITE)
            }
    }

    private fun hideToolbarMenu() {
        val menu = notificationHistoryListToolbar.menu
        menu.findItem(R.id.notifications_history_menu_select).isVisible = false
        menu.findItem(R.id.notifications_history_menu_delete_all).isVisible = false
        menu.findItem(R.id.notifications_history_menu_delete_selected).isVisible = false
    }

    private fun toolbarMenuItemsVisibilityChange(inSelectMode: Boolean) {
        val menu = notificationHistoryListToolbar.menu
        menu.findItem(R.id.notifications_history_menu_select).isVisible = !inSelectMode
        menu.findItem(R.id.notifications_history_menu_delete_all).isVisible = !inSelectMode
        menu.findItem(R.id.notifications_history_menu_delete_selected).isVisible = inSelectMode
    }
}