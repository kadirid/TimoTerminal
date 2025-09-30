package com.timo.timoterminal.fragmentViews

// --- Imports ---
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.asFlow
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentProjectStopwatchBinding
import com.timo.timoterminal.entityAdaptor.*
import com.timo.timoterminal.entityClasses.*
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.ProjectFragmentViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger

/**
 * Fragment für die Projekt-Zeiterfassung (Stoppuhr).
 *
 * Verantwortlichkeiten:
 * - Anzeige und Aktualisierung von Datum/Uhrzeit
 * - Eingabefelder für Projekt, Aufgabe, Kunde, Tickets, Leistungsarten, Reise etc.
 * - Dynamische Sichtbarkeit der UI je nach Berechtigung und Server-Settings
 * - Validierung und Übergabe der Nutzereingaben an das ViewModel
 */
class ProjectFragmentStopwatch : Fragment() {

    // --- Companion Object ---
    companion object {
        private const val ARG_USERID = "userId"
        private const val ARG_CUSTOMER_TIME_TRACK = "customerTimeTrack"
        private const val ARG_TIME_ENTRY_TYPE = "timeEntryType"
        private const val ARG_CROSS_DAY = "crossDay"

        fun newInstance(
            userId: Long,
            customerTimeTrack: Boolean,
            timeEntryType: Long,
            crossDay: Boolean
        ) = ProjectFragmentStopwatch().apply {
            arguments = Bundle().apply {
                putLong(ARG_USERID, userId)
                putBoolean(ARG_CUSTOMER_TIME_TRACK, customerTimeTrack)
                putLong(ARG_TIME_ENTRY_TYPE, timeEntryType)
                putBoolean(ARG_CROSS_DAY, crossDay)
            }
        }
    }

    // --- Properties ---
    private val logger = Logger.getLogger(ProjectFragmentStopwatch::class.java.name)
    private val languageService: LanguageService by inject()
    private val viewModel: ProjectFragmentViewModel by inject()
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private lateinit var binding: FragmentProjectStopwatchBinding
    private var userId: Long? = null
    private var isCustomerTimeTrack: Boolean = false
    private var timeEntryType: Long? = null
    private var crossDay: Boolean = false
    private var isEntry: Boolean = true
    private var flatReorderMode: Boolean = false
    private var hasAppliedCurrentWT = false
    private var populationInProgress = false

