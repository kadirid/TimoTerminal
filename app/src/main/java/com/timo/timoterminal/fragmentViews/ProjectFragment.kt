package com.timo.timoterminal.fragmentViews

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
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
import com.timo.timoterminal.entityClasses.ProjectTimeEntity
import com.timo.timoterminal.entityClasses.ProjectTimeTrackSetting
import com.timo.timoterminal.entityClasses.TaskEntity
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.ProjectFragmentViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.android.ext.android.inject
import java.util.Calendar

private const val ARG_USERID = "userId"
private const val ARG_CUSTOMER_TIME_TRACK = "customerTimeTrack"
private const val ARG_TIME_ENTRY_TYPE = "timeEntryType"
private const val ARG_CROSS_DAY = "crossDay"
private const val ARG_PROJECT_TIME_ENTITY = "projectTimeEntity"

class ProjectFragment : Fragment() {
    private val languageService: LanguageService by inject()
    private var userId: Long? = null
    private var isCustomerTimeTrack: Boolean = false
    private var timeEntryType: Long? = null
    private var crossDay: Boolean = false
    private var projectTimeEntity: ProjectTimeEntity? = null

    private lateinit var binding: FragmentProjectBinding

    private val viewModel: ProjectFragmentViewModel by inject()

    private var firstP = true
    private var firstT = true
    private var firstC = true
    private var firstAt = true
    private var firstS = true
    private var firstTm = true
    private var firstTk = true
    private var firstJ = true

