package pl.gov.mf.etoll.ui.components.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.joda.time.format.DateTimeFormat
import pl.gov.mf.etoll.ui.components.R
import pl.gov.mf.etoll.ui.components.databinding.CalendarDialogBinding
import pl.gov.mf.mobile.ui.components.dialogs.BlurableDatabindingDialogFragment
import java.util.*

class CalendarDialogFragment : BlurableDatabindingDialogFragment() {

    companion object {
        fun createDialog(
            mindate: Long,
            initialDate: Long,
            maxDate: Long,
            localeTag: String,
        ): CalendarDialogFragment = CalendarDialogFragment().apply {
            val bundle = Bundle()
            bundle.putString(ARG_LOCALE, localeTag)
            bundle.putLong(ARG_INITIALDATE, initialDate)
            bundle.putLong(ARG_MINDATE, mindate)
            bundle.putLong(ARG_MAXDATE, maxDate)
            arguments = bundle
        }

        private const val ARG_LOCALE = "ARG_LOCALE"
        private const val ARG_MINDATE = "ARG_MINDATE"
        private const val ARG_MAXDATE = "ARG_MAXDATE"
        private const val ARG_INITIALDATE = "ARG_INITIALDATE"
    }

    private lateinit var binding: CalendarDialogBinding
    private val yearFormatter = DateTimeFormat.forPattern("yyyy")
    private val dateFormatter = DateTimeFormat.forPattern("EEEE, dd MMMMM")
    private val _dialogResult = MutableLiveData<CalendarDialogData>()
    private val dialogResult: LiveData<CalendarDialogData> = _dialogResult
    private var selectedDate: Long = 0
    private var minDate: Long = 0
    private var maxDate: Long = 0

    private lateinit var currentLocale: Locale

    private var vm = object :
        CalendarViewModelSchema {

        override fun onConfirm() {
            _dialogResult.postValue(CalendarDialogData(selectedDate))
            dismiss()
        }

        override fun onClear() {
            _dialogResult.postValue(CalendarDialogData(null))
            dismiss()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().let {
            minDate =
                it.getLong(ARG_MINDATE, Date().time)
            maxDate =
                it.getLong(ARG_MAXDATE, Date().time)
            selectedDate = it.getLong(ARG_INITIALDATE, Date().time)
            currentLocale = Locale.forLanguageTag(it.getString(ARG_LOCALE, "pl"))
        }
    }

    override fun getBindings(): ViewDataBinding = binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        setLocaleForCalendarView()

        binding = DataBindingUtil.inflate(
            LayoutInflater.from(
                context
            ), R.layout.calendar_dialog, null, false
        )
        binding.viewModelSchema = vm
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar(binding.calendarViewDark)
        setupCalendar(binding.calendarViewLight)
    }

    private fun setupCalendar(calendarView: CalendarView) {
        calendarView.minDate = minDate
        calendarView.maxDate = maxDate
        calendarView.date = selectedDate
        printSelectedDate()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = Date(year - 1900, month, dayOfMonth).time
            printSelectedDate()
        }
    }

    private fun setLocaleForCalendarView() {
        val configuration = requireContext().resources.configuration
        configuration.setLocale(currentLocale)
        configuration.setLayoutDirection(currentLocale)
        requireContext().createConfigurationContext(configuration)
    }

    private fun printSelectedDate() {
        Locale.setDefault(currentLocale)
        binding.calendarDialogYear.text = yearFormatter.print(selectedDate)
        binding.calendarDialogDate.text =
            dateFormatter.print(selectedDate)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun showDialog(fm: FragmentManager, tag: String): LiveData<CalendarDialogData> {
        show(fm, tag)
        return dialogResult
    }

    data class CalendarDialogData(
        val date: Long?,
    )

    interface CalendarViewModelSchema {
        fun onConfirm()
        fun onClear()
    }
}