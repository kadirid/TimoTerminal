package com.timo.timoterminal.fragmentViews

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentProjectBinding
import com.timo.timoterminal.entityAdaptor.ActivityTypeEntityAdaptor
import com.timo.timoterminal.entityAdaptor.ActivityTypeMatrixEntityAdaptor
import com.timo.timoterminal.entityAdaptor.CustomerEntityAdapter
import com.timo.timoterminal.entityAdaptor.JourneyEntityAdaptor
import com.timo.timoterminal.entityAdaptor.ProjectEntityAdaptor
import com.timo.timoterminal.entityAdaptor.SkillEntityAdaptor
import com.timo.timoterminal.entityAdaptor.TaskEntityAdaptor
import com.timo.timoterminal.entityAdaptor.TeamEntityAdaptor
import com.timo.timoterminal.entityAdaptor.TicketEntityAdaptor
import com.timo.timoterminal.entityClasses.ActivityTypeEntity
import com.timo.timoterminal.entityClasses.JourneyEntity
import com.timo.timoterminal.entityClasses.ProjectEntity
import com.timo.timoterminal.entityClasses.ProjectTimeTrackSetting
import com.timo.timoterminal.entityClasses.TaskEntity
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.ProjectFragmentViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Calendar

private const val ARG_USERID = "userId"
private const val ARG_CUSTOMER_TIME_TRACK = "customerTimeTrack"
private const val ARG_TIME_ENTRY_TYPE = "timeEntryType"
private const val ARG_CROSS_DAY = "crossDay"

class ProjectFragment : Fragment() {
    private val languageService: LanguageService by inject()// TODO Use Language Service to set hints and texts depending on external data
    private var userId: Long? = null
    private var isCustomerTimeTrack: Boolean = false
    private var timeEntryType: Long? = null
    private var crossDay: Boolean = false

    private var isEntry: Boolean = true

    private lateinit var binding: FragmentProjectBinding

    private val viewModel: ProjectFragmentViewModel by inject()