    private val v = View.VISIBLE
    private val g = View.GONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USERID)
            isCustomerTimeTrack = it.getBoolean(ARG_CUSTOMER_TIME_TRACK, false)
            timeEntryType = it.getLong(ARG_TIME_ENTRY_TYPE, -1L)
            crossDay = it.getBoolean(ARG_CROSS_DAY, false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                projectTimeEntity =
                    it.getParcelable(ARG_PROJECT_TIME_ENTITY, ProjectTimeEntity::class.java)
            } else {
                @Suppress("DEPRECATION")
                projectTimeEntity = it.getParcelable(ARG_PROJECT_TIME_ENTITY)
            }
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rootView = requireActivity().window.decorView.findViewById<View>(android.R.id.content)

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            if (isAdded) {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.height
                val keypadHeight = screenHeight - rect.bottom
                if (keypadHeight > screenHeight * 0.15) {
                    // Keyboard is visible
                    val focusedView = requireActivity().currentFocus
                    if (focusedView != null) {
                        val p2 = focusedView.parent.parent as View
                        val p3 = p2.parent as View
                        var y = p2.y.toInt()
                        if (y == 0) {
                            y = p3.y.toInt()
                        }
                        requireActivity().runOnUiThread {
                            binding.fragmaneProjectScrollView.smoothScrollTo(0, y)
                        }
                    }

                    requireActivity().runOnUiThread {
                        val params =
                            binding.fragmaneProjectScrollView.layoutParams as LinearLayout.LayoutParams
                        if (params.height != rect.height() - 60) {
                            params.height = rect.height() - 60
                            params.weight = 0f
                            binding.fragmaneProjectScrollView.layoutParams = params
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        val params =
                            binding.fragmaneProjectScrollView.layoutParams as LinearLayout.LayoutParams
                        if (params.height != 0) {
                            params.height = 0
                            params.weight = 1f
                            binding.fragmaneProjectScrollView.layoutParams = params
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadForProjectTimeTrack(requireContext())
        binding.fromDate.setText(Utils.getDateFromGC(Utils.getCal()) as CharSequence)
    }

    override fun onPause() {
        super.onPause()
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
            binding.saveProjectTimeButton.isEnabled = false
            saveData()
            viewModel.viewModelScope.launch {
                withTimeout(1000) {
                    binding.saveProjectTimeButton.isEnabled = true
                }
            }
        }

        binding.descriptionCloseButton.setOnClickListener {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.description.windowToken, 0)
        }

        binding.projectFragmentButtonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.projectFragmentButtonProjectTimeList.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            val frag = ProjectTimeListFragment()
            val bundle = Bundle()
            bundle.putString("userId", userId?.toString())
            frag.arguments = bundle
            parentFragmentManager.commit {
                addToBackStack(null)
                replace(R.id.fragment_container_view, frag)
            }
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

        if (projectTimeEntity != null) {
            loadValuesFromOldProjectTimeEntity()
            binding.projectFragmentButtonProjectTimeList.visibility = g
        }
    }

    private fun loadValuesFromOldProjectTimeEntity() {
        binding.fromDate.setText(projectTimeEntity?.date ?: "")
        binding.toDate.setText(projectTimeEntity?.dateTo ?: "")
        binding.from.setText(projectTimeEntity?.from ?: "")
        binding.to.setText(projectTimeEntity?.to ?: "")
        binding.hours.setText(projectTimeEntity?.hours ?: "")
        binding.manDays.setText(projectTimeEntity?.manDays ?: "")
        binding.description.setText(projectTimeEntity?.description ?: "")
        binding.orderNo.setText(projectTimeEntity?.orderNo ?: "")
        binding.performanceLocation.setText(projectTimeEntity?.performanceLocation ?: "")
        binding.drivenKm.setText(projectTimeEntity?.drivenKm ?: "")
        binding.travelTime.setText(projectTimeEntity?.travelTime ?: "")
        binding.kmFlatRate.isChecked = projectTimeEntity?.kmFlatRate == "true"
        binding.billable.isChecked = projectTimeEntity?.billable == "true"
        binding.premiumable.isChecked = projectTimeEntity?.premium == "true"
        binding.unit.setText(projectTimeEntity?.units ?: "")
        binding.evaluation.setText(projectTimeEntity?.evaluation ?: "")
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
        val data = HashMap<String, String?>()
        if (!isValid()) {
            Utils.showErrorMessage(
                requireContext(),
                languageService.getText("ALLGEMEIN#Rote Felder korrigieren")
            )
            return
        }

        data["id"] = if (projectTimeEntity != null) projectTimeEntity?.id.toString() else null
        data["createdTime"] =
            if (projectTimeEntity != null) projectTimeEntity?.createdTime else null

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
        data["kmFlatRate"] = if (binding.kmFlatRate.isChecked) "true" else "false"
        data["billable"] = if (binding.billable.isChecked) "true" else "false"
        data["premium"] = if (binding.premiumable.isChecked) "true" else "false"
        data["units"] = binding.unit.text.toString()
        data["evaluation"] = binding.evaluation.text.toString()
        data["timeEntryType"] = timeEntryType.toString()

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
            taskSelection(selectedItem)
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

    private fun taskSelection(selectedItem: TaskEntity) {
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
            if (projectTimeEntity != null && firstTk) {
                firstTk = false
                val ticketName =
                    tickets.find { t -> t.ticketId.toString() == projectTimeEntity?.ticketId }?.ticketName
                binding.ticket.setText(ticketName ?: "", false)
            }
        }

        if (!isCustomerTimeTrack) {
            viewModel.showCustomers(selectedItem)
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
                if (projectTimeEntity != null && firstP) {
                    firstP = false
                    val projectName =
                        it.find { p -> p.projectId.toString() == projectTimeEntity?.projectId }?.projectName
                    binding.project.setText(projectName ?: "", false)
                    viewModel.showFilteredTasksForProject(
                        projectTimeEntity?.projectId?.toLong() ?: -1L
                    )
                }
            }
        }
        viewModel.liveTaskEntities.value = emptyList()
        viewModel.liveTaskEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = TaskEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.task.setAdapter(adapter)
                viewModel.liveTaskEntities.value = emptyList()
                if (projectTimeEntity != null && firstT) {
                    firstT = false
                    val task =
                        it.find { t -> t.taskId.toString() == projectTimeEntity?.taskId }
                    binding.task.setText(task?.taskName ?: "", false)
                    if (task != null) {
                        taskSelection(task)
                    }
                }
            }
        }
        viewModel.liveCustomerEntities.value = emptyList()
        viewModel.liveCustomerEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = CustomerEntityAdapter(requireContext(), R.layout.dropdown_small, it)
                binding.customer.setAdapter(adapter)
                viewModel.liveCustomerEntities.value = emptyList()
                if (projectTimeEntity != null && firstC) {
                    firstC = false
                    val customerName =
                        it.find { c -> c.id == projectTimeEntity?.customerId }?.name
                    binding.customer.setText(customerName ?: "", false)
                }
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
                if (projectTimeEntity != null && firstAt) {
                    val activityType =
                        it.find { a -> a.activityTypeId.toString() == projectTimeEntity?.activityType }
                    binding.activityType.setText(activityType?.activityTypeName ?: "", false)
                    if (activityType != null) {
                        val matrices =
                            viewModel.getActivityTypeMatrixId(activityType.activityTypeId)
                        if (matrices.isNotEmpty()) {
                            binding.activityTypeMatrix.setAdapter(
                                ActivityTypeMatrixEntityAdaptor(
                                    requireContext(),
                                    R.layout.dropdown_small,
                                    matrices
                                )
                            )
                            val matrixName =
                                matrices.find { m -> m.activityTypeMatrixId.toString() == projectTimeEntity?.activityTypeMatrix }?.activityTypeMatrixName
                            binding.activityTypeMatrix.setText(matrixName ?: "", false)
                        }
                    }
                }
            }
        }
        viewModel.liveTeamEntities.value = emptyList()
        viewModel.liveTeamEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = TeamEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.team.setAdapter(adapter)
                viewModel.liveTeamEntities.value = emptyList()
                if (projectTimeEntity != null && firstTm) {
                    firstTm = false
                    val teamName =
                        it.find { t -> t.teamId.toString() == projectTimeEntity?.teamId }?.teamName
                    binding.team.setText(teamName ?: "", false)
                }
            }
        }
        viewModel.liveSkillEntities.value = emptyList()
        viewModel.liveSkillEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = SkillEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.skillLevel.setAdapter(adapter)
                viewModel.liveSkillEntities.value = emptyList()
                if (projectTimeEntity != null && firstS) {
                    firstS = false
                    val skillName =
                        it.find { s -> s.skillId.toString() == projectTimeEntity?.skillLevel }?.skillName
                    binding.skillLevel.setText(skillName ?: "", false)
                }
            }
        }
        viewModel.liveJourneyEntities.value = emptyList()
        viewModel.liveJourneyEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adaptor = JourneyEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.journey.setAdapter(adaptor)
                viewModel.liveJourneyEntities.value = emptyList()
                if (projectTimeEntity != null && firstJ) {
                    firstJ = false
                    val journeyName =
                        it.find { j -> j.journeyId.toString() == projectTimeEntity?.journeyId }?.journeyName
                    binding.journey.setText(journeyName ?: "", false)
                    binding.travelTime.setText(projectTimeEntity?.travelTime ?: "")
                    binding.drivenKm.setText(projectTimeEntity?.drivenKm ?: "")
                    binding.performanceLocation.setText(
                        projectTimeEntity?.performanceLocation ?: ""
                    )
                    binding.kmFlatRate.isChecked = projectTimeEntity?.kmFlatRate == "1"
                }
            }
        }
        viewModel.liveMessage.value = ""
        viewModel.liveMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                requireActivity().runOnUiThread {
                    if (!it.startsWith('e')) {
                        Utils.showMessage(parentFragmentManager, it)
                        if (isAdded) {
                            if (projectTimeEntity != null) {
                                parentFragmentManager.popBackStack()
                            } else {
                                when (timeEntryType) {
                                    1L -> {
                                        binding.from.text = binding.to.text
                                        binding.to.text = null
                                    }

                                    3L, 5L, 8L -> {
                                        binding.hours.text = null
                                    }

                                    6L -> {
                                        binding.manDays.text = null
                                    }
                                }
                            }
                        }
                    } else
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
                    binding.textInputLayoutProjectTimeToDate.visibility =
                        if (crossDay) v else g
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
            }
        }

        viewModel.viewModelScope.launch {
            if (binding.textInputLayoutProjectTimeTo.visibility == v
                && perm("zeiterfassung.normal.auto-bis")
                && binding.textInputLayoutProjectTimeTo.hint?.last() != '*'
            ) {
                binding.textInputLayoutProjectTimeTo.hint =
                    "${binding.textInputLayoutProjectTimeTo.hint ?: ""}*"
            }
            if (binding.textInputLayoutProjectTimeUnit.visibility == v
                && perm("einheiten.pflicht")
                && it.entryType != 1
                && timeEntryType != 10L
                && binding.textInputLayoutProjectTimeUnit.hint?.last() != '*'
            ) {
                binding.textInputLayoutProjectTimeUnit.hint =
                    "${binding.textInputLayoutProjectTimeUnit.hint ?: ""}*"
            }
            if (binding.textInputLayoutProjectTimeCustomer.visibility == v
                && perm("tab_zeiterfassung.kunde.muss")
                && binding.textInputLayoutProjectTimeCustomer.hint?.last() != '*'
            ) {
                binding.textInputLayoutProjectTimeCustomer.hint =
                    "${binding.textInputLayoutProjectTimeCustomer.hint ?: ""}*"
            }
            if (binding.textInputLayoutProjectTimeSkillLevel.visibility == v
                && perm("zeiterfassung.skilllevel.pflicht")
                && binding.textInputLayoutProjectTimeSkillLevel.hint?.last() != '*'
            ) {
                binding.textInputLayoutProjectTimeSkillLevel.hint =
                    "${binding.textInputLayoutProjectTimeSkillLevel.hint ?: ""}*"
            }
            if (binding.descriptionContainer.visibility == v
                && perm("zeiterfassung.beschreibung.pflicht")
                && binding.textInputLayoutProjectTimeDescription.hint?.last() != '*'
            ) {
                binding.textInputLayoutProjectTimeDescription.hint =
                    "${binding.textInputLayoutProjectTimeDescription.hint ?: ""}*"
            }
            if (binding.textInputLayoutProjectTimePerformanceLocation.visibility == v
                && perm("zeiterfassung.leistungsort.pflicht")
                && binding.textInputLayoutProjectTimePerformanceLocation.hint?.last() != '*'
            ) {
                binding.textInputLayoutProjectTimePerformanceLocation.hint =
                    "${binding.textInputLayoutProjectTimePerformanceLocation.hint ?: ""}*"
            }
        }

        // Prüfe ob Layout-Neuanordnung nötig ist (wenn Einstellungen nicht dem Default entsprechen)
        val hasCustomOrder =
            it.fieldOrder.isNotBlank() && it.fieldOrder != ProjectTimeTrackSetting.DEFAULT_FIELD_ORDER

        if (!hasCustomOrder) {
            // Modify anchor of dropdowns for better positioning
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
        } else {
            reorderProjectLayout(it)
        }
    }

    private fun reorderProjectLayout(settings: ProjectTimeTrackSetting) {
        // Überprüfe, ob die Einstellungen vom Default abweichen
        val defaultOrder = ProjectTimeTrackSetting.DEFAULT_FIELD_ORDER
        val currentOrder = settings.fieldOrder.takeIf { it.isNotBlank() } ?: defaultOrder

        // Wenn die Reihenfolge dem Default entspricht, keine Neuanordnung nötig
        if (currentOrder == defaultOrder) return

        // Hole das ScrollView-Container
        val scrollViewContainer = binding.fragmentProjectRootLayout

        // Parse die Reihenfolge aus den Einstellungen
        val fieldOrder = currentOrder.split(',').map { it.trim() }.filter { it.isNotEmpty() }

        // Identifiziere alle Container-Views, die entfernt werden müssen
        val containersToRemove = listOfNotNull(
            binding.customerContainer,
            binding.textInputLayoutProjectTimeActivityType.parent as? ViewGroup,
            binding.textInputLayoutProjectTimeJourney.parent as? ViewGroup,
            binding.textInputLayoutProjectTimeEvaluation.parent as? ViewGroup
        ).filter { it != scrollViewContainer }

        // Entferne alle Container (aber nicht deren Kinder)
        containersToRemove.forEach { container ->
            (container.parent as? ViewGroup)?.removeView(container)
        }

        // Mapping von Keys zu Views (direkte Views, keine Container)
        val viewMap = mapOf(
            ProjectTimeTrackSetting.Keys.CUSTOMER to binding.textInputLayoutProjectTimeCustomer,
            ProjectTimeTrackSetting.Keys.PROJECT to binding.textInputLayoutProjectTimeProject,
            ProjectTimeTrackSetting.Keys.TASK to binding.textInputLayoutProjectTimeTask,
            ProjectTimeTrackSetting.Keys.TICKET to binding.textInputLayoutProjectTimeTicket,
            ProjectTimeTrackSetting.Keys.TEAM to binding.textInputLayoutProjectTimeTeam,
            ProjectTimeTrackSetting.Keys.ACTIVITY_TYPE to binding.textInputLayoutProjectTimeActivityType,
            ProjectTimeTrackSetting.Keys.ACTIVITY_TYPE_MATRIX to binding.textInputLayoutProjectTimeActivityTypeMatrix,
            ProjectTimeTrackSetting.Keys.SKILL_LEVEL to binding.textInputLayoutProjectTimeSkillLevel,
            ProjectTimeTrackSetting.Keys.UNIT to binding.textInputLayoutProjectTimeUnit,
            ProjectTimeTrackSetting.Keys.ORDER_NO to binding.textInputLayoutProjectTimeOrderNo,
            ProjectTimeTrackSetting.Keys.JOURNEY to binding.textInputLayoutProjectTimeJourney,
            ProjectTimeTrackSetting.Keys.TRAVEL_TIME to binding.textInputLayoutProjectTimeTravelTime,
            ProjectTimeTrackSetting.Keys.DRIVEN_KM to binding.textInputLayoutProjectTimeDrivenKm,
            ProjectTimeTrackSetting.Keys.DESCRIPTION to binding.descriptionContainer,
            ProjectTimeTrackSetting.Keys.PERFORMANCE_LOCATION to binding.textInputLayoutProjectTimePerformanceLocation,
            ProjectTimeTrackSetting.Keys.EVALUATION to binding.textInputLayoutProjectTimeEvaluation,
            ProjectTimeTrackSetting.Keys.BILLABLE to binding.billable,
            ProjectTimeTrackSetting.Keys.PREMIUMABLE to binding.premiumable,
            ProjectTimeTrackSetting.Keys.KM_FLAT_RATE to binding.kmFlatRate
        )

        val allViews = mutableListOf<View>()


        // Füge Views in der Reihenfolge aus fieldOrder hinzu
        for (key in fieldOrder) {
            val view = viewMap[key]
            if (view != null && view.visibility != View.GONE) {
                // Entferne View aus seinem aktuellen Parent (falls vorhanden)
                (view.parent as? ViewGroup)?.removeView(view)
                allViews.add(view)
            }
        }


        val amountOfChildren = binding.fromToHoursContainer.childCount
        val lastElOfContainer =
            if (amountOfChildren > 0) binding.fromToHoursContainer.getChildAt(amountOfChildren - 1) else null
        //set marginEnd to 0 for the last element in the container
        lastElOfContainer?.let {
            val layoutParams = it.layoutParams as? ViewGroup.MarginLayoutParams
            layoutParams?.marginEnd = 0
            it.layoutParams = layoutParams
        }

        // do the same with project container
        binding.textInputLayoutProjectTimeProject.let {
            val layoutParams = it.layoutParams as? ViewGroup.MarginLayoutParams
            layoutParams?.marginEnd = 0
            it.layoutParams = layoutParams
        }

        // Füge alle Views in der neuen Reihenfolge direkt zum ScrollView hinzu
        allViews.forEachIndexed { index, view ->
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                if (view == binding.descriptionContainer)
                    (75 * resources.displayMetrics.density).toInt()
                else
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Margin für alle Felder außer dem ersten
            if (index > 0) {
                layoutParams.topMargin = (8 * resources.displayMetrics.density).toInt()
            }

            view.layoutParams = layoutParams
            scrollViewContainer.addView(view)
        }
    }

    private fun changePositionForCustomerTimeTrack() {
        val parent = binding.customerContainer.parent as? ViewGroup
        parent?.let {
            val customerView = binding.customerContainer
            val projectView = binding.projectOrderUnitContainer
            val customerIndex = it.indexOfChild(customerView)
            val projectIndex = it.indexOfChild(projectView)
            if (customerIndex > projectIndex) {
                it.removeView(customerView)
                it.addView(customerView, projectIndex)
            }
        }
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

    private fun setLanguageTexts() {
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
    }

    private fun isValid(): Boolean {
        var error = false
        // ... Pflichtfeldprüfung ...
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
        if (binding.descriptionContainer.visibility == v) {
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

        if (binding.textInputLayoutProjectTimeFromDate.visibility == v) {
            if (binding.fromDate.text == null || binding.fromDate.text!!.isEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeFromDate.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeFromDate.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeToDate.visibility == v) {
            if (binding.toDate.text == null || binding.toDate.text!!.isEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeToDate.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeToDate.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeFrom.visibility == v) {
            if (binding.from.text == null || binding.from.text!!.isEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeFrom.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeFrom.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeFromDate.visibility == v) {
            if (binding.to.text == null || binding.to.text!!.isEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeTo.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeTo.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeHours.visibility == v) {
            if (binding.hours.text == null || binding.hours.text!!.isEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeHours.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeHours.error = null
            }
        }
        if (binding.textInputLayoutProjectTimeManDays.visibility == v) {
            if (binding.manDays.text == null || binding.manDays.text!!.isEmpty()) {
                error = true
                binding.textInputLayoutProjectTimeManDays.error = "Required"
            } else {
                binding.textInputLayoutProjectTimeManDays.error = null
            }
        }

        return !error
    }

    companion object {
        @JvmStatic
        fun newInstance(
            userId: Long,
            isCustomerTimeTrack: Boolean,
            timeEntryType: Long,
            crossDay: Boolean
        ) =
            ProjectFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                    putBoolean(ARG_CUSTOMER_TIME_TRACK, isCustomerTimeTrack)
                    putLong(ARG_TIME_ENTRY_TYPE, timeEntryType)
                    putBoolean(ARG_CROSS_DAY, crossDay)
                }
            }

        fun newInstance(
            userId: Long,
            isCustomerTimeTrack: Boolean,
            timeEntryType: Long,
            crossDay: Boolean,
            projectTimeEntity: ProjectTimeEntity
        ) =
            ProjectFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                    putBoolean(ARG_CUSTOMER_TIME_TRACK, isCustomerTimeTrack)
                    putLong(ARG_TIME_ENTRY_TYPE, timeEntryType)
                    putBoolean(ARG_CROSS_DAY, crossDay)
                    putParcelable(ARG_PROJECT_TIME_ENTITY, projectTimeEntity)
                }
            }
    }
}
