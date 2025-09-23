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
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentProjectStopwatchBinding
import com.timo.timoterminal.entityAdaptor.*
import com.timo.timoterminal.entityClasses.*
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.ProjectFragmentViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import kotlin.getValue

// --- Konstanten ---
private const val ARG_USERID = "userId"
private const val ARG_CUSTOMER_TIME_TRACK = "customerTimeTrack"
private const val ARG_TIME_ENTRY_TYPE = "timeEntryType"
private const val ARG_CROSS_DAY = "crossDay"

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

    // --- Felder ---
    private val logger = Logger.getLogger(ProjectFragmentStopwatch::class.java.name)
    private val languageService: LanguageService by inject()
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var userId: Long? = null
    private var isCustomerTimeTrack: Boolean = false
    private var timeEntryType: Long? = null
    private var crossDay: Boolean = false
    private var isEntry: Boolean = true
    private lateinit var binding: FragmentProjectStopwatchBinding
    private val viewModel: ProjectFragmentViewModel by inject()

    // --- Lifecycle ---
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
        binding = FragmentProjectStopwatchBinding.inflate(inflater, container, false)
        setUp() // Initialisiere UI und Listener
        isEntry = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler.post(updateTimeRunnable) // Starte Zeit-Update
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadForProjectTimeTrack() // Lade Daten neu
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateTimeRunnable) // Stoppe Zeit-Update
    }

    // --- Companion Object: Factory-Methode ---
    companion object {
        /**
         * Erzeugt eine konfigurierte Instanz dieses Fragments.
         */
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

    // --- UI: Zeit/Datum aktualisieren ---
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val currentTime = timeFormat.format(Date())
            val currentDate = dateFormat.format(Date())
            binding.dateView.text = currentDate
            binding.timeView.text = currentTime
            handler.postDelayed(this, 1000)
        }
    }

    // --- UI-Setup und Listener ---
    /**
     * Initialisiert die View, Listener und Sprache.
     */
    private fun setUp() {
        // Session am Leben halten
        binding.fragmentProjectRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        // Animationsobjekte
        val fadeOutSlideOutLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_slide_out_left)
        val fadeInSlideInRight = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_slide_in_right)
        val fadeInSlideInLeft = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_slide_in_left)
        val fadeOutSlideOutRight = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_slide_out_right)

        // Seitenwechsel-Animationen
        binding.goToSecondPageButton.setOnClickListener {
            fadeOutSlideOutLeft.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    binding.firstPageDetailLayout.visibility = View.INVISIBLE
                    binding.secondPageDetailLayout.visibility = View.VISIBLE
                    binding.secondPageDetailLayout.startAnimation(fadeInSlideInRight)
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            binding.firstPageDetailLayout.startAnimation(fadeOutSlideOutLeft)
        }
        binding.goBackToFirstPageButton.setOnClickListener {
            fadeOutSlideOutRight.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    binding.secondPageDetailLayout.visibility = View.INVISIBLE
                    binding.firstPageDetailLayout.visibility = View.VISIBLE
                    binding.firstPageDetailLayout.startAnimation(fadeInSlideInLeft)
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            binding.secondPageDetailLayout.startAnimation(fadeOutSlideOutRight)
        }
        // Schließen-Button
        binding.fabClose.setOnClickListener { parentFragmentManager.popBackStack() }
        // Zeitpicker für Reisezeit
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
        // Beschreibung schließen
        binding.descriptionCloseButton.setOnClickListener {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.description.windowToken, 0)
        }
        setUpOnItemClickListeners()
        setLanguageTexts()
        observeLiveData()
        viewLifecycleOwner.lifecycleScope.launch {
            hideNonVisibleItems()
        }
        addTextWatcher()
        placeCustomerAtTopIfNeeded()
    }

    // --- Berechtigungsabfrage ---
    private suspend fun perm(name: String): Boolean {
        return viewModel.permission(name) == "true"
    }

    // --- UI: Felder nach Berechtigung ausblenden ---
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
        // ... Logik zum Ausblenden der Felder ...
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

    // --- UI: Felder nach Server-Settings ausblenden ---
    private fun processProjectTimeTrackSetting(it: ProjectTimeTrackSetting) {
        val g = View.GONE
        // ... Logik zum Ausblenden nach Settings ...
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

    // --- UI: Autocomplete-Listener ---
    private fun setUpOnItemClickListeners() {
        val v = View.VISIBLE
        binding.project.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            val selectedItem = parent.getItemAtPosition(position) as ProjectEntity
            viewModel.showFilteredTasksForProject(selectedItem.projectId)
        }
        binding.task.setOnItemClickListener { parent, _, position, _ ->
            defOnItemClickListener()
            val selectedItem = parent.getItemAtPosition(position) as TaskEntity
            // ... Logik für Billable, Description, ActivityType, OrderNo ...
            if (selectedItem.abrechenbarPBaum != 3L) {
                binding.billable.isChecked = selectedItem.abrechenbarPBaum == 1L
            }
            binding.billable.isEnabled = !selectedItem.abrechenbarDisabled
            if(binding.textInputLayoutProjectTimeDescription.visibility == v) {
                var hint = binding.textInputLayoutProjectTimeDescription.hint
                if (!hint.isNullOrEmpty()) {
                    if (hint.last() == '*') hint = hint.dropLast(1)
                    if (selectedItem.descriptionMust) hint = "${hint}*"
                }
                binding.textInputLayoutProjectTimeDescription.hint = hint
            }
            if (binding.textInputLayoutProjectTimeActivityType.visibility == v) {
                var hint = binding.textInputLayoutProjectTimeActivityType.hint
                if (!hint.isNullOrEmpty()) {
                    if (hint.last() == '*') hint = hint.dropLast(1)
                    if(selectedItem.activityTypeMust) hint = "${hint}*"
                }
                binding.textInputLayoutProjectTimeActivityType.hint = hint
            }
            if (binding.textInputLayoutProjectTimeOrderNo.visibility == v) {
                var hint = binding.textInputLayoutProjectTimeOrderNo.hint
                if (!hint.isNullOrEmpty()) {
                    if (hint.last() == '*') hint = hint.dropLast(1)
                    if (selectedItem.orderNoMust) hint = "${hint}*"
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

    // --- Hilfsmethode: Tastatur schließen und Session-Timer ---
    private fun defOnItemClickListener() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.project.windowToken, 0)
        (activity as MainActivity?)?.restartTimer()
    }

    // --- LiveData-Observer ---
    private fun observeLiveData() {
        // ... LiveData-Logik, Masken, Listen, Settings, Fehler ...
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
                viewLifecycleOwner.lifecycleScope.launch { hideNonVisibleItems() }
                viewModel.liveProjectTimeTrackSetting.value = null
            }
        }
        viewModel.liveActivityTypeEntities.value = emptyList()
        viewModel.liveActivityTypeEntities.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adapter = ActivityTypeEntityAdaptor(requireContext(), R.layout.dropdown_small, it)
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

    // --- Validierung und Speichern ---
    /**
     * Sammelt und validiert die Nutzereingaben und speichert sie über das ViewModel.
     */
    private fun saveData() {
        val data = HashMap<String, String>()
        if(!isValid()) return
        data["userId"] = userId.toString()
        // ... Daten auslesen ...
        data["description"] = binding.description.text.toString()
        val cAdapter = binding.customer.adapter as? CustomerEntityAdapter
        data["customerId"] = cAdapter?.getItemByName(binding.customer.text.toString())?.id.toString()
        val tiAdaptor = binding.ticket.adapter as? TicketEntityAdaptor
        data["ticketId"] = tiAdaptor?.getItemByName(binding.ticket.text.toString())?.ticketId.toString()
        val pAdapter = binding.project.adapter as? ProjectEntityAdaptor
        data["projectId"] = pAdapter?.getItemByName(binding.project.text.toString())?.projectId.toString()
        val tAdapter = binding.task.adapter as? TaskEntityAdaptor
        data["taskId"] = tAdapter?.getItemByName(binding.task.text.toString())?.taskId.toString()
        data["orderNo"] = binding.orderNo.text.toString()
        val atAdapter = binding.activityType.adapter as? ActivityTypeEntityAdaptor
        data["activityType"] = atAdapter?.getItemByName(binding.activityType.text.toString())?.activityTypeId.toString()
        val atmAdapter = binding.activityTypeMatrix.adapter as? ActivityTypeMatrixEntityAdaptor
        data["activityTypeMatrix"] = atmAdapter?.getItemByName(binding.activityTypeMatrix.text.toString())?.activityTypeMatrixId.toString()
        val sAdapter = binding.skillLevel.adapter as? SkillEntityAdaptor
        data["skillLevel"] = sAdapter?.getItemByName(binding.skillLevel.text.toString())?.skillId.toString()
        data["performanceLocation"] = binding.performanceLocation.text.toString()
        val teamAdapter = binding.team.adapter as? TeamEntityAdaptor
        data["teamId"] = teamAdapter?.getItemByName(binding.team.text.toString())?.teamId.toString()
        val jAdapter = binding.journey.adapter as? JourneyEntityAdaptor
        data["journeyId"] = jAdapter?.getItemByName(binding.journey.text.toString())?.journeyId.toString()
        data["travelTime"] = binding.travelTime.text.toString()
        data["drivenKm"] = binding.drivenKm.text.toString()
        data["kmFlatRate"] = if (binding.kmFlatRate.isChecked) "1" else "0"
        data["billable"] = if (binding.billable.isChecked) "1" else "0"
        data["premium"] = if (binding.premiumable.isChecked) "1" else "0"
        data["units"] = binding.unit.text.toString()
        data["evaluation"] = binding.evaluation.text.toString()
        viewModel.saveProjectTime(data, requireContext())
    }

    /**
     * Prüft die Pflichtfelder und zeigt Fehler an.
     */
    private fun isValid(): Boolean {
        val v = View.VISIBLE
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
        if(binding.textInputLayoutProjectTimeDescription.visibility == v) {
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
                if(binding.activityType.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeActivityType.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeActivityType.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimeOrderNo.visibility == v) {
            if (binding.textInputLayoutProjectTimeOrderNo.hint?.last() == '*') {
                if(binding.orderNo.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeOrderNo.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeOrderNo.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimeUnit.visibility == v) {
            if (binding.textInputLayoutProjectTimeUnit.hint?.last() == '*') {
                if(binding.unit.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeUnit.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeUnit.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimeCustomer.visibility == v) {
            if (binding.textInputLayoutProjectTimeCustomer.hint?.last() == '*') {
                if(binding.customer.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeCustomer.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeCustomer.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimeSkillLevel.visibility == v) {
            if (binding.textInputLayoutProjectTimeSkillLevel.hint?.last() == '*') {
                if(binding.skillLevel.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimeSkillLevel.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimeSkillLevel.error = null
                }
            }
        }
        if (binding.textInputLayoutProjectTimePerformanceLocation.visibility == v) {
            if (binding.textInputLayoutProjectTimePerformanceLocation.hint?.last() == '*') {
                if(binding.performanceLocation.text.isNullOrEmpty()) {
                    error = true
                    binding.textInputLayoutProjectTimePerformanceLocation.error = "Required"
                } else {
                    binding.textInputLayoutProjectTimePerformanceLocation.error = null
                }
            }
        }
        return !error
    }

    // --- TextWatcher für Timer-Reset ---
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

    // --- UI: Puffer und Container-Logik ---
    /**
     * Berechnet die Sichtbarkeit der Puffer und Container.
     */
    private fun updatePufferComponents() {
        val g = View.GONE
        val v = View.VISIBLE
        // ... Logik für Puffer zwischen Komponenten ...
        binding.pufferBetweenTeamAndUnit.visibility =
            if (binding.textInputLayoutProjectTimeTicket.visibility == v && binding.textInputLayoutProjectTimeTeam.visibility == v) v else g
        binding.pufferBetweenActivityAndMatrix.visibility =
            if (binding.textInputLayoutProjectTimeActivityType.visibility == v && binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility == v) v else g
        binding.pufferBetweenActivityMatrixAndSkill.visibility =
            if (binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility == v && binding.textInputLayoutProjectTimeSkillLevel.visibility == v) v else g
        binding.pufferBetweenActivitySkillAndUnits.visibility =
            if (binding.textInputLayoutProjectTimeSkillLevel.visibility == v && binding.textInputLayoutProjectTimeUnit.visibility == v) v else g
        binding.pufferBetweenJourneyAndTraveltime.visibility =
            if (binding.textInputLayoutProjectTimeJourney.visibility == v && binding.textInputLayoutProjectTimeTravelTime.visibility == v) v else g
        binding.pufferBetweenTraveltimeAndKm.visibility =
            if (binding.textInputLayoutProjectTimeTravelTime.visibility == v && binding.textInputLayoutProjectTimeDrivenKm.visibility == v) v else g
        binding.pufferBetweenKmAndKmblan.visibility =
            if (binding.textInputLayoutProjectTimeDrivenKm.visibility == v && binding.kmFlatRate.visibility == v) v else g
        binding.bufferBetweenBillablePremiumable.visibility =
            if (binding.billable.visibility == v && binding.premiumable.visibility == v) v else g
        binding.bufferBetweenPlaceOfWorkAndEvaluation.visibility =
            if (binding.textInputLayoutProjectTimePerformanceLocation.visibility == v && binding.textInputLayoutProjectTimeEvaluation.visibility == v) v else g
        // Container ausblenden, wenn alle Komponenten unsichtbar sind
        val assignmentVisible = binding.textInputLayoutProjectTimeTicket.visibility == v || binding.textInputLayoutProjectTimeTeam.visibility == v
        binding.assignmentContainer.visibility = if (assignmentVisible) v else g
        val activityVisible = binding.textInputLayoutProjectTimeActivityType.visibility == v || binding.textInputLayoutProjectTimeActivityTypeMatrix.visibility == v || binding.textInputLayoutProjectTimeSkillLevel.visibility == v || binding.textInputLayoutProjectTimeUnit.visibility == v
        binding.activityContainer.visibility = if (activityVisible) v else g
        val billableVisible = binding.textInputLayoutProjectTimeOrderNo.visibility == v || binding.billable.visibility == v || binding.premiumable.visibility == v
        binding.billableContainer.visibility = if (billableVisible) v else g
        val journeyVisible = binding.textInputLayoutProjectTimeJourney.visibility == v || binding.textInputLayoutProjectTimeTravelTime.visibility == v || binding.textInputLayoutProjectTimeDrivenKm.visibility == v || binding.kmFlatRate.visibility == v
        binding.journeyContainer.visibility = if (journeyVisible) v else g
        val descriptionVisible = binding.textInputLayoutProjectTimeDescription.visibility == v || binding.descriptionCloseButton.visibility == v
        binding.descriptionContainer.visibility = if (descriptionVisible) v else g
        val locationEvaluationVisible = binding.textInputLayoutProjectTimePerformanceLocation.visibility == v || binding.textInputLayoutProjectTimeEvaluation.visibility == v
        binding.locationEvaluationContainer.visibility = if (locationEvaluationVisible) v else g
        updateContainerMargins()
    }

    /**
     * Setzt die oberen Abstände der Container dynamisch.
     */
    private fun updateContainerMargins() {
        val standardMargin = 10.dpToPx()
        val firstContainerMargin = 0.dpToPx()
        val containers = listOf(
            binding.assignmentContainer,
            binding.activityContainer,
            binding.billableContainer,
            binding.journeyContainer,
            binding.descriptionContainer,
            binding.locationEvaluationContainer
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

    // --- Hilfsmethode: dp zu px ---
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun placeCustomerAtTopIfNeeded() {
        if (!isCustomerTimeTrack) return
        val customerLayout = binding.textInputLayoutProjectTimeCustomer
        val parent = customerLayout.parent as? LinearLayout ?: return
        // set marginbottom to 5dp
        val layoutParams = customerLayout.layoutParams as LinearLayout.LayoutParams
        layoutParams.bottomMargin = 10.dpToPx()
        customerLayout.layoutParams = layoutParams
        // Move customerLayout to the top if it's not already there
        if (parent.indexOfChild(customerLayout) != 0) {
            parent.removeView(customerLayout)
            parent.addView(customerLayout, 0)
        }
    }
}