    // --- Lifecycle Methods ---
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectStopwatchBinding.inflate(inflater, container, false)
        setUp()
        isEntry = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler.post(updateTimeRunnable)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadForProjectTimeTrack(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateTimeRunnable)
    }

    // --- Private Methods ---
    private fun setUp() {
        binding.fragmentProjectRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        setUpAnimations()
        setUpClickListeners()
        setUpOnItemClickListeners()
        setLanguageTexts()
        observeLiveData()
        viewLifecycleOwner.lifecycleScope.launch {
            hideNonVisibleItems()
        }
        addTextWatcher()
    }

    private fun setUpAnimations() {
        val fadeOutSlideOutLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_slide_out_left)
        val fadeInSlideInRight = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_slide_in_right)
        val fadeInSlideInLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_slide_in_left)
        val fadeOutSlideOutRight = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_slide_out_right)

        binding.goToSecondPageButton.setOnClickListener {
            fadeOutSlideOutLeft.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    binding.firstPageDetailLayout.visibility = View.INVISIBLE
                    binding.secondPageContainer.visibility = View.VISIBLE
                    binding.secondPageContainer.startAnimation(fadeInSlideInRight)
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            binding.firstPageDetailLayout.startAnimation(fadeOutSlideOutLeft)
        }
        binding.goBackToFirstPageButton.setOnClickListener {
            fadeOutSlideOutRight.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    binding.secondPageContainer.visibility = View.INVISIBLE
                    binding.firstPageDetailLayout.visibility = View.VISIBLE
                    binding.firstPageDetailLayout.startAnimation(fadeInSlideInLeft)
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            binding.secondPageContainer.startAnimation(fadeOutSlideOutRight)
        }
    }

    private fun setUpClickListeners() {
        binding.fabClose.setOnClickListener {
            parentFragmentManager.popBackStack()
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

        binding.descriptionCloseButton.setOnClickListener {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.description.windowToken, 0)
        }

        binding.startProjectTimeButton.setOnClickListener { startProjectTime() }
        binding.stopProjectTimeButton.setOnClickListener { stopProjectTime() }
    }

    private fun setUpOnItemClickListeners() {
        val v = View.VISIBLE

        binding.project.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            val selectedItem = parent.getItemAtPosition(position) as ProjectEntity
            binding.task.setText("", false)
            binding.customer.setText("", false)
            hasAppliedCurrentWT = true
            if (!isCustomerTimeTrack) viewModel.showCustomersForProject(selectedItem.projectId)
            viewModel.showFilteredTasksForProject(selectedItem.projectId)
        }

        binding.task.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            val selectedItem = parent.getItemAtPosition(position) as TaskEntity

            if (selectedItem.abrechenbarPBaum != 3L) {
                binding.billable.isChecked = selectedItem.abrechenbarPBaum == 1L
            }
            binding.billable.isEnabled = !selectedItem.abrechenbarDisabled

            updateHintForRequiredField(binding.textInputLayoutProjectTimeDescription, selectedItem.descriptionMust)
            updateHintForRequiredField(binding.textInputLayoutProjectTimeActivityType, selectedItem.activityTypeMust)
            updateHintForRequiredField(binding.textInputLayoutProjectTimeOrderNo, selectedItem.orderNoMust)

            val activityTypeId = viewModel.getActivityTypeId(selectedItem.taskId)
            if (activityTypeId.isNotEmpty()) {
                binding.activityType.setText(activityTypeId, true)
            }

            if (binding.textInputLayoutProjectTimeTicket.visibility == v) {
                val tickets = viewModel.getTickets(selectedItem.taskId)
                binding.ticket.setAdapter(
                    TicketEntityAdaptor(requireContext(), R.layout.dropdown_small, tickets)
                )
            }

            if (!isCustomerTimeTrack) {
                viewModel.showCustomers(selectedItem)
            }
        }

        binding.customer.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            if (isCustomerTimeTrack) {
                val selectedItem = parent.getItemAtPosition(position) as CustomerEntityAdapter.CustomerComboEntity
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
                    ActivityTypeMatrixEntityAdaptor(requireContext(), R.layout.dropdown_small, matrices)
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

    private fun updateHintForRequiredField(textInputLayout: com.google.android.material.textfield.TextInputLayout, isRequired: Boolean) {
        if (textInputLayout.visibility != View.VISIBLE) return
        var hint = textInputLayout.hint
        if (!hint.isNullOrEmpty()) {
            if (hint.last() == '*') hint = hint.dropLast(1)
            if (isRequired) hint = "${hint}*"
            textInputLayout.hint = hint
        }
    }

    private fun defOnItemClickListener() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.project.windowToken, 0)
        (activity as MainActivity?)?.restartTimer()
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
                    Utils.showMessage(parentFragmentManager, "Hinweis\n\nDaten sind eventuell nicht vollständig")
                }
                viewModel.liveShowOfflineNotice.value = false
            }
        }
        viewModel.liveProjectEntities.value = emptyList()
        viewModel.liveProjectEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = ProjectEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.project.setAdapter(adapter)
                attemptPopulateFromWT()
                viewModel.liveProjectEntities.value = emptyList()
            }
        }
        viewModel.liveTaskEntities.value = emptyList()
        viewModel.liveTaskEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = TaskEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.task.setAdapter(adapter)
                attemptPopulateFromWT()
                viewModel.liveTaskEntities.value = emptyList()
            }
        }
        viewModel.liveCustomerEntities.value = emptyList()
        viewModel.liveCustomerEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = CustomerEntityAdapter(requireContext(), R.layout.dropdown_small, it)
                binding.customer.setAdapter(adapter)
                attemptPopulateFromWT()
                viewModel.liveCustomerEntities.value = emptyList()
            }
        }
        viewModel.liveProjectTimeTrackSetting.value = null
        viewModel.liveProjectTimeTrackSetting.observe(viewLifecycleOwner) {
            if (it != null) {
                processProjectTimeTrackSetting(it)
                viewLifecycleOwner.lifecycleScope.launch {
                    hideNonVisibleItems()
                    arrangeInputsBasedOnSettings(it)
                }
                viewModel.liveProjectTimeTrackSetting.value = null
            }
        }
        viewModel.liveActivityTypeEntities.value = emptyList()
        viewModel.liveActivityTypeEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = ActivityTypeEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.activityType.setAdapter(adapter)
                attemptPopulateFromWT()
                viewModel.liveActivityTypeEntities.value = emptyList()
            }
        }
        viewModel.liveTeamEntities.value = emptyList()
        viewModel.liveTeamEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = TeamEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.team.setAdapter(adapter)
                attemptPopulateFromWT()
                viewModel.liveTeamEntities.value = emptyList()
            }
        }
        viewModel.liveSkillEntities.value = emptyList()
        viewModel.liveSkillEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = SkillEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.skillLevel.setAdapter(adapter)
                attemptPopulateFromWT()
                viewModel.liveSkillEntities.value = emptyList()
            }
        }
        viewModel.liveJourneyEntities.value = emptyList()
        viewModel.liveJourneyEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adaptor = JourneyEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
                binding.journey.setAdapter(adaptor)
                attemptPopulateFromWT()
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

        viewModel.currentOpenWorkingTime.observe(viewLifecycleOwner) { wt ->
            updateWorkingTimeButtons(wt)
            if (wt != null && !hasAppliedCurrentWT) {
                val pid = wt.projectId.toLongOrNull()
                if (pid != null) {
                    viewModel.showTaskForProject(pid)
                    if (!isCustomerTimeTrack) {
                        viewModel.showCustomersForProject(pid)
                    }
                }
            }
            attemptPopulateFromWT()
        }
    }

    // --- UI: Sprache setzen ---
    private fun setLanguageTexts() {
        binding.textInputLayoutProjectTimeProject.hint = languageService.getText("ALLGEMEIN#Projekt")
        binding.textInputLayoutProjectTimeOrderNo.hint = languageService.getText("ALLGEMEIN#Auftragsnummer")
        binding.textInputLayoutProjectTimeUnit.hint = languageService.getText("CLIENT#Zeiterfassung_Einheiten")
        binding.textInputLayoutProjectTimeTask.hint = languageService.getText("ALLGEMEIN#Vorgang")
        binding.textInputLayoutProjectTimeCustomer.hint = languageService.getText("ALLGEMEIN#Kunde")
        binding.textInputLayoutProjectTimeTeam.hint = languageService.getText("TEAM#Team")
        binding.textInputLayoutProjectTimeTicket.hint = languageService.getText("ALLGEMEIN#Ticket")
        binding.textInputLayoutProjectTimeActivityType.hint = languageService.getText("ALLGEMEIN#Leistungsart")
        binding.textInputLayoutProjectTimeActivityTypeMatrix.hint = languageService.getText("Leistungsnachweis#Column_leistungsartmatrix")
        binding.textInputLayoutProjectTimeSkillLevel.hint = languageService.getText("ALLGEMEIN#Skilllevel")
        binding.textInputLayoutProjectTimeDescription.hint = languageService.getText("ALLGEMEIN#Beschreibung")
        binding.textInputLayoutProjectTimePerformanceLocation.hint = languageService.getText("ALLGEMEIN#Leistungsort")
        binding.textInputLayoutProjectTimeJourney.hint = languageService.getText("CLIENT#ReiseKosten_Reise")
        binding.textInputLayoutProjectTimeTravelTime.hint = languageService.getText("ALLGEMEIN#Fahrtzeit")
        binding.textInputLayoutProjectTimeDrivenKm.hint = languageService.getText("CLIENT#ReiseKosten_GefahreneKm")
        binding.textInputLayoutProjectTimeEvaluation.hint = languageService.getText("ALLGEMEIN#Abschaetzung")
        binding.kmFlatRate.text = languageService.getText("#KmPausch")
        binding.billable.text = languageService.getText("ALLGEMEIN#Abrechenbar")
        binding.premiumable.text = languageService.getText("CLIENT#Zeiterfassung_Praemienrelevant")
    }

    private suspend fun hideNonVisibleItems() {
        val g = View.GONE
        val permKeys = listOf(
            "auftragsnummer.show", "einheiten.use", "kunde.use", "tab_zeiterfassung.kunde.show",
            "teams.show", "zeiterfassung.teamauswahl.use", "tickets.use", "zeiterfassung.leistungsart.use",
            "leistungsart.matrix.use", "zeiterfassung.skilllevel.use", "zeiterfassung.reise.use",
            "reisekosten.use", "fertigstellungsgrad.use", "zeiterfassung.abschaetzung.use",
            "abrechenbar.show", "praemienrelevant.show"
        )
        val permValues = permKeys.associateWith { perm(it) }
        logger.info("Permission-Keys und Werte: $permValues")

        if (!perm("auftragsnummer.show")) binding.textInputLayoutProjectTimeOrderNo.visibility = g
        if (!perm("einheiten.use")) binding.textInputLayoutProjectTimeUnit.visibility = g
        val bCustomer = perm("kunde.use") && perm("tab_zeiterfassung.kunde.show")
        if (!bCustomer) binding.textInputLayoutProjectTimeCustomer.visibility = g
        val bTeam = perm("teams.show") && perm("zeiterfassung.teamauswahl.use")
        if (!bTeam) binding.textInputLayoutProjectTimeTeam.visibility = g
        if (!perm("tickets.use")) binding.textInputLayoutProjectTimeTicket.visibility = g
        if (!perm("zeiterfassung.leistungsart.use")) binding.textInputLayoutProjectTimeActivityType.visibility = g
        val bActivityTypeMatrix = perm("zeiterfassung.leistungsart.use") && perm("leistungsart.matrix.use")
        if (!bActivityTypeMatrix) binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility = g
        if (!perm("zeiterfassung.skilllevel.use")) binding.textInputLayoutProjectTimeSkillLevel.visibility = g
        val bJourney = perm("zeiterfassung.reise.use")
        val bJourneyCost = perm("reisekosten.use")
        if (!(bJourney || bJourneyCost)) {
            binding.textInputLayoutProjectTimeJourney.visibility = g
            binding.textInputLayoutProjectTimeTravelTime.visibility = g
            binding.textInputLayoutProjectTimeDrivenKm.visibility = g
            binding.kmFlatRate.visibility = g
        }
        val bEvaluation = perm("fertigstellungsgrad.use") && perm("zeiterfassung.abschaetzung.use")
        if (!bEvaluation) binding.textInputLayoutProjectTimeEvaluation.visibility = g
        if (!perm("abrechenbar.show")) binding.billable.visibility = g
        if (!perm("praemienrelevant.show")) binding.premiumable.visibility = g
        updatePufferComponents()
    }

    private suspend fun perm(name: String): Boolean {
        return viewModel.permission(name) == "true"
    }

    private fun processProjectTimeTrackSetting(it: ProjectTimeTrackSetting) {
        val g = View.GONE
        if (!it.activityType) {
            binding.textInputLayoutProjectTimeActivityType.visibility = g
            binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility = g
        }
        if (!it.activityTypeMatrix) binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility = g
        if (!it.billable) binding.billable.visibility = g
        if (!it.customer) binding.textInputLayoutProjectTimeCustomer.visibility = g
        if (!it.description) {
            binding.textInputLayoutProjectTimeDescription.visibility = g
            binding.descriptionCloseButton.visibility = g
        }
        if (!it.drivenKm) binding.textInputLayoutProjectTimeDrivenKm.visibility = g
        if (!it.evaluation) binding.textInputLayoutProjectTimeEvaluation.visibility = g
        if (!it.journey) binding.textInputLayoutProjectTimeJourney.visibility = g
        if (!it.kmFlatRate) binding.kmFlatRate.visibility = g
        if (!it.orderNo) binding.textInputLayoutProjectTimeOrderNo.visibility = g
        if (!it.performanceLocation) binding.textInputLayoutProjectTimePerformanceLocation.visibility = g
        if (!it.premiumable) binding.premiumable.visibility = g
        if (!it.skillLevel) binding.textInputLayoutProjectTimeSkillLevel.visibility = g
        if (!it.team) binding.textInputLayoutProjectTimeTeam.visibility = g
        if (!it.ticket) binding.textInputLayoutProjectTimeTicket.visibility = g
        if (!it.travelTime) binding.textInputLayoutProjectTimeTravelTime.visibility = g
        if (!it.unit) binding.textInputLayoutProjectTimeUnit.visibility = g
        updatePufferComponents()
    }

    private fun addTextWatcher() {
        val timerRestartTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                (activity as MainActivity?)?.restartTimer()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        listOf(
            binding.orderNo, binding.unit, binding.description,
            binding.performanceLocation, binding.drivenKm, binding.evaluation
        ).forEach { it.addTextChangedListener(timerRestartTextWatcher) }
    }

    private fun isValid(startWt: Boolean): Boolean {
        val v = View.VISIBLE
        var error = false

        val requiredFields = listOf(
            binding.textInputLayoutProjectTimeProject to binding.project.text,
            binding.textInputLayoutProjectTimeTask to binding.task.text
        )

        requiredFields.forEach { (layout, text) ->
            if (text.isNullOrEmpty()) {
                error = true
                layout.error = "Required"
            } else {
                layout.error = null
            }
        }

        if (!startWt) {
            val conditionalFields = listOf(
                binding.textInputLayoutProjectTimeDescription to binding.description.text,
                binding.textInputLayoutProjectTimeOrderNo to binding.orderNo.text,
                binding.textInputLayoutProjectTimeUnit to binding.unit.text
            )

            conditionalFields.forEach { (layout, text) ->
                if (layout.visibility == v && layout.hint?.last() == '*') {
                    if (text.isNullOrEmpty()) {
                        error = true
                        layout.error = "Required"
                    } else {
                        layout.error = null
                    }
                }
            }
        }

        val alwaysRequiredFields = listOf(
            binding.textInputLayoutProjectTimeActivityType to binding.activityType.text,
            binding.textInputLayoutProjectTimeCustomer to binding.customer.text,
            binding.textInputLayoutProjectTimeSkillLevel to binding.skillLevel.text,
            binding.textInputLayoutProjectTimePerformanceLocation to binding.performanceLocation.text
        )

        alwaysRequiredFields.forEach { (layout, text) ->
            if (layout.visibility == v && layout.hint?.last() == '*') {
                if (text.isNullOrEmpty()) {
                    error = true
                    layout.error = "Required"
                } else {
                    layout.error = null
                }
            }
        }

        return !error
    }

    private fun updateWorkingTimeButtons(wt: ProjectTimeEntity?) {
        if (!this::binding.isInitialized) return
        binding.startProjectTimeButton.visibility = View.VISIBLE
        binding.stopProjectTimeButton.visibility = if (wt != null) View.VISIBLE else View.GONE
    }

    // --- Auto-Population Logic ---
    private fun attemptPopulateFromWT() {
        if (!this::binding.isInitialized || hasAppliedCurrentWT || populationInProgress) return
        val wt = viewModel.currentOpenWorkingTime.value ?: return

        populationInProgress = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                populateSequentially(wt)
                hasAppliedCurrentWT = true
                logger.info("Population completed successfully!")
            } catch (e: Exception) {
                logger.warning("Population failed: ${e.message}")
            } finally {
                populationInProgress = false
            }
        }
    }

    private suspend fun populateSequentially(wt: ProjectTimeEntity) {
        logger.info("Starting sequential population for WT: projectId=${wt.projectId}, taskId=${wt.taskId}")

        // 1) Projekt setzen und Tasks laden
        if (wt.projectId.isNotEmpty() && binding.project.text.isNullOrEmpty()) {
            logger.info("Step 1: Setting project ${wt.projectId}")
            if (setProjectById(wt.projectId)) {
                logger.info("Step 1: Project set, loading tasks/customers")
                val pid = wt.projectId.toLongOrNull()
                if (pid != null) {
                    viewModel.showTaskForProject(pid)
                    awaitAdapterReady { binding.task.adapter?.count ?: 0 > 0 }
                    logger.info("Step 1: Tasks loaded")

                    if (!isCustomerTimeTrack) {
                        viewModel.showCustomersForProject(pid)
                        awaitAdapterReady { binding.customer.adapter?.count ?: 0 > 0 }
                        logger.info("Step 1: Customers loaded")
                    }
                }
            }
        }

        // 2) Task setzen und spezifische Kunden laden
        if (wt.taskId.isNotEmpty() && binding.task.text.isNullOrEmpty()) {
            logger.info("Step 2: Setting task ${wt.taskId}")
            if (setTaskById(wt.taskId)) {
                logger.info("Step 2: Task set successfully")
                if (!isCustomerTimeTrack) {
                    val taskEntity = findInAdapter(binding.task.adapter) { (it as? TaskEntity)?.taskId?.toString() == wt.taskId } as? TaskEntity
                    taskEntity?.let {
                        viewModel.showCustomers(it)
                        awaitAdapterReady {
                            val adapter = binding.customer.adapter
                            adapter != null && adapter.count > 0
                        }
                        logger.info("Step 2: Task-specific customers loaded")
                    }
                }
                if (binding.textInputLayoutProjectTimeTicket.visibility == View.VISIBLE) {
                    val taskEntity = findInAdapter(binding.task.adapter) { (it as? TaskEntity)?.taskId?.toString() == wt.taskId } as? TaskEntity
                    taskEntity?.let { t ->
                        val tickets = viewModel.getTickets(t.taskId)
                        binding.ticket.setAdapter(TicketEntityAdaptor(requireContext(), R.layout.dropdown_small, tickets))
                    }
                }
            }
        }

        // Ab hier alles synchron ohne Warten
        logger.info("Step 3+: Setting all remaining fields")
        populateRemainingFields(wt)
    }

    private fun populateRemainingFields(wt: ProjectTimeEntity) {
        // Dropdowns
        if (wt.customerId.isNotEmpty() && binding.customer.text.isNullOrEmpty()) {
            logger.info("Setting customer ${wt.customerId}")
            setCustomerById("kid_"+wt.customerId)
        }

        if (wt.activityType.isNotEmpty() && binding.activityType.text.isNullOrEmpty()) {
            logger.info("Setting activityType ${wt.activityType}")
            if (setActivityTypeById(wt.activityType)) {
                val atEntity = findInAdapter(binding.activityType.adapter) { (it as? ActivityTypeEntity)?.activityTypeId?.toString() == wt.activityType } as? ActivityTypeEntity
                atEntity?.let { entity ->
                    val matrices = viewModel.getActivityTypeMatrixId(entity.activityTypeId)
                    if (matrices.isNotEmpty()) {
                        binding.activityTypeMatrix.setAdapter(
                            ActivityTypeMatrixEntityAdaptor(requireContext(), R.layout.dropdown_small, matrices)
                        )
                        logger.info("ActivityTypeMatrix adapter created")
                    }
                }
            }
        }

        if (wt.activityTypeMatrix.isNotEmpty() && binding.activityTypeMatrix.text.isNullOrEmpty()) {
            logger.info("Setting activityTypeMatrix ${wt.activityTypeMatrix}")
            setActivityTypeMatrixById(wt.activityTypeMatrix)
        }

        // Weitere Dropdowns
        logger.info("Setting remaining dropdown fields")
        if (wt.skillLevel.isNotEmpty() && binding.skillLevel.text.isNullOrEmpty()) setSkillByName(wt.skillLevel)
        if (wt.teamId.isNotEmpty() && binding.team.text.isNullOrEmpty()) setTeamById(wt.teamId)
        if (wt.journeyId.isNotEmpty() && binding.journey.text.isNullOrEmpty()) setJourneyById(wt.journeyId)

        // Textfelder
        logger.info("Setting text fields")
        val textFields = mapOf(
            binding.description to wt.description,
            binding.orderNo to wt.orderNo,
            binding.performanceLocation to wt.performanceLocation,
            binding.travelTime to wt.travelTime,
            binding.drivenKm to wt.drivenKm,
            binding.evaluation to wt.evaluation
        )
        textFields.forEach { (field, value) ->
            if (field.text.isNullOrEmpty() && value.isNotEmpty()) field.setText(value)
        }
        if (binding.unit.text.isNullOrEmpty() && wt.units.isNotEmpty() && wt.units != "0.0") {
            binding.unit.setText(wt.units)
        }

        // Checkboxes
        logger.info("Setting checkboxes")
        if (!binding.kmFlatRate.isChecked && wt.kmFlatRate == "1") binding.kmFlatRate.isChecked = true
        if (!binding.billable.isChecked && wt.billable == "1") binding.billable.isChecked = true
        if (!binding.premiumable.isChecked && wt.premium == "1") binding.premiumable.isChecked = true
    }

    private suspend fun awaitAdapterReady(condition: () -> Boolean) {
        var attempts = 0
        val maxAttempts = 50 // 5 Sekunden bei 100ms Intervals

        while (!condition() && attempts < maxAttempts) {
            kotlinx.coroutines.delay(100)
            attempts++
        }

        if (attempts >= maxAttempts) {
            logger.warning("Timeout waiting for adapter to be ready")
        }
    }

    private fun findInAdapter(adapter: android.widget.ListAdapter?, predicate: (Any?) -> Boolean): Any? {
        adapter ?: return null
        for (i in 0 until adapter.count) {
            val item = adapter.getItem(i)
            if (predicate(item)) return item
        }
        return null
    }

    private fun setProjectById(idStr: String): Boolean {
        val adapter = binding.project.adapter ?: return false
        val entity = findInAdapter(adapter) { (it as? ProjectEntity)?.projectId?.toString() == idStr } as? ProjectEntity
        if (entity != null && binding.project.text.isNullOrEmpty()) {
            binding.project.setText(entity.projectName, false)
            return true
        }
        // Falls nicht gefunden, Feld leer lassen und trotzdem true zurückgeben
        logger.warning("Project with ID $idStr not found in adapter, leaving field empty")
        return true
    }

    private fun setTaskById(idStr: String): Boolean {
        val adapter = binding.task.adapter ?: return false
        val entity = findInAdapter(adapter) { (it as? TaskEntity)?.taskId?.toString() == idStr } as? TaskEntity
        if (entity != null && binding.task.text.isNullOrEmpty()) {
            binding.task.setText(entity.taskName, false)
            if (entity.abrechenbarPBaum != 3L) binding.billable.isChecked = entity.abrechenbarPBaum == 1L
            binding.billable.isEnabled = !entity.abrechenbarDisabled
            val activityTypeId = viewModel.getActivityTypeId(entity.taskId)
            if (activityTypeId.isNotEmpty() && binding.activityType.text.isNullOrEmpty()) binding.activityType.setText(activityTypeId, true)
            return true
        }
        // Falls nicht gefunden, Feld leer lassen und trotzdem true zurückgeben
        logger.warning("Task with ID $idStr not found in adapter, leaving field empty")
        return true
    }

    private fun setCustomerById(idStr: String): Boolean {
        val adapter = binding.customer.adapter ?: return false
        val entity = findInAdapter(adapter) { (it as? CustomerEntityAdapter.CustomerComboEntity)?.id == idStr } as? CustomerEntityAdapter.CustomerComboEntity
        if (entity != null && binding.customer.text.isNullOrEmpty()) {
            binding.customer.setText(entity.name, false)
        } else {
            logger.warning("Customer with ID $idStr not found in adapter, leaving field empty")
        }
        return true
    }

    private fun setActivityTypeById(idStr: String): Boolean {
        val adapter = binding.activityType.adapter ?: return false
        val entity = findInAdapter(adapter) { (it as? ActivityTypeEntity)?.activityTypeId?.toString() == idStr } as? ActivityTypeEntity
        if (entity != null && binding.activityType.text.isNullOrEmpty()) {
            binding.activityType.setText(entity.activityTypeName, false)
        } else {
            logger.warning("ActivityType with ID $idStr not found in adapter, leaving field empty")
        }
        return true
    }

    private fun setActivityTypeMatrixById(idStr: String): Boolean {
        val adapter = binding.activityTypeMatrix.adapter ?: return false
        val entity = findInAdapter(adapter) { (it as? ActivityTypeMatrixEntity)?.activityTypeMatrixId?.toString() == idStr } as? ActivityTypeMatrixEntity
        if (entity != null && binding.activityTypeMatrix.text.isNullOrEmpty()) {
            binding.activityTypeMatrix.setText(entity.activityTypeMatrixName, false)
        } else {
            logger.warning("ActivityTypeMatrix with ID $idStr not found in adapter, leaving field empty")
        }
        return true
    }

    private fun setSkillByName(name: String): Boolean {
        val adapter = binding.skillLevel.adapter ?: return false
        val entity = findInAdapter(adapter) { (it as? SkillEntity)?.skillName?.toString() == name } as? SkillEntity
        if (entity != null && binding.skillLevel.text.isNullOrEmpty()) {
            binding.skillLevel.setText(entity.skillName, false)
        } else {
            logger.warning("Skill with Name $name not found in adapter, leaving field empty")
        }
        return true
    }

    private fun setTeamById(idStr: String): Boolean {
        val adapter = binding.team.adapter ?: return false
        val entity = findInAdapter(adapter) { (it as? TeamEntity)?.teamId?.toString() == idStr } as? TeamEntity
        if (entity != null && binding.team.text.isNullOrEmpty()) {
            binding.team.setText(entity.teamName, false)
        } else {
            logger.warning("Team with ID $idStr not found in adapter, leaving field empty")
        }
        return true
    }

    private fun setJourneyById(idStr: String): Boolean {
        val adapter = binding.journey.adapter ?: return false
        val entity = findInAdapter(adapter) { (it as? JourneyEntity)?.journeyId?.toString() == idStr } as? JourneyEntity
        if (entity != null && binding.journey.text.isNullOrEmpty()) {
            binding.journey.setText(entity.journeyName, false)
        } else {
            logger.warning("Journey with ID $idStr not found in adapter, leaving field empty")
        }
        return true
    }

    // --- UI Layout Management ---
    private fun updatePufferComponents() {
        if (flatReorderMode) return
        val g = View.GONE
        val v = View.VISIBLE

        // Puffer-Sichtbarkeit
        val puffers = mapOf(
            binding.pufferBetweenTeamAndUnit to (binding.textInputLayoutProjectTimeTicket.visibility == v && binding.textInputLayoutProjectTimeTeam.visibility == v),
            binding.pufferBetweenActivityAndMatrix to (binding.textInputLayoutProjectTimeActivityType.visibility == v && binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility == v),
            binding.pufferBetweenActivityMatrixAndSkill to (binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility == v && binding.textInputLayoutProjectTimeSkillLevel.visibility == v),
            binding.pufferBetweenActivitySkillAndUnits to (binding.textInputLayoutProjectTimeSkillLevel.visibility == v && binding.textInputLayoutProjectTimeUnit.visibility == v),
            binding.pufferBetweenJourneyAndTraveltime to (binding.textInputLayoutProjectTimeJourney.visibility == v && binding.textInputLayoutProjectTimeTravelTime.visibility == v),
            binding.pufferBetweenTraveltimeAndKm to (binding.textInputLayoutProjectTimeTravelTime.visibility == v && binding.textInputLayoutProjectTimeDrivenKm.visibility == v),
            binding.pufferBetweenKmAndKmblan to (binding.textInputLayoutProjectTimeDrivenKm.visibility == v && binding.kmFlatRate.visibility == v),
            binding.bufferBetweenBillablePremiumable to (binding.billable.visibility == v && binding.premiumable.visibility == v),
            binding.bufferBetweenPlaceOfWorkAndEvaluation to (binding.textInputLayoutProjectTimePerformanceLocation.visibility == v && binding.textInputLayoutProjectTimeEvaluation.visibility == v)
        )
        puffers.forEach { (puffer, condition) ->
            puffer.visibility = if (condition) v else g
        }

        // Container-Sichtbarkeit
        val containers = mapOf(
            binding.assignmentContainer to (binding.textInputLayoutProjectTimeTicket.visibility == v || binding.textInputLayoutProjectTimeTeam.visibility == v),
            binding.activityContainer to (binding.textInputLayoutProjectTimeActivityType.visibility == v || binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility == v || binding.textInputLayoutProjectTimeSkillLevel.visibility == v || binding.textInputLayoutProjectTimeUnit.visibility == v),
            binding.billableContainer to (binding.textInputLayoutProjectTimeOrderNo.visibility == v || binding.billable.visibility == v || binding.premiumable.visibility == v),
            binding.journeyContainer to (binding.textInputLayoutProjectTimeJourney.visibility == v || binding.textInputLayoutProjectTimeTravelTime.visibility == v || binding.textInputLayoutProjectTimeDrivenKm.visibility == v || binding.kmFlatRate.visibility == v),
            binding.descriptionContainer to (binding.textInputLayoutProjectTimeDescription.visibility == v || binding.descriptionCloseButton.visibility == v),
            binding.locationEvaluationContainer to (binding.textInputLayoutProjectTimePerformanceLocation.visibility == v || binding.textInputLayoutProjectTimeEvaluation.visibility == v)
        )
        containers.forEach { (container, condition) ->
            container.visibility = if (condition) v else g
        }

        updateContainerMargins()
    }

    private fun updateContainerMargins() {
        if (flatReorderMode) return
        val standardMargin = 10.dpToPx()
        val firstContainerMargin = 0.dpToPx()
        val containers = listOf(
            binding.assignmentContainer, binding.activityContainer, binding.billableContainer,
            binding.journeyContainer, binding.descriptionContainer, binding.locationEvaluationContainer
        )
        var isFirstVisibleContainer = true
        for (container in containers) {
            if (container.visibility == View.VISIBLE) {
                val layoutParams = container.layoutParams as LinearLayout.LayoutParams
                layoutParams.topMargin = if (isFirstVisibleContainer) firstContainerMargin else standardMargin
                container.layoutParams = layoutParams
                isFirstVisibleContainer = false
            }
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun arrangeInputsBasedOnSettings(settings: ProjectTimeTrackSetting) {
        try {
            val normalized = settings.fieldOrder.trim()
            val isCustomOrder = normalized.isNotEmpty() && normalized != ProjectTimeTrackSetting.DEFAULT_FIELD_ORDER
            flatReorderMode = isCustomOrder

            val arrangement = settings.arrangeInputsBasedOnSettingsForStopwatch(isCustomerTimeTrack)
            reorderFirstPageInputs(arrangement.firstPageOrder)

            if (flatReorderMode) {
                val keysForSecondPage = (settings.fieldOrder.takeIf { it.isNotBlank() } ?: ProjectTimeTrackSetting.DEFAULT_FIELD_ORDER)
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && it !in arrangement.firstPageOrder }
                reorderSecondPageFlat(keysForSecondPage)
            } else {
                reorderSecondPageContainers(arrangement.secondPageContainerOrder)
                updateContainerMargins()
            }
        } catch (ex: Exception) {
            logger.warning("arrangeInputsBasedOnSettings failed: ${ex.message}")
        }
    }

    private fun reorderFirstPageInputs(order: List<String>) {
        val parent = binding.dropdownMenuLayout
        // Basis-Mapping für Seite 1 (Textfelder)
        val baseMapping = mutableMapOf(
            ProjectTimeTrackSetting.Keys.PROJECT to binding.textInputLayoutProjectTimeProject,
            ProjectTimeTrackSetting.Keys.TASK to binding.textInputLayoutProjectTimeTask,
            ProjectTimeTrackSetting.Keys.CUSTOMER to binding.textInputLayoutProjectTimeCustomer,
        )
        // Optional: weitere Felder im Flat-Mode zulassen
        val checkboxMap: Map<String, View> = if (flatReorderMode) {
            baseMapping.putAll(
                mapOf(
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
                    ProjectTimeTrackSetting.Keys.DESCRIPTION to binding.textInputLayoutProjectTimeDescription,
                    ProjectTimeTrackSetting.Keys.PERFORMANCE_LOCATION to binding.textInputLayoutProjectTimePerformanceLocation,
                    ProjectTimeTrackSetting.Keys.EVALUATION to binding.textInputLayoutProjectTimeEvaluation
                )
            )
            mapOf(
                ProjectTimeTrackSetting.Keys.BILLABLE to binding.billable,
                ProjectTimeTrackSetting.Keys.PREMIUMABLE to binding.premiumable,
                ProjectTimeTrackSetting.Keys.KM_FLAT_RATE to binding.kmFlatRate
            )
        } else emptyMap()

        // Hilfsfunktionen für LayoutParams, damit Elemente aus horizontalen Zeilen korrekt angezeigt werden
        fun firstPageRowLp(isFirst: Boolean): LinearLayout.LayoutParams {
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = if (isFirst) 0 else 10.dpToPx()
            lp.bottomMargin = 10.dpToPx()
            return lp
        }
        fun firstPageInlineLp(): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Zuerst alle Kandidaten (Textfelder) lösen
        for ((_, view) in baseMapping) {
            (view.parent as? ViewGroup)?.removeView(view)
        }
        // Auch Checkboxen ggf. lösen
        for ((_, cb) in checkboxMap) {
            (cb.parent as? ViewGroup)?.removeView(cb)
        }

        // Dann in gewünschter Reihenfolge (max. 3, bereits durch order begrenzt) einfügen
        var i = 0
        var isFirstAdded = true
        while (i < order.size) {
            val key = order[i]
            val cbView = checkboxMap[key]
            if (cbView != null) {
                // Checkbox(en) auf einer Zeile gruppieren
                val row = LinearLayout(requireContext())
                row.orientation = LinearLayout.HORIZONTAL
                row.layoutParams = firstPageRowLp(isFirstAdded)
                var added = false
                var j = i
                while (j < order.size) {
                    val k = order[j]
                    val v = checkboxMap[k] ?: break
                    if (v.visibility == View.GONE) { j++; continue }
                    (v.parent as? ViewGroup)?.removeView(v)
                    row.addView(v, firstPageInlineLp())
                    added = true
                    j++
                }
                if (added) {
                    parent.addView(row)
                    isFirstAdded = false
                }
                i = j
                continue
            }
            val view = baseMapping[key]
            if (view != null && view.visibility != View.GONE) {
                (view.parent as? ViewGroup)?.removeView(view)
                view.layoutParams = firstPageRowLp(isFirstAdded)
                parent.addView(view, parent.childCount)
                isFirstAdded = false
            }
            i++
        }
    }

    private fun reorderSecondPageContainers(order: List<ProjectTimeTrackSetting.ContainerId>) {
        val parent = binding.secondPageDetailLayout
        val backButton = binding.goBackToFirstPageButton
        val containerMap = mapOf(
            ProjectTimeTrackSetting.ContainerId.ASSIGNMENT to binding.assignmentContainer,
            ProjectTimeTrackSetting.ContainerId.ACTIVITY to binding.activityContainer,
            ProjectTimeTrackSetting.ContainerId.BILLABLE to binding.billableContainer,
            ProjectTimeTrackSetting.ContainerId.JOURNEY to binding.journeyContainer,
            ProjectTimeTrackSetting.ContainerId.DESCRIPTION to binding.descriptionContainer,
            ProjectTimeTrackSetting.ContainerId.LOCATION_EVALUATION to binding.locationEvaluationContainer
        )
        for (id in order) {
            val view = containerMap[id] ?: continue
            if (view.visibility == View.GONE) continue
            // detach and insert before the back button so it stays last
            (view.parent as? ViewGroup)?.removeView(view)
            val insertIndex = parent.indexOfChild(backButton).takeIf { it >= 0 } ?: parent.childCount
            parent.addView(view, insertIndex)
        }
    }

    // Flacher Aufbau der zweiten Seite ohne Container-Gruppierung
    private fun reorderSecondPageFlat(keys: List<String>) {
        val parent = binding.secondPageDetailLayout
        val backButton = binding.goBackToFirstPageButton

        // Container ausblenden, damit sie nicht doppelt erscheinen
        binding.assignmentContainer.visibility = View.GONE
        binding.activityContainer.visibility = View.GONE
        binding.billableContainer.visibility = View.GONE
        binding.journeyContainer.visibility = View.GONE
        binding.locationEvaluationContainer.visibility = View.GONE
        // Beschreibung darf als eigener Block sichtbar bleiben, wenn gewählt
        // Wir fügen aber bevorzugt das Textfeld direkt ein; der Container wird nicht benötigt
        binding.descriptionContainer.visibility = View.GONE

        // Back-Button sichern und alle Kinder entfernen
        parent.removeAllViews()

        // Mapping von Keys zu Views
        val viewMap: Map<String, View> = mapOf(
            ProjectTimeTrackSetting.Keys.CUSTOMER to binding.textInputLayoutProjectTimeCustomer,
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
            ProjectTimeTrackSetting.Keys.DESCRIPTION to binding.textInputLayoutProjectTimeDescription,
            ProjectTimeTrackSetting.Keys.PERFORMANCE_LOCATION to binding.textInputLayoutProjectTimePerformanceLocation,
            ProjectTimeTrackSetting.Keys.EVALUATION to binding.textInputLayoutProjectTimeEvaluation
        )
        val checkboxMap: Map<String, View> = mapOf(
            ProjectTimeTrackSetting.Keys.BILLABLE to binding.billable,
            ProjectTimeTrackSetting.Keys.PREMIUMABLE to binding.premiumable,
            ProjectTimeTrackSetting.Keys.KM_FLAT_RATE to binding.kmFlatRate
        )

        fun lpRow(first: Boolean): LinearLayout.LayoutParams {
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = if (first) 0 else 10.dpToPx()
            return p
        }
        fun lpWrap(): LinearLayout.LayoutParams {
            return LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        var first = true
        var i = 0
        while (i < keys.size) {
            val key = keys[i]
            // Überspringe ggf. unsichtbare Elemente
            val cb = checkboxMap[key]
            if (cb != null) {
                // Starte eine Checkbox-Zeile und sammle alle aufeinanderfolgenden Checkbox-Keys
                val row = LinearLayout(requireContext())
                row.orientation = LinearLayout.HORIZONTAL
                row.layoutParams = lpRow(first)
                var added = false
                var j = i
                while (j < keys.size) {
                    val k = keys[j]
                    val v = checkboxMap[k] ?: break
                    if (v.visibility == View.GONE) { j++; continue }
                    // Aus altem Parent lösen und hinzufügen
                    (v.parent as? ViewGroup)?.removeView(v)
                    row.addView(v, lpWrap())
                    added = true
                    j++
                }
                if (added) {
                    parent.addView(row)
                    first = false
                }
                i = j
                continue
            }
            val view = viewMap[key]
            if (view == null) { i++; continue }
            if (view.visibility == View.GONE) { i++; continue }
            // Lösen und als volle Zeile einfügen
            (view.parent as? ViewGroup)?.removeView(view)
            val params = lpRow(first)
            view.layoutParams = params
            parent.addView(view)
            first = false
            i++
        }
    }

    private fun startProjectTime() {
        if (!isValid(true)) return

        val data = HashMap<String, String>().apply {
            val date = Date()
            put("userId", userId.toString())
            put("date", android.text.format.DateFormat.format("yyyy-MM-dd", date).toString())
            put("from", android.text.format.DateFormat.format("HH:mm", date).toString())
            put("description", binding.description.text.toString())
            put("orderNo", binding.orderNo.text.toString())
            put("performanceLocation", binding.performanceLocation.text.toString())
            put("travelTime", binding.travelTime.text.toString())
            put("drivenKm", binding.drivenKm.text.toString())
            put("units", binding.unit.text.toString())
            put("evaluation", binding.evaluation.text.toString())
            put("kmFlatRate", if (binding.kmFlatRate.isChecked) "true" else "false")
            put("billable", if (binding.billable.isChecked) "true" else "false")
            put("premium", if (binding.premiumable.isChecked) "true" else "false")
            put("timeEntryType", "10")

            // Adapter-based IDs
            (binding.customer.adapter as? CustomerEntityAdapter)?.getItemByName(binding.customer.text.toString())?.id?.let { put("customerId", it) }
            (binding.ticket.adapter as? TicketEntityAdaptor)?.getItemByName(binding.ticket.text.toString())?.ticketId?.let { put("ticketId", it.toString()) }
            (binding.project.adapter as? ProjectEntityAdaptor)?.getItemByName(binding.project.text.toString())?.projectId?.let { put("projectId", it.toString()) }
            (binding.task.adapter as? TaskEntityAdaptor)?.getItemByName(binding.task.text.toString())?.taskId?.let { put("taskId", it.toString()) }
            (binding.activityType.adapter as? ActivityTypeEntityAdaptor)?.getItemByName(binding.activityType.text.toString())?.activityTypeId?.let { put("activityType", it.toString()) }
            (binding.activityTypeMatrix.adapter as? ActivityTypeMatrixEntityAdaptor)?.getItemByName(binding.activityTypeMatrix.text.toString())?.activityTypeMatrixId?.let { put("activityTypeMatrix", it.toString()) }
            (binding.skillLevel.adapter as? SkillEntityAdaptor)?.getItemByName(binding.skillLevel.text.toString())?.skillId?.let { put("skillLevel", it.toString()) }
            (binding.team.adapter as? TeamEntityAdaptor)?.getItemByName(binding.team.text.toString())?.teamId?.let { put("teamId", it.toString()) }
            (binding.journey.adapter as? JourneyEntityAdaptor)?.getItemByName(binding.journey.text.toString())?.journeyId?.let { put("journeyId", it.toString()) }
        }

        viewModel.startWorkingTime(data, requireContext())
    }

    private fun stopProjectTime() {
        if (!isValid(false)) return
        viewModel.stopLatestOpenWorkingTime(requireContext())
    }

    // --- Runnable ---
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val currentTime = timeFormat.format(Date())
            val currentDate = dateFormat.format(Date())
            binding.dateView.text = currentDate
            binding.timeView.text = currentTime
            handler.postDelayed(this, 1000)
        }
    }
}