    private val v = View.VISIBLE
    private val g = View.GONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USERID)
            isCustomerTimeTrack = it.getBoolean(ARG_CUSTOMER_TIME_TRACK, false)
            timeEntryType = it.getLong(ARG_TIME_ENTRY_TYPE, -1L)
            crossDay = it.getBoolean(ARG_CROSS_DAY, false)
        }
        viewModel.userId = userId ?: -1L
        viewModel.isCustomerTimeTrack = isCustomerTimeTrack
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectBinding.inflate(inflater, container, false)

        setUp()
        isEntry = false

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadForProjectTimeTrack()

        binding.fromDate.setText(Utils.getDateFromGC(Utils.getCal()) as CharSequence)
    }

    override fun onPause() {
        super.onPause()
        isEntry = false
        viewModel.userId = -1L
    }

    private fun setUp() {
        binding.fragmentProjectRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }

        binding.fromDate.setOnClickListener {
            val greg = Utils.getCal()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = "%02d.%02d.%04d".format(dayOfMonth, month + 1, year)
                    binding.fromDate.setText(selectedDate)
                    viewModel.getJourneys(selectedDate)
                },
                greg.get(Calendar.YEAR),
                greg.get(Calendar.MONTH),
                greg.get(Calendar.DAY_OF_MONTH)
            )
            (activity as MainActivity?)?.restartTimer()
            datePickerDialog.show()
        }

        binding.toDate.setOnClickListener {
            val greg = Utils.getCal()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = "%02d.%02d.%04d".format(dayOfMonth, month + 1, year)
                    binding.toDate.setText(selectedDate)
                },
                greg.get(Calendar.YEAR),
                greg.get(Calendar.MONTH),
                greg.get(Calendar.DAY_OF_MONTH)
            )
            (activity as MainActivity?)?.restartTimer()
            datePickerDialog.show()
        }

        binding.from.setOnClickListener {
            val greg = Utils.getCal()
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = "%02d:%02d".format(hourOfDay, minute)
                    binding.from.setText(selectedTime)
                },
                greg.get(Calendar.HOUR_OF_DAY),
                greg.get(Calendar.MINUTE),
                true
            )
            (activity as MainActivity?)?.restartTimer()
            timePickerDialog.show()
        }

        binding.to.setOnClickListener {
            val greg = Utils.getCal()
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = "%02d:%02d".format(hourOfDay, minute)
                    binding.to.setText(selectedTime)
                },
                greg.get(Calendar.HOUR_OF_DAY),
                greg.get(Calendar.MINUTE),
                true
            )
            (activity as MainActivity?)?.restartTimer()
            timePickerDialog.show()
        }

        binding.hours.setOnClickListener {
            val greg = Utils.getCal()
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = "%02d:%02d".format(hourOfDay, minute)
                    binding.hours.setText(selectedTime)
                },
                greg.get(Calendar.HOUR_OF_DAY),
                greg.get(Calendar.MINUTE),
                true
            )
            (activity as MainActivity?)?.restartTimer()
            timePickerDialog.show()
        }

        binding.travelTime.setOnClickListener {
            val greg = Utils.getCal()
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val selectedTime = "%02d:%02d".format(hourOfDay, minute)
                    binding.travelTime.setText(selectedTime)
                },
                greg.get(Calendar.HOUR_OF_DAY),
                greg.get(Calendar.MINUTE),
                true
            )
            (activity as MainActivity?)?.restartTimer()
            timePickerDialog.show()
        }

        binding.saveProjectTimeButton.setOnClickListener {
            saveData()
        }

        binding.descriptionCloseButton.setOnClickListener {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.description.windowToken, 0)
        }

        setUpOnItemClickListeners()

        setLanguageTexts()

        observeLiveData()
        viewModel.viewModelScope.launch {
            hideNonVisibleItems()

            if (isCustomerTimeTrack) {
                changePositionForCustomerTimeTrack()
            }
        }
        addTextWatcher()
    }

    private fun addTextWatcher() {
        val timerRestartTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                (activity as MainActivity?)?.restartTimer()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.orderNo.addTextChangedListener(timerRestartTextWatcher)
        binding.unit.addTextChangedListener(timerRestartTextWatcher)
        binding.description.addTextChangedListener(timerRestartTextWatcher)
        binding.performanceLocation.addTextChangedListener(timerRestartTextWatcher)
        binding.drivenKm.addTextChangedListener(timerRestartTextWatcher)
        binding.evaluation.addTextChangedListener(timerRestartTextWatcher)
    }

    private fun saveData() {
        val data = HashMap<String, String>()
        if (!isValid()) {
            return
        }

        data["userId"] = userId.toString()
        data["date"] = binding.fromDate.text.toString()
        data["dateTo"] = binding.toDate.text.toString()
        data["from"] = binding.from.text.toString()
        data["to"] = binding.to.text.toString()
        data["hours"] = binding.hours.text.toString()
        data["manDays"] = binding.manDays.text.toString()
        data["description"] = binding.description.text.toString()
        val cAdapter = binding.customer.adapter as? CustomerEntityAdapter
        data["customerId"] =
            cAdapter?.getItemByName(binding.customer.text.toString())?.id.toString()
        val tiAdaptor = binding.ticket.adapter as? TicketEntityAdaptor
        data["ticketId"] = tiAdaptor?.getItemByName(binding.ticket.text.toString())?.ticketId
            .toString()
        val pAdapter = binding.project.adapter as? ProjectEntityAdaptor
        data["projectId"] =
            pAdapter?.getItemByName(binding.project.text.toString())?.projectId.toString()
        val tAdapter = binding.task.adapter as? TaskEntityAdaptor
        data["taskId"] =
            tAdapter?.getItemByName(binding.task.text.toString())?.taskId.toString()
        data["orderNo"] = binding.orderNo.text.toString()
        val atAdapter = binding.activityType.adapter as? ActivityTypeEntityAdaptor
        data["activityType"] = atAdapter?.getItemByName(binding.activityType.text.toString())
            ?.activityTypeId.toString()
        val atmAdapter = binding.activityTypeMatrix.adapter as? ActivityTypeMatrixEntityAdaptor
        data["activityTypeMatrix"] =
            atmAdapter?.getItemByName(binding.activityTypeMatrix.text.toString())
                ?.activityTypeMatrixId.toString()
        val sAdapter = binding.skillLevel.adapter as? SkillEntityAdaptor
        data["skillLevel"] = sAdapter?.getItemByName(binding.skillLevel.text.toString())?.skillId
            .toString()
        data["performanceLocation"] = binding.performanceLocation.text.toString()
        val teamAdapter = binding.team.adapter as? TeamEntityAdaptor
        data["teamId"] = teamAdapter?.getItemByName(binding.team.text.toString())?.teamId.toString()
        val jAdapter = binding.journey.adapter as? JourneyEntityAdaptor
        data["journeyId"] = jAdapter?.getItemByName(binding.journey.text.toString())?.journeyId
            .toString()
        data["travelTime"] = binding.travelTime.text.toString()
        data["drivenKm"] = binding.drivenKm.text.toString()
        data["kmFlatRate"] = if (binding.kmFlatRate.isChecked) "1" else "0"
        data["billable"] = if (binding.billable.isChecked) "1" else "0"
        data["premium"] = if (binding.premiumable.isChecked) "1" else "0"
        data["units"] = binding.unit.text.toString()
        data["evaluation"] = binding.evaluation.text.toString()

        viewModel.saveProjectTime(data, requireContext())
    }

    private fun defOnItemClickListener() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.project.windowToken, 0)
        (activity as MainActivity?)?.restartTimer()
    }

    private fun setUpOnItemClickListeners() {
        binding.project.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            val selectedItem = parent.getItemAtPosition(position) as ProjectEntity
            viewModel.showFilteredTasksForProject(selectedItem.projectId)
        }
        binding.task.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            val selectedItem = parent.getItemAtPosition(position) as TaskEntity
            if (selectedItem.abrechenbarPBaum != 3L) {
                binding.billable.isChecked = selectedItem.abrechenbarPBaum == 1L
            }
            if (selectedItem.abrechenbarDisabled) {
                binding.billable.isEnabled = false
            } else {
                binding.billable.isEnabled = true
            }
            if (binding.textInputLayoutProjectTimeDescription.visibility == v) {
                var hint = binding.textInputLayoutProjectTimeDescription.hint
                if (!hint.isNullOrEmpty()) {
                    if (hint.last() == '*') {
                        hint = hint.dropLast(1)
                    }
                    if (selectedItem.descriptionMust) {
                        hint = "${hint}*"
                    }
                }
                binding.textInputLayoutProjectTimeDescription.hint = hint
            }
            if (binding.textInputLayoutProjectTimeActivityType.visibility == v) {
                var hint = binding.textInputLayoutProjectTimeActivityType.hint
                if (!hint.isNullOrEmpty()) {
                    if (hint.last() == '*') {
                        hint = hint.dropLast(1)
                    }
                    if (selectedItem.activityTypeMust) {
                        hint = "${hint}*"
                    }
                }
                binding.textInputLayoutProjectTimeActivityType.hint = hint
            }
            if (binding.textInputLayoutProjectTimeOrderNo.visibility == v) {
                var hint = binding.textInputLayoutProjectTimeOrderNo.hint
                if (!hint.isNullOrEmpty()) {
                    if (hint.last() == '*') {
                        hint = hint.dropLast(1)
                    }
                    if (selectedItem.orderNoMust) {
                        hint = "${hint}*"
                    }
                }
                binding.textInputLayoutProjectTimeOrderNo.hint = hint
            }
            val activityTypeId = viewModel.getActivityTypeId(selectedItem.taskId)
            if (activityTypeId.isNotEmpty()) {
                binding.activityType.setText(activityTypeId, true)
            }
            if (binding.textInputLayoutProjectTimeTicket.visibility == v) {
                val tickets = viewModel.getTickets(selectedItem.taskId)
                binding.ticket.setAdapter(
                    TicketEntityAdaptor(
                        requireContext(),
                        R.layout.dropdown_small,
                        tickets
                    )
                )
            }
            if (!isCustomerTimeTrack) {
                viewModel.showCustomers(selectedItem)
            }
        }
        binding.customer.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            if (isCustomerTimeTrack) {
                val selectedItem =
                    parent.getItemAtPosition(position) as CustomerEntityAdapter.CustomerComboEntity
                viewModel.showFilteredProjectsForCustomer(selectedItem.id)
            }
        }
        binding.team.setOnItemClickListener { _, _, _, _ -> defOnItemClickListener() }
        binding.ticket.setOnItemClickListener { _, _, _, _ -> defOnItemClickListener() }
        binding.activityType.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            val selectedItem = parent.getItemAtPosition(position) as ActivityTypeEntity
            val matrices = viewModel.getActivityTypeMatrixId(selectedItem.activityTypeId)
            if (matrices.isNotEmpty()) {
                binding.activityTypeMatrix.setAdapter(
                    ActivityTypeMatrixEntityAdaptor(
                        requireContext(),
                        R.layout.dropdown_small,
                        matrices
                    )
                )
            }
        }
        binding.activityTypeMatrix.setOnItemClickListener { _, _, _, _ -> defOnItemClickListener() }
        binding.skillLevel.setOnItemClickListener { _, _, _, _ -> defOnItemClickListener() }
        binding.journey.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            val selectedItem = parent.getItemAtPosition(position) as JourneyEntity
            binding.travelTime.setText(selectedItem.journeyTime)
            binding.drivenKm.setText(selectedItem.journeyKm)
            binding.performanceLocation.setText(selectedItem.journeyLocation)
            binding.kmFlatRate.isChecked = selectedItem.journeyVehicle
        }
    }

    private fun observeLiveData() {
        viewModel.liveHideMask.value = false
        viewModel.liveHideMask.observe(viewLifecycleOwner) {
            if (it == true) {
                (activity as MainActivity?)?.hideLoadMask()
                viewModel.liveHideMask.value = false
            }
        }
        viewModel.liveShowMask.value = false
        viewModel.liveShowMask.observe(viewLifecycleOwner) {
            if (it == true) {
                (activity as MainActivity?)?.showLoadMask()
                viewModel.liveShowMask.value = false
            }
        }
        viewModel.liveShowOfflineNotice.value = false
        viewModel.liveShowOfflineNotice.observe(viewLifecycleOwner) {
            if (it == true) {
                requireActivity().runOnUiThread {
                    Utils.showMessage(
                        parentFragmentManager,
                        "Hinweis\n\nDaten sind eventuell nicht vollständig"
                    )
                }
                viewModel.liveShowOfflineNotice.value = false
            }
        }
        viewModel.liveProjectEntities.value = emptyList()
        viewModel.liveProjectEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = ProjectEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.project.setAdapter(adapter)
                viewModel.liveProjectEntities.value = emptyList()
            }
        }
        viewModel.liveTaskEntities.value = emptyList()
        viewModel.liveTaskEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = TaskEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.task.setAdapter(adapter)
                viewModel.liveTaskEntities.value = emptyList()
            }
        }
        viewModel.liveCustomerEntities.value = emptyList()
        viewModel.liveCustomerEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = CustomerEntityAdapter(requireContext(), R.layout.dropdown_small, it)
                binding.customer.setAdapter(adapter)
                viewModel.liveCustomerEntities.value = emptyList()
            }
        }
        viewModel.liveProjectTimeTrackSetting.value = null
        viewModel.liveProjectTimeTrackSetting.observe(viewLifecycleOwner) {
            if (it != null) {
                processProjectTimeTrackSetting(it)
                viewModel.liveProjectTimeTrackSetting.value = null
            }
        }
        viewModel.liveActivityTypeEntities.value = emptyList()
        viewModel.liveActivityTypeEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter =
                    ActivityTypeEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.activityType.setAdapter(adapter)
                viewModel.liveActivityTypeEntities.value = emptyList()
            }
        }
        viewModel.liveTeamEntities.value = emptyList()
        viewModel.liveTeamEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = TeamEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.team.setAdapter(adapter)
                viewModel.liveTeamEntities.value = emptyList()
            }
        }
        viewModel.liveSkillEntities.value = emptyList()
        viewModel.liveSkillEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = SkillEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.skillLevel.setAdapter(adapter)
                viewModel.liveSkillEntities.value = emptyList()
            }
        }
        viewModel.liveJourneyEntities.value = emptyList()
        viewModel.liveJourneyEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adaptor = JourneyEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.journey.setAdapter(adaptor)
                viewModel.liveJourneyEntities.value = emptyList()
            }
        }
        viewModel.liveMessage.value = ""
        viewModel.liveMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                requireActivity().runOnUiThread {
                    if (!it.startsWith('e'))
                        Utils.showMessage(parentFragmentManager, it)
                    else
                        Utils.showErrorMessage(requireContext(), it.substring(1))
                }
                viewModel.liveMessage.value = ""
            }
        }
    }

    private fun processProjectTimeTrackSetting(it: ProjectTimeTrackSetting) {
        // Hide values that should not be shown
        if (!it.activityType) {
            binding.textInputLayoutProjectTimeActivityType.visibility = g
            binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility = g
        }
        if (!it.activityTypeMatrix) {
            binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility = g
        }
        if (!it.billable) {
            binding.billable.visibility = g
        }
        if (!it.customer) {
            binding.textInputLayoutProjectTimeCustomer.visibility = g
        }
        if (!it.description) {
            binding.textInputLayoutProjectTimeDescription.visibility = g
            binding.descriptionCloseButton.visibility = g
        }
        if (!it.drivenKm) {
            binding.textInputLayoutProjectTimeDrivenKm.visibility = g
        }
        if (!it.evaluation) {
            binding.textInputLayoutProjectTimeEvaluation.visibility = g
        }
        if (!it.journey) {
            binding.textInputLayoutProjectTimeJourney.visibility = g
        }
        if (!it.kmFlatRate) {
            binding.kmFlatRate.visibility = g
        }
        if (!it.orderNo) {
            binding.textInputLayoutProjectTimeOrderNo.visibility = g
        }
        if (!it.performanceLocation) {
            binding.textInputLayoutProjectTimePerformanceLocation.visibility = g
        }
        if (!it.premiumable) {
            binding.premiumable.visibility = g
        }
        if (!it.skillLevel) {
            binding.textInputLayoutProjectTimeSkillLevel.visibility = g
        }
        if (!it.team) {
            binding.textInputLayoutProjectTimeTeam.visibility = g
        }
        if (!it.ticket) {
            binding.textInputLayoutProjectTimeTicket.visibility = g
        }
        if (!it.travelTime) {
            binding.textInputLayoutProjectTimeTravelTime.visibility = g
        }
        if (!it.unit) {
            binding.textInputLayoutProjectTimeUnit.visibility = g
        }

        if (it.entryType == 1) {
            binding.textInputLayoutProjectTimeFromDate.visibility = g
            binding.textInputLayoutProjectTimeToDate.visibility = g
            binding.textInputLayoutProjectTimeFrom.visibility = g
            binding.textInputLayoutProjectTimeTo.visibility = g
            binding.textInputLayoutProjectTimeHours.visibility = g
            binding.textInputLayoutProjectTimeManDays.visibility = g
        } else {
            when (timeEntryType) {
                1L -> {
                    binding.textInputLayoutProjectTimeFromDate.visibility = v
                    binding.textInputLayoutProjectTimeToDate.visibility = if (crossDay) v else g
                    binding.textInputLayoutProjectTimeFrom.visibility = v
                    binding.textInputLayoutProjectTimeTo.visibility = v
                    binding.textInputLayoutProjectTimeHours.visibility = g
                    binding.textInputLayoutProjectTimeManDays.visibility = g
                }

                3L, 5L, 8L -> {
                    binding.textInputLayoutProjectTimeFromDate.visibility = v
                    binding.textInputLayoutProjectTimeToDate.visibility = g
                    binding.textInputLayoutProjectTimeFrom.visibility = g
                    binding.textInputLayoutProjectTimeTo.visibility = g
                    binding.textInputLayoutProjectTimeHours.visibility = v
                    binding.textInputLayoutProjectTimeManDays.visibility = g
                }

                6L -> {
                    binding.textInputLayoutProjectTimeFromDate.visibility = v
                    binding.textInputLayoutProjectTimeToDate.visibility = g
                    binding.textInputLayoutProjectTimeFrom.visibility = g
                    binding.textInputLayoutProjectTimeTo.visibility = g
                    binding.textInputLayoutProjectTimeHours.visibility = g
                    binding.textInputLayoutProjectTimeManDays.visibility = v
                }

                10L -> {
                    binding.textInputLayoutProjectTimeFromDate.visibility = g
                    binding.textInputLayoutProjectTimeToDate.visibility = g
                    binding.textInputLayoutProjectTimeFrom.visibility = g
                    binding.textInputLayoutProjectTimeTo.visibility = g
                    binding.textInputLayoutProjectTimeHours.visibility = g
                    binding.textInputLayoutProjectTimeManDays.visibility = g
                }
            }
        }

        viewModel.viewModelScope.launch {
            if (binding.textInputLayoutProjectTimeTo.visibility == v && perm("zeiterfassung.normal.auto-bis")) {
                binding.textInputLayoutProjectTimeTo.hint =
                    "${binding.textInputLayoutProjectTimeTo.hint ?: ""}*"
            }
            if (binding.textInputLayoutProjectTimeUnit.visibility == v
                && perm("einheiten.pflicht")
                && it.entryType != 1
                && timeEntryType != 10L
            ) {
                binding.textInputLayoutProjectTimeUnit.hint =
                    "${binding.textInputLayoutProjectTimeUnit.hint ?: ""}*"
            }
            if (binding.textInputLayoutProjectTimeCustomer.visibility == v && perm("tab_zeiterfassung.kunde.muss")) {
                binding.textInputLayoutProjectTimeCustomer.hint =
                    "${binding.textInputLayoutProjectTimeCustomer.hint ?: ""}*"
            }
            if (binding.textInputLayoutProjectTimeSkillLevel.visibility == v && perm("zeiterfassung.skilllevel.pflicht")) {
                binding.textInputLayoutProjectTimeSkillLevel.hint =
                    "${binding.textInputLayoutProjectTimeSkillLevel.hint ?: ""}*"
            }
            if (binding.textInputLayoutProjectTimeDescription.visibility == v && perm("zeiterfassung.beschreibung.pflicht")) {
                binding.textInputLayoutProjectTimeDescription.hint =
                    "${binding.textInputLayoutProjectTimeDescription.hint ?: ""}*"
            }
            if (binding.textInputLayoutProjectTimePerformanceLocation.visibility == v && perm("zeiterfassung.leistungsort.pflicht")) {
                binding.textInputLayoutProjectTimePerformanceLocation.hint =
                    "${binding.textInputLayoutProjectTimePerformanceLocation.hint ?: ""}*"
            }
        }

        // Modify anchor of dropdowns not starting on the right
        if (it.team) {
            if (it.customer && !isCustomerTimeTrack && binding.textInputLayoutProjectTimeCustomer.visibility == v) {
                binding.team.dropDownAnchor = binding.customer.id
            } else if (isCustomerTimeTrack && binding.textInputLayoutProjectTimeTask.visibility == v) {
                binding.team.dropDownAnchor = binding.task.id
            }
        }

        if (it.skillLevel) {
            if (it.activityType && binding.textInputLayoutProjectTimeActivityType.visibility == v) {
                binding.skillLevel.dropDownAnchor = binding.activityType.id
            }
        }

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.fragmentProjectRootLayout)

        // Resolve issue of horizontal alignment
        if (!it.team) {
            if (isCustomerTimeTrack) {
                constraintSet.setMargin(
                    binding.textInputLayoutProjectTimeTask.id,
                    ConstraintSet.END,
                    0
                )
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeTask.id,
                    ConstraintSet.END,
                    binding.fragmentProjectRootLayout.id,
                    ConstraintSet.END,
                    0
                )
            } else {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeCustomer.id,
                    ConstraintSet.END,
                    binding.fragmentProjectRootLayout.id,
                    ConstraintSet.END,
                    0
                )
            }
        }

        if (!it.customer && !isCustomerTimeTrack && it.team) {
            constraintSet.constrainWidth(binding.textInputLayoutProjectTimeTeam.id, 0)
            constraintSet.connect(
                binding.textInputLayoutProjectTimeTeam.id,
                ConstraintSet.START,
                binding.fragmentProjectRootLayout.id,
                ConstraintSet.START,
                0
            )
        }

        if (!it.orderNo && !it.unit) {
            if (isCustomerTimeTrack) {
                constraintSet.setMargin(
                    binding.textInputLayoutProjectTimeCustomer.id,
                    ConstraintSet.END,
                    0
                )
            } else {
                constraintSet.setMargin(
                    binding.textInputLayoutProjectTimeProject.id,
                    ConstraintSet.END,
                    0
                )
            }
        }

        if (it.orderNo && !it.unit) {
            constraintSet.setMargin(
                binding.textInputLayoutProjectTimeOrderNo.id,
                ConstraintSet.END,
                0
            )
        }

        if (!it.activityType) {
            if (it.skillLevel) {
                constraintSet.setMargin(
                    binding.textInputLayoutProjectTimeSkillLevel.id,
                    ConstraintSet.LEFT,
                    0
                )
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeSkillLevel.id,
                    ConstraintSet.START,
                    binding.fragmentProjectRootLayout.id,
                    ConstraintSet.START,
                    0
                )
            }
        } else if (!it.activityTypeMatrix) {
            constraintSet.constrainWidth(binding.textInputLayoutProjectTimeActivityType.id, 0)
            if (!it.skillLevel) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeActivityType.id,
                    ConstraintSet.END,
                    binding.fragmentProjectRootLayout.id,
                    ConstraintSet.END,
                    0
                )
            } else {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeActivityType.id,
                    ConstraintSet.END,
                    binding.textInputLayoutProjectTimeSkillLevel.id,
                    ConstraintSet.START,
                    0
                )
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeSkillLevel.id,
                    ConstraintSet.START,
                    binding.textInputLayoutProjectTimeActivityType.id,
                    ConstraintSet.END,
                    5
                )
            }
        } else if (!it.skillLevel) {
            constraintSet.constrainWidth(binding.textInputLayoutProjectTimeActivityTypeMatrix.id, 0)
            constraintSet.connect(
                binding.textInputLayoutProjectTimeActivityTypeMatrix.id,
                ConstraintSet.END,
                binding.fragmentProjectRootLayout.id,
                ConstraintSet.END,
                0
            )
        }

        if (!it.journey) {
            if (it.travelTime) {
                constraintSet.clear(
                    binding.textInputLayoutProjectTimeTravelTime.id,
                    ConstraintSet.END
                )
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeTravelTime.id,
                    ConstraintSet.START,
                    binding.fragmentProjectRootLayout.id,
                    ConstraintSet.START,
                    0
                )
            }
            if (it.drivenKm) {
                constraintSet.clear(
                    binding.textInputLayoutProjectTimeDrivenKm.id,
                    ConstraintSet.END
                )
                if (it.travelTime) {
                    constraintSet.connect(
                        binding.textInputLayoutProjectTimeDrivenKm.id,
                        ConstraintSet.START,
                        binding.textInputLayoutProjectTimeTravelTime.id,
                        ConstraintSet.END,
                        5
                    )
                } else {
                    constraintSet.connect(
                        binding.textInputLayoutProjectTimeDrivenKm.id,
                        ConstraintSet.START,
                        binding.fragmentProjectRootLayout.id,
                        ConstraintSet.START,
                        0
                    )
                }
            }
            if (it.kmFlatRate) {
                constraintSet.clear(
                    binding.kmFlatRate.id,
                    ConstraintSet.END
                )
                if (it.drivenKm) {
                    constraintSet.connect(
                        binding.kmFlatRate.id,
                        ConstraintSet.START,
                        binding.textInputLayoutProjectTimeDrivenKm.id,
                        ConstraintSet.END,
                        5
                    )
                } else if (it.travelTime) {
                    constraintSet.connect(
                        binding.kmFlatRate.id,
                        ConstraintSet.START,
                        binding.textInputLayoutProjectTimeTravelTime.id,
                        ConstraintSet.END,
                        5
                    )
                } else {
                    constraintSet.connect(
                        binding.kmFlatRate.id,
                        ConstraintSet.START,
                        binding.fragmentProjectRootLayout.id,
                        ConstraintSet.START,
                        0
                    )
                }
            }
        } else {
            if (!it.travelTime && !it.drivenKm && !it.kmFlatRate) {
                constraintSet.setMargin(
                    binding.textInputLayoutProjectTimeJourney.id,
                    ConstraintSet.END,
                    0
                )
            }
        }

        if (!it.evaluation) {
            if (it.billable) {
                constraintSet.connect(
                    binding.billable.id,
                    ConstraintSet.START,
                    binding.fragmentProjectRootLayout.id,
                    ConstraintSet.START,
                    0
                )
            } else if (it.premiumable) {
                constraintSet.connect(
                    binding.premiumable.id,
                    ConstraintSet.START,
                    binding.fragmentProjectRootLayout.id,
                    ConstraintSet.START,
                    0
                )
            }
        }

        // Check and Resolve issues of vertical alignment
        val pjParams =
            binding.textInputLayoutProjectTimeProject.layoutParams as ConstraintLayout.LayoutParams
        val pjNext = getNextTopId(binding.project.id, it)
        if (pjParams.topToBottom != pjNext) {
            if (pjNext == -1) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeProject.id,
                    ConstraintSet.TOP,
                    binding.fragmentProjectRootLayout.id,
                    ConstraintSet.TOP,
                    0
                )
            } else {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeProject.id,
                    ConstraintSet.TOP,
                    pjNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.orderNo) {
            val oParams =
                binding.textInputLayoutProjectTimeOrderNo.layoutParams as ConstraintLayout.LayoutParams
            val oNext = getNextTopId(binding.orderNo.id, it)
            if (oParams.topToBottom != oNext) {
                if (oNext == -1) {
                    constraintSet.connect(
                        binding.textInputLayoutProjectTimeOrderNo.id,
                        ConstraintSet.TOP,
                        binding.fragmentProjectRootLayout.id,
                        ConstraintSet.TOP,
                        0
                    )
                } else {
                    constraintSet.connect(
                        binding.textInputLayoutProjectTimeOrderNo.id,
                        ConstraintSet.TOP,
                        oNext,
                        ConstraintSet.BOTTOM
                    )
                }
            }
        }

        if (it.unit) {
            val uParams =
                binding.textInputLayoutProjectTimeUnit.layoutParams as ConstraintLayout.LayoutParams
            val uNext = getNextTopId(binding.unit.id, it)
            if (uParams.topToBottom != uNext) {
                if (uNext == -1) {
                    constraintSet.connect(
                        binding.textInputLayoutProjectTimeUnit.id,
                        ConstraintSet.TOP,
                        binding.fragmentProjectRootLayout.id,
                        ConstraintSet.TOP,
                        0
                    )
                } else {
                    constraintSet.connect(
                        binding.textInputLayoutProjectTimeUnit.id,
                        ConstraintSet.TOP,
                        uNext,
                        ConstraintSet.BOTTOM
                    )
                }
            }
        }

        if (it.customer) {
            val cParams =
                binding.textInputLayoutProjectTimeCustomer.layoutParams as ConstraintLayout.LayoutParams
            val cNext = getNextTopId(binding.customer.id, it)
            if (cParams.topToBottom != cNext) {
                if (cNext == -1) {
                    constraintSet.connect(
                        binding.textInputLayoutProjectTimeCustomer.id,
                        ConstraintSet.TOP,
                        binding.fragmentProjectRootLayout.id,
                        ConstraintSet.TOP,
                        0
                    )
                } else {
                    constraintSet.connect(
                        binding.textInputLayoutProjectTimeCustomer.id,
                        ConstraintSet.TOP,
                        cNext,
                        ConstraintSet.BOTTOM
                    )
                }
            }
        }

        if (it.team) {
            val tParams =
                binding.textInputLayoutProjectTimeTeam.layoutParams as ConstraintLayout.LayoutParams
            val tNext = getNextTopId(binding.team.id, it)
            if (tParams.topToBottom != tNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeTeam.id,
                    ConstraintSet.TOP,
                    tNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.ticket) {
            val tParams =
                binding.textInputLayoutProjectTimeTicket.layoutParams as ConstraintLayout.LayoutParams
            val tNext = getNextTopId(binding.ticket.id, it)
            if (tParams.topToBottom != tNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeTicket.id,
                    ConstraintSet.TOP,
                    tNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.activityType) {
            val aParams =
                binding.textInputLayoutProjectTimeActivityType.layoutParams as ConstraintLayout.LayoutParams
            val aNext = getNextTopId(binding.activityType.id, it)
            if (aParams.topToBottom != aNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeActivityType.id,
                    ConstraintSet.TOP,
                    aNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.activityTypeMatrix) {
            val aParams =
                binding.textInputLayoutProjectTimeActivityTypeMatrix.layoutParams as ConstraintLayout.LayoutParams
            val aNext = getNextTopId(binding.activityTypeMatrix.id, it)
            if (aParams.topToBottom != aNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeActivityTypeMatrix.id,
                    ConstraintSet.TOP,
                    aNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.skillLevel) {
            val sParams =
                binding.textInputLayoutProjectTimeSkillLevel.layoutParams as ConstraintLayout.LayoutParams
            val sNext = getNextTopId(binding.skillLevel.id, it)
            if (sParams.topToBottom != sNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeSkillLevel.id,
                    ConstraintSet.TOP,
                    sNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.description) {
            val dParams =
                binding.textInputLayoutProjectTimeDescription.layoutParams as ConstraintLayout.LayoutParams
            val dNext = getNextTopId(binding.description.id, it)
            if (dParams.topToBottom != dNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeDescription.id,
                    ConstraintSet.TOP,
                    dNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.performanceLocation) {
            val pParams =
                binding.textInputLayoutProjectTimePerformanceLocation.layoutParams as ConstraintLayout.LayoutParams
            val pNext = getNextTopId(binding.performanceLocation.id, it)
            if (pParams.topToBottom != pNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimePerformanceLocation.id,
                    ConstraintSet.TOP,
                    pNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.journey) {
            val jParams =
                binding.textInputLayoutProjectTimeJourney.layoutParams as ConstraintLayout.LayoutParams
            val jNext = getNextTopId(binding.journey.id, it)
            if (jParams.topToBottom != jNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeJourney.id,
                    ConstraintSet.TOP,
                    jNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.travelTime) {
            val tParams =
                binding.textInputLayoutProjectTimeTravelTime.layoutParams as ConstraintLayout.LayoutParams
            val tNext = getNextTopId(binding.travelTime.id, it)
            if (tParams.topToBottom != tNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeTravelTime.id,
                    ConstraintSet.TOP,
                    tNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.drivenKm) {
            val dParams =
                binding.textInputLayoutProjectTimeDrivenKm.layoutParams as ConstraintLayout.LayoutParams
            val dNext = getNextTopId(binding.drivenKm.id, it)
            if (dParams.topToBottom != dNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeDrivenKm.id,
                    ConstraintSet.TOP,
                    dNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.kmFlatRate) {
            val kParams = binding.kmFlatRate.layoutParams as ConstraintLayout.LayoutParams
            val kNext = getNextTopId(binding.kmFlatRate.id, it)
            if (kParams.topToBottom != kNext) {
                constraintSet.connect(
                    binding.kmFlatRate.id,
                    ConstraintSet.TOP,
                    kNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.evaluation) {
            val eParams =
                binding.textInputLayoutProjectTimeEvaluation.layoutParams as ConstraintLayout.LayoutParams
            val eNext = getNextTopId(binding.evaluation.id, it)
            if (eParams.topToBottom != eNext) {
                constraintSet.connect(
                    binding.textInputLayoutProjectTimeEvaluation.id,
                    ConstraintSet.TOP,
                    eNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.billable) {
            val bParams = binding.billable.layoutParams as ConstraintLayout.LayoutParams
            val bNext = getNextTopId(binding.billable.id, it)
            if (bParams.topToBottom != bNext) {
                constraintSet.connect(
                    binding.billable.id,
                    ConstraintSet.TOP,
                    bNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        if (it.premiumable) {
            val pParams = binding.premiumable.layoutParams as ConstraintLayout.LayoutParams
            val pNext = getNextTopId(binding.premiumable.id, it)
            if (pParams.topToBottom != pNext) {
                constraintSet.connect(
                    binding.premiumable.id,
                    ConstraintSet.TOP,
                    pNext,
                    ConstraintSet.BOTTOM
                )
            }
        }

        val buParam = binding.saveProjectTimeButton.layoutParams as ConstraintLayout.LayoutParams
        val buNext = getNextTopId(binding.saveProjectTimeButton.id, it)
        if (buParam.topToBottom != buNext) {
            constraintSet.connect(
                binding.saveProjectTimeButton.id,
                ConstraintSet.TOP,
                buNext,
                ConstraintSet.BOTTOM
            )
        }

        constraintSet.applyTo(binding.fragmentProjectRootLayout)
        if (!it.customer && !isCustomerTimeTrack && it.team) {
            binding.textInputLayoutProjectTimeTeam.requestLayout()
        }

        if (!it.activityType) {
            if (it.activityTypeMatrix) {
                binding.textInputLayoutProjectTimeActivityTypeMatrix.requestLayout()
            } else if (it.skillLevel) {
                binding.textInputLayoutProjectTimeSkillLevel.requestLayout()
            }
        } else if (!it.activityTypeMatrix) {
            binding.textInputLayoutProjectTimeActivityType.requestLayout()
        } else if (!it.skillLevel) {
            binding.textInputLayoutProjectTimeActivityTypeMatrix.requestLayout()
        }
    }

    private suspend fun changePositionForCustomerTimeTrack() {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.fragmentProjectRootLayout)
        val bTeam = perm("teams.show") && perm("zeiterfassung.teamauswahl.use")
        val bInvoice = perm("auftragsnummer.show")
        val bUnit = perm("einheiten.use")

        if (bTeam) {
            constraintSet.connect(
                binding.textInputLayoutProjectTimeTeam.id,
                ConstraintSet.TOP,
                binding.textInputLayoutProjectTimeTask.id,
                ConstraintSet.TOP
            )
            constraintSet.connect(
                binding.textInputLayoutProjectTimeTask.id,
                ConstraintSet.END,
                binding.textInputLayoutProjectTimeTeam.id,
                ConstraintSet.START,
                5
            )
        } else {
            constraintSet.connect(
                binding.textInputLayoutProjectTimeTask.id,
                ConstraintSet.END,
                binding.fragmentProjectRootLayout.id,
                ConstraintSet.END,
                0
            )
        }
        constraintSet.connect(
            binding.textInputLayoutProjectTimeProject.id,
            ConstraintSet.TOP,
            binding.textInputLayoutProjectTimeCustomer.id,
            ConstraintSet.BOTTOM
        )
        constraintSet.connect(
            binding.textInputLayoutProjectTimeProject.id,
            ConstraintSet.END,
            binding.fragmentProjectRootLayout.id,
            ConstraintSet.END,
            0
        )
        constraintSet.connect(
            binding.textInputLayoutProjectTimeCustomer.id,
            ConstraintSet.TOP,
            binding.textInputLayoutProjectTimeFromDate.id,
            ConstraintSet.BOTTOM
        )
        if (bInvoice) {
            constraintSet.connect(
                binding.textInputLayoutProjectTimeCustomer.id,
                ConstraintSet.END,
                binding.textInputLayoutProjectTimeOrderNo.id,
                ConstraintSet.START,
                5
            )
        } else if (bUnit) {
            constraintSet.connect(
                binding.textInputLayoutProjectTimeCustomer.id,
                ConstraintSet.END,
                binding.textInputLayoutProjectTimeUnit.id,
                ConstraintSet.START,
                5
            )
        } else {
            constraintSet.connect(
                binding.textInputLayoutProjectTimeCustomer.id,
                ConstraintSet.END,
                binding.fragmentProjectRootLayout.id,
                ConstraintSet.END,
                0
            )
        }

        constraintSet.applyTo(binding.fragmentProjectRootLayout)
    }

    private suspend fun hideNonVisibleItems() {
        if (!perm("auftragsnummer.show")) binding.textInputLayoutProjectTimeOrderNo.visibility = g
        if (!perm("einheiten.use")) binding.textInputLayoutProjectTimeUnit.visibility = g
        val bCustomer = perm("kunde.use") && perm("tab_zeiterfassung.kunde.show")
        if (!bCustomer) binding.textInputLayoutProjectTimeCustomer.visibility = g
        val bTeam = perm("teams.show") && perm("zeiterfassung.teamauswahl.use")
        if (!bTeam) binding.textInputLayoutProjectTimeTeam.visibility = g
        if (!perm("tickets.use")) binding.textInputLayoutProjectTimeTicket.visibility = g
        if (!perm("zeiterfassung.leistungsart.use")) binding.textInputLayoutProjectTimeActivityType.visibility =
            g
        val bActivityTypeMatrix =
            perm("zeiterfassung.leistungsart.use") && perm("leistungsart.matrix.use")
        if (!bActivityTypeMatrix) binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility =
            g
        if (!perm("zeiterfassung.skilllevel.use")) binding.textInputLayoutProjectTimeSkillLevel.visibility =
            g
        val bJourney = perm("zeiterfassung.reise.use")
        val bJourneyCost = perm("reisekosten.use")
        if (!bJourney) binding.textInputLayoutProjectTimePerformanceLocation.visibility = g
        if (!bJourney || !bJourneyCost) {
            binding.textInputLayoutProjectTimeJourney.visibility = g
            binding.textInputLayoutProjectTimeTravelTime.visibility = g
            binding.textInputLayoutProjectTimeDrivenKm.visibility = g
            binding.kmFlatRate.visibility = g
        }
        val bEvaluation = perm("fertigstellungsgrad.use") && perm("zeiterfassung.abschaetzung.use")
        if (!bEvaluation) binding.textInputLayoutProjectTimeEvaluation.visibility = g
        if (!perm("abrechenbar.show")) binding.billable.visibility = g
        if (!perm("praemienrelevant.show")) binding.premiumable.visibility = g
    }

    private suspend fun perm(name: String): Boolean {
        return viewModel.permission(name) == "true"
    }

    private fun getNextTopId(id: Int, it: ProjectTimeTrackSetting): Int {
        when (id) {
            binding.orderNo.id,
            binding.unit.id -> {
                if (isCustomerTimeTrack) {
                    return getNextTopId(binding.customer.id, it)
                }
                return getNextTopId(binding.project.id, it)
            }

            binding.team.id -> {
                if (isCustomerTimeTrack) {
                    return getNextTopId(binding.task.id, it)
                }
                return getNextTopId(binding.customer.id, it)
            }

            binding.activityTypeMatrix.id,
            binding.skillLevel.id -> {
                return getNextTopId(binding.activityType.id, it)
            }

            binding.travelTime.id,
            binding.drivenKm.id,
            binding.kmFlatRate.id -> {
                return getNextTopId(binding.journey.id, it)
            }

            binding.billable.id,
            binding.premiumable.id,
            binding.saveProjectTimeButton.id -> {
                return getNextTopId(binding.evaluation.id, it)
            }
        }

        when (id) {
            binding.project.id -> {
                if (isCustomerTimeTrack && it.customer && binding.textInputLayoutProjectTimeCustomer.visibility == v) {
                    return binding.textInputLayoutProjectTimeCustomer.id
                }
                if (it.entryType != 1) {
                    if (binding.textInputLayoutProjectTimeFromDate.visibility == v) {
                        return binding.textInputLayoutProjectTimeFromDate.id
                    }
                    if (binding.textInputLayoutProjectTimeFrom.visibility == v) {
                        return binding.textInputLayoutProjectTimeFrom.id
                    }
                    if (binding.textInputLayoutProjectTimeToDate.visibility == v) {
                        return binding.textInputLayoutProjectTimeToDate.id
                    }
                    if (binding.textInputLayoutProjectTimeTo.visibility == v) {
                        return binding.textInputLayoutProjectTimeTo.id
                    }
                    if (binding.textInputLayoutProjectTimeHours.visibility == v) {
                        return binding.textInputLayoutProjectTimeHours.id
                    }
                    if (binding.textInputLayoutProjectTimeManDays.visibility == v) {
                        return binding.textInputLayoutProjectTimeManDays.id
                    }
                }
                return -1
            }

            binding.customer.id -> {
                if (isCustomerTimeTrack) {
                    if (it.entryType != 1) {
                        if (binding.textInputLayoutProjectTimeFromDate.visibility == v) {
                            return binding.textInputLayoutProjectTimeFromDate.id
                        }
                        if (binding.textInputLayoutProjectTimeToDate.visibility == v) {
                            return binding.textInputLayoutProjectTimeToDate.id
                        }
                        if (binding.textInputLayoutProjectTimeFrom.visibility == v) {
                            return binding.textInputLayoutProjectTimeFrom.id
                        }
                        if (binding.textInputLayoutProjectTimeTo.visibility == v) {
                            return binding.textInputLayoutProjectTimeTo.id
                        }
                        if (binding.textInputLayoutProjectTimeHours.visibility == v) {
                            return binding.textInputLayoutProjectTimeHours.id
                        }
                        if (binding.textInputLayoutProjectTimeManDays.visibility == v) {
                            return binding.textInputLayoutProjectTimeManDays.id
                        }
                    }
                    return -1
                }
                return binding.textInputLayoutProjectTimeTask.id
            }

            binding.ticket.id -> {
                if (it.customer && !isCustomerTimeTrack && binding.textInputLayoutProjectTimeCustomer.visibility == v) {
                    return binding.textInputLayoutProjectTimeCustomer.id
                }
                if (it.team && binding.textInputLayoutProjectTimeTeam.visibility == v) {
                    return binding.textInputLayoutProjectTimeTeam.id
                }
                return binding.textInputLayoutProjectTimeTask.id
            }

            binding.activityType.id -> {
                if (it.ticket && binding.textInputLayoutProjectTimeTicket.visibility == v) {
                    return binding.textInputLayoutProjectTimeTicket.id
                }
                return getNextTopId(binding.ticket.id, it)
            }

            binding.description.id -> {
                if (it.activityType && binding.textInputLayoutProjectTimeActivityType.visibility == v) {
                    return binding.textInputLayoutProjectTimeActivityType.id
                }
                if (it.activityTypeMatrix && binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility == v) {
                    return binding.textInputLayoutProjectTimeActivityTypeMatrix.id
                }
                if (it.skillLevel && binding.textInputLayoutProjectTimeSkillLevel.visibility == v) {
                    return binding.textInputLayoutProjectTimeSkillLevel.id
                }
                return getNextTopId(binding.activityType.id, it)
            }

            binding.performanceLocation.id -> {
                if (it.description && binding.textInputLayoutProjectTimeDescription.visibility == v) {
                    return binding.textInputLayoutProjectTimeDescription.id
                }
                return getNextTopId(binding.description.id, it)
            }

            binding.journey.id -> {
                if (it.performanceLocation && binding.textInputLayoutProjectTimePerformanceLocation.visibility == v) {
                    return binding.textInputLayoutProjectTimePerformanceLocation.id
                }
                return getNextTopId(binding.performanceLocation.id, it)
            }

            binding.evaluation.id -> {
                if (it.journey && binding.textInputLayoutProjectTimeJourney.visibility == v) {
                    return binding.textInputLayoutProjectTimeJourney.id
                }
                if (it.travelTime && binding.textInputLayoutProjectTimeTravelTime.visibility == v) {
                    return binding.textInputLayoutProjectTimeTravelTime.id
                }
                if (it.drivenKm && binding.textInputLayoutProjectTimeDrivenKm.visibility == v) {
                    return binding.textInputLayoutProjectTimeDrivenKm.id
                }
                if (it.kmFlatRate && binding.kmFlatRate.visibility == v) {
                    return binding.kmFlatRate.id
                }
                return getNextTopId(binding.journey.id, it)
            }
        }
        return -1
    }

    private fun setLanguageTexts() {
        binding.textInputLayoutProjectTimeFromDate.hint = languageService.getText("ALLGEMEIN#Von")
        binding.textInputLayoutProjectTimeFrom.hint = languageService.getText("ALLGEMEIN#Von")
        binding.textInputLayoutProjectTimeToDate.hint = languageService.getText("ALLGEMEIN#Bis")
        binding.textInputLayoutProjectTimeTo.hint = languageService.getText("ALLGEMEIN#Bis")
        binding.textInputLayoutProjectTimeHours.hint = languageService.getText("ALLGEMEIN#Stunden")
        binding.textInputLayoutProjectTimeManDays.hint =
            languageService.getText("CLIENT#CompData_ManDay")
        binding.textInputLayoutProjectTimeProject.hint =
            languageService.getText("ALLGEMEIN#Projekt")
        binding.textInputLayoutProjectTimeOrderNo.hint =
            languageService.getText("ALLGEMEIN#Auftragsnummer")
        binding.textInputLayoutProjectTimeUnit.hint =
            languageService.getText("CLIENT#Zeiterfassung_Einheiten")
        binding.textInputLayoutProjectTimeTask.hint = languageService.getText("ALLGEMEIN#Vorgang")
        binding.textInputLayoutProjectTimeCustomer.hint = languageService.getText("ALLGEMEIN#Kunde")
        binding.textInputLayoutProjectTimeTeam.hint = languageService.getText("TEAM#Team")
        binding.textInputLayoutProjectTimeTicket.hint = languageService.getText("ALLGEMEIN#Ticket")
        binding.textInputLayoutProjectTimeActivityType.hint =
            languageService.getText("ALLGEMEIN#Leistungsart")
        binding.textInputLayoutProjectTimeActivityTypeMatrix.hint =
            languageService.getText("Leistungsnachweis#Column_leistungsartmatrix")
        binding.textInputLayoutProjectTimeSkillLevel.hint =
            languageService.getText("ALLGEMEIN#Skilllevel")
        binding.textInputLayoutProjectTimeDescription.hint =
            languageService.getText("ALLGEMEIN#Beschreibung")
        binding.textInputLayoutProjectTimePerformanceLocation.hint =
            languageService.getText("ALLGEMEIN#Leistungsort")
        binding.textInputLayoutProjectTimeJourney.hint =
            languageService.getText("CLIENT#ReiseKosten_Reise")
        binding.textInputLayoutProjectTimeTravelTime.hint =
            languageService.getText("ALLGEMEIN#Fahrtzeit")
        binding.textInputLayoutProjectTimeDrivenKm.hint =
            languageService.getText("CLIENT#ReiseKosten_GefahreneKm")
        binding.textInputLayoutProjectTimeEvaluation.hint =
            languageService.getText("ALLGEMEIN#Abschaetzung")
        binding.kmFlatRate.text = languageService.getText("#KmPausch")
        binding.billable.text = languageService.getText("ALLGEMEIN#Abrechenbar")
        binding.premiumable.text = languageService.getText("CLIENT#Zeiterfassung_Praemienrelevant")
        binding.saveProjectTimeButton.text = languageService.getText("ALLGEMEIN#Speichern")
    }

    private fun isValid(): Boolean {
        var error = false

        if (binding.project.text.isNullOrEmpty()) {
            error = true
            binding.textInputLayoutProjectTimeProject.error = "Required"
        } else {
            binding.textInputLayoutProjectTimeProject.error = null
        }
        if (binding.task.text.isNullOrEmpty()) {
            error = true
            binding.textInputLayoutProjectTimeTask.error = "Required"
        } else {
            binding.textInputLayoutProjectTimeTask.error = null
        }

        if (binding.textInputLayoutProjectTimeFromDate.visibility == v) {
            if (binding.fromDate.text.isNullOrEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeFromDate.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeFromDate.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeToDate.visibility == v) {
            if (binding.toDate.text.isNullOrEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeToDate.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeToDate.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeFrom.visibility == v) {
            if (binding.from.text.isNullOrEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeFrom.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeFrom.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeTo.visibility == v) {
            if (binding.to.text.isNullOrEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeTo.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeTo.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeHours.visibility == v) {
            if (binding.hours.text.isNullOrEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeHours.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeHours.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeManDays.visibility == v) {
            if (binding.manDays.text.isNullOrEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeManDays.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeManDays.error = null
            }
        }

        if (binding.textInputLayoutProjectTimeDescription.visibility == v) {
            if (binding.textInputLayoutProjectTimeDescription.hint?.last() == '*') {
                if (binding.description.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeDescription.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeDescription.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimeActivityType.visibility == v) {
            if (binding.textInputLayoutProjectTimeActivityType.hint?.last() == '*') {
                if (binding.activityType.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeActivityType.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeActivityType.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimeOrderNo.visibility == v) {
            if (binding.textInputLayoutProjectTimeOrderNo.hint?.last() == '*') {
                if (binding.orderNo.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeOrderNo.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeOrderNo.error = null
                }
            }
        }

        if (binding.textInputLayoutProjectTimeTo.visibility == v) {
            if (binding.textInputLayoutProjectTimeTo.hint?.last() == '*') {
                if (binding.to.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeTo.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeTo.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimeUnit.visibility == v) {
            if (binding.textInputLayoutProjectTimeUnit.hint?.last() == '*') {
                if (binding.unit.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeUnit.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeUnit.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimeCustomer.visibility == v) {
            if (binding.textInputLayoutProjectTimeCustomer.hint?.last() == '*') {
                if (binding.customer.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeCustomer.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeCustomer.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimeSkillLevel.visibility == v) {
            if (binding.textInputLayoutProjectTimeSkillLevel.hint?.last() == '*') {
                if (binding.skillLevel.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeSkillLevel.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeSkillLevel.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimePerformanceLocation.visibility == v) {
            if (binding.textInputLayoutProjectTimePerformanceLocation.hint?.last() == '*') {
                if (binding.performanceLocation.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimePerformanceLocation.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimePerformanceLocation.error = null
                }
            }
        }

        return !error
    }

    companion object {
        fun newInstance(
            userId: Long,
            customerTimeTrack: Boolean,
            timeEntryType: Long,
            crossDay: Boolean
        ) =
            ProjectFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                    putBoolean(ARG_CUSTOMER_TIME_TRACK, customerTimeTrack)
                    putLong(ARG_TIME_ENTRY_TYPE, timeEntryType)
                    putBoolean(ARG_CROSS_DAY, crossDay)
                }
            }
    }
}