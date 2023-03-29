package pl.gov.mf.etoll.front.tecs.transaction

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import pl.gov.mf.etoll.R
import pl.gov.mf.etoll.databinding.FragmentTecsTransactionBinding
import pl.gov.mf.etoll.front.BaseDatabindingFragment
import pl.gov.mf.etoll.ui.components.dialogs.TecsCancelTransactionDialogFragment
import pl.gov.mf.etoll.ui.components.dialogs.ridedetails.TecsSessionExpiredDialogFragment
import pl.gov.mf.mobile.ui.components.dialogs.DialogsHelper
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicOneActionDialogFragment
import pl.gov.mf.mobile.ui.components.dialogs.basic.BasicTwoActionsDialogFragment
import java.io.UnsupportedEncodingException
import javax.inject.Inject

class TecsTransactionFragment :
    BaseDatabindingFragment<TecsTransactionFragmentViewModel, TecsTransactionFragmentComponent>() {

    private lateinit var binding: FragmentTecsTransactionBinding

    @Inject
    lateinit var dialogsHelper: DialogsHelper

    private var appInForeground: Boolean = false

    override fun getBindings(): ViewDataBinding? = binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        component.inject(viewModel)
        val bundle = requireArguments()
        if (!bundle.containsKey("url"))
            throw IllegalStateException("Wrong view params")
        viewModel.webViewUrl = bundle.getString("url")!!
        viewModel.selectedAmount = bundle.getString("amount")!!
        lockBackPress { viewModel.onToolbarCrossClick() }
    }

    override fun onResume() {
        super.onResume()
        appInForeground = true
    }

    override fun onPause() {
        super.onPause()
        appInForeground = false
    }

    override fun createComponent(): TecsTransactionFragmentComponent = activityComponent.plus(
        TecsTransactionFragmentModule(this, lifecycle)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_tecs_transaction,
            container,
            false
        )

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tecsTransactionWebView = view.findViewById<WebView>(R.id.tecs_transaction_webview)
        // js is required for transaction to be possible
        tecsTransactionWebView.settings.javaScriptEnabled = true
        // listen to url changes on webview, and if it's using our schema - catch output
        tecsTransactionWebView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return if (url != null && url.startsWith("etoll://")) {
                    Log.d("WEBVIEW_DEBUG", "Received special call")
                    val queryParams = url.substringAfter("etoll://finished?").split("&")
                    val output = HashMap<String, String>()
                    queryParams.forEach {
                        val data = it.split("=")
                        output.put(data[0], data[1])
                    }

                    if (appInForeground) {
                        Log.d("TECS_INFO", "Foreground work, continuing")
                        viewModel.interpretTransactionOutput(output)
                    } else {
                        // cache info
                        Log.d("TECS_INFO", "Background work - caching")
                        viewModel.updateCache(output)
                    }

                    Log.e("WEBVIEW_DEBUG", output.toString())

                    true
                } else
                // do not override other links
                    false
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                webview: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                val url = request?.url
                url?.let { uri ->
                    if (uri.toString().startsWith("etoll://")) {
                        Log.e("WEBVIEW_DEBUG", "Received special call")
                        try {
                            val params: Set<String> = uri.queryParameterNames
                            val output = HashMap<String, String>()
                            params.forEach { output[it] = uri.getQueryParameter(it)!! }
                            if (appInForeground) {
                                Log.e("TECS_INFO", "Foreground work, continuing")
                                viewModel.interpretTransactionOutput(output)
                            } else {
                                // cache info
                                Log.e("TECS_INFO", "Background work - caching")
                                viewModel.updateCache(output)
                            }

                            Log.e("WEBVIEW_DEBUG", output.toString())
                        } catch (e: UnsupportedEncodingException) {
                            Log.e("WEBVIEW_DEBUG", "failed to decode source", e)
                        }
                        return true
                    }
                }
                // do not override other links
                return false
            }
        }
        // load transaction url
        tecsTransactionWebView.loadUrl(viewModel.webViewUrl)
        // sign for navigation changes
        viewModel.navigationDestinations.observe(viewLifecycleOwner) { target ->
            when (target) {
                TecsTransactionFragmentViewModel.NavigationDestinations.NONE -> {
                }
                TecsTransactionFragmentViewModel.NavigationDestinations.TRANSACTION_CANCELLED -> {
                    viewModel.resetNavigation()
                    findNavController().popBackStack(R.id.dashboardFragment, false)
                }
                TecsTransactionFragmentViewModel.NavigationDestinations.TRANSACTION_SESSION_EXPIRED -> {
                    TecsSessionExpiredDialogFragment.createAndShow(childFragmentManager)
                        ?.observe(viewLifecycleOwner) {
                            when (it) {
                                BasicOneActionDialogFragment.DialogResult.CONFIRMED -> {
                                    findNavController().popBackStack(R.id.dashboardFragment, false)
                                    viewModel.cancelTransaction()
                                }
                            }
                        }
                }
                TecsTransactionFragmentViewModel.NavigationDestinations.TRANSACTION_SUCCESS -> {
                    viewModel.resetNavigation()
                    // navigate to transaction finish
                    findNavController().navigate(
                        TecsTransactionFragmentDirections.actionFinishedTransaction(
                            true,
                            viewModel.selectedAmount,
                            "", "",
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                is TecsTransactionFragmentViewModel.NavigationDestinations.TRANSACTION_FAILED -> {
                    viewModel.resetNavigation()
                    // navigate to transaction error
                    findNavController().navigate(
                        TecsTransactionFragmentDirections.actionFinishedTransaction(
                            false,
                            viewModel.selectedAmount,
                            target.title ?: "",
                            target.content ?: "",
                            signComponentUseCase.executeCustomSign()
                        )
                    )
                }
                TecsTransactionFragmentViewModel.NavigationDestinations.USER_CANCEL_REQUEST -> {
                    viewModel.resetNavigation()
                    TecsCancelTransactionDialogFragment.showTecsCancelTransactionDialog(
                        childFragmentManager
                    )?.observe(viewLifecycleOwner) { result ->
                        when (result) {
                            BasicTwoActionsDialogFragment.DialogResult.CONFIRMED -> {
                                viewModel.cancelTransaction()
                            }
                            else -> {

                            }
                        }
                    }
                }
                else -> {
                    viewModel.resetNavigation()
                }
            }
        }
    }

    override fun shouldSecureThisFragment(): Boolean = true
}