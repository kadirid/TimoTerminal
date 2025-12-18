package com.timo.timoterminal.fragmentViews

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentAbsenceFormBinding
import com.timo.timoterminal.entityAdaptor.AbsenceDeputyEntityAdapter
import com.timo.timoterminal.entityAdaptor.AbsenceTypeMatrixEntityAdapter
import com.timo.timoterminal.entityClasses.AbsenceEntryEntity
import com.timo.timoterminal.entityClasses.AbsenceTypeEntity
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.modalBottomSheets.MBFragmentProjectErrorSheet
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.CustomDatePicker
import com.timo.timoterminal.utils.classes.CustomTimePicker
import com.timo.timoterminal.viewModel.AbsenceEditViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.util.Calendar

private const val ARG_USERID = "userId"
private const val ARG_EDITOR_ID = "editorId"
private const val ARG_ABSENCE_TYPE_ID = "absenceTypeId"
private const val ARG_ENTITY = "entity"

class AbsenceFormFragment : Fragment() {
    private val languageService: LanguageService by inject()
    private var userId: Long? = null
    private var editorId: String? = null
    private var absenceTypeId: Long? = null
    private var entity: AbsenceEntryEntity? = null
    private var firstM: Boolean = true
    private var firstD: Boolean = true
    private var checked: Boolean = false

    private lateinit var binding: FragmentAbsenceFormBinding

    private val viewModel: AbsenceEditViewModel by inject()

    private var darkMode: Boolean = false
    private var user: UserEntity? = null
    private var absenceEntity: AbsenceTypeEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USERID)
            editorId = it.getString(ARG_EDITOR_ID)
            absenceTypeId = it.getLong(ARG_ABSENCE_TYPE_ID)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                entity = it.getParcelable(ARG_ENTITY, AbsenceEntryEntity::class.java)
            } else {
                @Suppress("DEPRECATION")
                entity = it.getParcelable(ARG_ENTITY)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAbsenceFormBinding.inflate(inflater, container, false)

        binding.fromDate.setText(Utils.getDateFromGC(Utils.getCal()))
        viewModel.absenceTypeId = absenceTypeId ?: -1L
        viewModel.userId = userId ?: -1L

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        darkMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        setUp()
    }

    override fun onPause() {
        super.onPause()
        viewModel.liveHideMask.postValue(true)
    }

    private fun setUp() {
        binding.fragmentAbsenceFormRoot.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }

        binding.fromDate.setOnClickListener {
            val value = binding.fromDate.text.toString().split(".")
            val greg = Utils.getCal()
            val datePickerDialog = CustomDatePicker.newInstance(
                { _, year, month, dayOfMonth ->
                    val selectedDate = "%02d.%02d.%04d".format(dayOfMonth, month + 1, year)
                    binding.fromDate.setText(selectedDate)
                },
                if(value.size > 2) value[2].toInt() else greg.get(Calendar.YEAR),
                if(value.size > 2) value[1].toInt() - 1 else greg.get(Calendar.MONTH),
                if(value.size > 2) value[0].toInt() else greg.get(Calendar.DAY_OF_MONTH)
            )
            (activity as MainActivity?)?.restartTimer()
            datePickerDialog.isThemeDark = darkMode
            datePickerDialog.show(parentFragmentManager, "DatePickerDialog")
        }

        binding.toDate.setOnClickListener {
            val value = binding.toDate.text.toString().split(".")
            val greg = Utils.getCal()
            val datePickerDialog = CustomDatePicker.newInstance(
                { _, year, month, dayOfMonth ->
                    val selectedDate = "%02d.%02d.%04d".format(dayOfMonth, month + 1, year)
                    binding.toDate.setText(selectedDate)
                },
                if(value.size > 2) value[2].toInt() else greg.get(Calendar.YEAR),
                if(value.size > 2) value[1].toInt() - 1 else greg.get(Calendar.MONTH),
                if(value.size > 2) value[0].toInt() else greg.get(Calendar.DAY_OF_MONTH)
            )
            (activity as MainActivity?)?.restartTimer()
            datePickerDialog.isThemeDark = darkMode
            datePickerDialog.show(parentFragmentManager, "DatePickerDialog")
        }

        binding.from.setOnClickListener {
            if (checked) return@setOnClickListener
            val value = binding.from.text.toString().split(":")
            val greg = Utils.getCal()
            val timePickerDialog = CustomTimePicker.newInstance(
                { _, hourOfDay, minute, _ ->
                    val selectedTime = "%02d:%02d".format(hourOfDay, minute)
                    binding.from.setText(selectedTime)
                },
                if(value.size > 1) value[0].toInt() else greg.get(Calendar.HOUR_OF_DAY),
                if(value.size > 1) value[1].toInt() else greg.get(Calendar.MINUTE),
                true
            )
            (activity as MainActivity?)?.restartTimer()
            timePickerDialog.isThemeDark = darkMode
            timePickerDialog.show(parentFragmentManager, "TimePickerDialog")
        }

        binding.to.setOnClickListener {
            if (checked) return@setOnClickListener
            val value = binding.to.text.toString().split(":")
            val greg = Utils.getCal()
            val timePickerDialog = CustomTimePicker.newInstance(
                { _, hourOfDay, minute, _ ->
                    val selectedTime = "%02d:%02d".format(hourOfDay, minute)
                    binding.to.setText(selectedTime)
                },
                if(value.size > 1) value[0].toInt() else greg.get(Calendar.HOUR_OF_DAY),
                if(value.size > 1) value[1].toInt() else greg.get(Calendar.MINUTE),
                true
            )
            (activity as MainActivity?)?.restartTimer()
            timePickerDialog.isThemeDark = darkMode
            timePickerDialog.show(parentFragmentManager, "TimePickerDialog")
        }

        binding.hours.setOnClickListener {
            if (checked) return@setOnClickListener
            val value = binding.hours.text.toString().split(":")
            val greg = Utils.getCal()
            val timePickerDialog = CustomTimePicker.newInstance(
                { _, hourOfDay, minute, _ ->
                    val selectedTime = "%02d:%02d".format(hourOfDay, minute)
                    binding.hours.setText(selectedTime)
                },
                if(value.size > 1) value[0].toInt() else greg.get(Calendar.HOUR_OF_DAY),
                if(value.size > 1) value[1].toInt() else greg.get(Calendar.MINUTE),
                true
            )
            (activity as MainActivity?)?.restartTimer()
            timePickerDialog.isThemeDark = darkMode
            timePickerDialog.show(parentFragmentManager, "TimePickerDialog")
        }

        binding.saveAbsenceButton.setOnClickListener {
            saveData()
        }

        binding.descriptionCloseButton.setOnClickListener {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.description.windowToken, 0)
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setUpOnItemClickListeners()

        setLanguageTexts()

        observeLiveData()
        addTextWatcher()

        if (entity != null) {
            loadValuesFromEntity()
        }

        viewModel.loadAbsenceValuesForEdit()
    }

    private fun loadValuesFromEntity() {
        binding.fromDate.setText(
            if (entity?.date.isNullOrEmpty()) "" else Utils.getDateFromGC(
                Utils.parseDateFromTransfer(entity?.date)
            )
        )
        binding.toDate.setText(
            if (entity?.dateTo.isNullOrEmpty()) "" else Utils.getDateFromGC(
                Utils.parseDateFromTransfer(entity?.dateTo)
            )
        )
        binding.from.setText(entity?.from ?: "")
        binding.to.setText(entity?.to ?: "")
        binding.hours.setText(entity?.hours ?: "")
        binding.description.setText(entity?.description ?: "")
        binding.checkHalfDay.isChecked = entity?.halfDay == "1"
        binding.checkFullDay.isChecked = entity?.fullDay == "1"

        if(!entity?.message.isNullOrEmpty()) {
            val sheet = MBFragmentProjectErrorSheet()
            val bundle = Bundle()
            bundle.putString("message", entity!!.message)
            sheet.arguments = bundle
            sheet.show(parentFragmentManager, MBFragmentProjectErrorSheet.TAG)
        }
    }

    private fun defOnItemClickListener() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.matrix.windowToken, 0)
        (activity as MainActivity?)?.restartTimer()
    }

    private fun setUpOnItemClickListeners() {
        binding.deputy.setOnItemClickListener { _, _, _, _ ->
            defOnItemClickListener()
        }
        binding.matrix.setOnItemClickListener { _, _, _, _ ->
            defOnItemClickListener()
        }
        binding.checkFullDay.setOnCheckedChangeListener { _, checked ->
            (activity as MainActivity?)?.restartTimer()
            binding.textInputLayoutAbsenceEditHours
            this.checked = checked
            if (checked) {
                val empty = ""
                binding.from.setText(empty)
                binding.to.setText(empty)
                binding.hours.setText(empty)
            }
            binding.textInputLayoutAbsenceEditHours.isEnabled = !checked
            binding.textInputLayoutAbsenceEditFrom.isEnabled = !checked
            binding.textInputLayoutAbsenceEditTo.isEnabled = !checked
        }
        binding.checkHalfDay.setOnCheckedChangeListener { _, _ ->
            (activity as MainActivity?)?.restartTimer()
        }
    }

    private fun setLanguageTexts() {
        binding.textInputLayoutAbsenceEditFromDate.hint = languageService.getText("#Date from")
        binding.textInputLayoutAbsenceEditToDate.hint = languageService.getText("#Date to")
        binding.textInputLayoutAbsenceEditFrom.hint = languageService.getText("#Time from")
        binding.textInputLayoutAbsenceEditTo.hint = languageService.getText("#Time to")

        binding.textInputLayoutAbsenceEditMatrix.hint = languageService.getText("#Matrix")
        binding.textInputLayoutAbsenceEditDeputy.hint = languageService.getText("#Deputy")
        binding.textInputLayoutAbsenceEditDescription.hint =
            languageService.getText("ALLGEMEIN#Beschreibung")
        binding.checkFullDay.text = languageService.getText("#Ganzer Tag")
        binding.checkHalfDay.text = languageService.getText("Urlaubsverwaltung#Halber Tag")
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
        viewModel.liveAbsenceType.value = null
        viewModel.liveAbsenceType.observe(viewLifecycleOwner) {
            if (it != null) {
                absenceEntity = it
                processUserAndAbsence()
            }
        }
        viewModel.liveAbsenceTypeMatrix.value = emptyList()
        viewModel.liveAbsenceTypeMatrix.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.textInputLayoutAbsenceEditHours.visibility = View.GONE
                binding.checkFullDay.visibility = View.GONE
                binding.checkHalfDay.visibility = View.GONE
                binding.linearLayoutTimeFromTo.visibility = View.GONE

                binding.textInputLayoutAbsenceEditMatrix.visibility = View.VISIBLE
                val adaptor = AbsenceTypeMatrixEntityAdapter(
                    requireContext(),
                    R.layout.dropdown_small,
                    it
                )
                binding.matrix.setAdapter(adaptor)
                if(entity != null && firstM){
                    firstM = false
                    val matrixEntity = it.find { m -> m.id.toString() == entity?.matrix }
                    if(matrixEntity != null) {
                        binding.matrix.setText(matrixEntity.name, false)
                    }
                }
            }else {
                binding.textInputLayoutAbsenceEditMatrix.visibility = View.GONE
            }
        }
        viewModel.liveAbsenceDeputies.value = emptyList()
        viewModel.liveAbsenceDeputies.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val adaptor = AbsenceDeputyEntityAdapter(
                    requireContext(),
                    R.layout.dropdown_small,
                    it[0].deputies
                )
                binding.deputy.setAdapter(adaptor)
                if (entity != null && firstD) {
                    firstD = false
                    val deputyEntity: JSONObject? = it[0].deputies.let { array ->
                        var c = 0
                        while (c < array.length()) {
                            if (array.getJSONObject(c).optLong("id", -1L).toString() == entity?.deputy) {
                                return@let array.getJSONObject(c)
                            }
                            c++
                        }
                        null
                    }
                    if (deputyEntity != null) {
                        binding.deputy.setText(deputyEntity.optString("name", ""), false)
                    }
                }
            }
        }
        viewModel.liveMessage.value = ""
        viewModel.liveMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                requireActivity().runOnUiThread {
                    if (!it.startsWith('e')) {
                        Utils.showMessage(parentFragmentManager, it)
                    } else
                        Utils.showErrorMessage(requireContext(), it.substring(1))
                }
                viewModel.liveMessage.value = ""
            }
        }
        viewModel.liveRequireDeputy.value = false
        viewModel.liveRequireDeputy.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.textInputLayoutAbsenceEditDeputy.hint = "* " +
                    languageService.getText("#Deputy")
            }
        }
        viewModel.liveUser.value = null
        viewModel.liveUser.observe(viewLifecycleOwner) {
            if (it != null) {
                user = it
                processUserAndAbsence()
            }
        }
    }

    private fun processUserAndAbsence() {
        if (absenceEntity != null && user != null) {
            binding.textViewAbsenceName.text = absenceEntity!!.name

            if (user!!.timeEntryType == 1L || user!!.timeEntryType == 10L) {
                binding.textInputLayoutAbsenceEditHours.visibility = View.GONE
                binding.checkFullDay.visibility = View.VISIBLE
                binding.checkHalfDay.visibility = View.GONE
                binding.linearLayoutTimeFromTo.visibility = View.VISIBLE
            } else {
                binding.textInputLayoutAbsenceEditHours.visibility = View.VISIBLE
                binding.checkFullDay.visibility = View.VISIBLE
                binding.checkHalfDay.visibility = View.GONE
                binding.linearLayoutTimeFromTo.visibility = View.GONE
            }

            if (absenceEntity!!.subjectToApproval) {
                binding.saveAbsenceButton.text =
                    languageService.getText("URLAUB#beantragen")
            }

            if (absenceEntity!!.fromTo) {
                binding.textInputLayoutAbsenceEditHours.visibility = View.GONE
                binding.checkFullDay.visibility = View.VISIBLE
                binding.checkHalfDay.visibility = View.GONE
                binding.linearLayoutTimeFromTo.visibility = View.VISIBLE
            }
            if (absenceEntity!!.hours) {
                binding.textInputLayoutAbsenceEditHours.visibility = View.VISIBLE
                binding.checkFullDay.visibility = View.VISIBLE
                binding.checkHalfDay.visibility = View.GONE
                binding.linearLayoutTimeFromTo.visibility = View.GONE
            }

            if (absenceEntity!!.id == 2L || absenceEntity!!.id == 5L) {// Vacation or special vacation
                checkVacationVisibility()
            }
        }
    }

    private fun addTextWatcher() {
        val timerRestartTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                (activity as MainActivity?)?.restartTimer()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.description.addTextChangedListener(timerRestartTextWatcher)
    }

    private fun saveData() {
        if (!isValid()) {
            Utils.showErrorMessage(
                requireContext(),
                languageService.getText("ALLGEMEIN#Rote Felder korrigieren")
            )
            return
        }
        val data = HashMap<String, String?>()

        if(entity != null && entity?.id != null){
            data["id"] = entity?.id.toString()
        }

        data["userId"] = userId.toString()
        data["absenceTypeId"] = absenceTypeId.toString()
        data["date"] = binding.fromDate.text.toString()
        data["dateTo"] = binding.toDate.text.toString()
        data["from"] = binding.from.text.toString()
        data["to"] = binding.to.text.toString()
        data["hours"] = binding.hours.text.toString()
        data["description"] = binding.description.text.toString()
        val deputyEntityAdapter = binding.deputy.adapter as AbsenceDeputyEntityAdapter?
        data["deputy"] = deputyEntityAdapter?.getItemByName(binding.deputy.text.toString())
            ?.optLong("id", -1L)?.toString() ?: "-1"
        val matrixEntityAdapter = binding.matrix.adapter as AbsenceTypeMatrixEntityAdapter?
        data["matrix"] = matrixEntityAdapter?.getItemByName(binding.matrix.text.toString())
            ?.id?.toString() ?: "-1"
        data["halfDay"] = if (binding.checkHalfDay.isChecked) "1" else "0"
        data["fullDay"] = if (binding.checkFullDay.isChecked) "1" else "0"
        data["editorId"] = editorId ?: userId.toString()

        viewModel.saveAbsenceEntry(data, requireContext())
    }

    private fun isValid(): Boolean {
        val errorTxt = languageService.getText("ALLGEMEIN#Rote Felder korrigieren")
        var error = false
        if (binding.linearLayoutDate.visibility == View.VISIBLE) {
            if (binding.fromDate.text.isNullOrEmpty()) {
                binding.textInputLayoutAbsenceEditFromDate.error = errorTxt
                error = true
            } else {
                binding.textInputLayoutAbsenceEditFromDate.error = null
            }
            if (binding.toDate.text.isNullOrEmpty()) {
                binding.textInputLayoutAbsenceEditToDate.error = errorTxt
                error = true
            } else {
                binding.textInputLayoutAbsenceEditToDate.error = null
            }
        }

        if (binding.textInputLayoutAbsenceEditMatrix.visibility == View.VISIBLE) {
            if (binding.matrix.text.isNullOrEmpty()) {
                binding.textInputLayoutAbsenceEditMatrix.error = errorTxt
                error = true
            } else {
                binding.textInputLayoutAbsenceEditMatrix.error = null
            }
        }
        if (binding.linearLayoutTimeFromTo.visibility == View.VISIBLE) {
            if (binding.from.text.isNullOrEmpty() && !binding.checkFullDay.isChecked) {
                binding.textInputLayoutAbsenceEditFrom.error = errorTxt
                error = true
            } else {
                binding.textInputLayoutAbsenceEditFrom.error = null
            }
            if (binding.to.text.isNullOrEmpty() && !binding.checkFullDay.isChecked) {
                binding.textInputLayoutAbsenceEditTo.error = errorTxt
                error = true
            } else {
                binding.textInputLayoutAbsenceEditTo.error = null
            }
        }
        if (binding.textInputLayoutAbsenceEditHours.visibility == View.VISIBLE) {
            if (binding.hours.text.isNullOrEmpty() && !binding.checkFullDay.isChecked) {
                binding.textInputLayoutAbsenceEditHours.error = errorTxt
                error = true
            } else {
                binding.textInputLayoutAbsenceEditHours.error = null
            }
        }

        if(binding.textInputLayoutAbsenceEditDeputy.visibility == View.VISIBLE){
            if (binding.textInputLayoutAbsenceEditDeputy.hint?.startsWith("*") == true &&
                binding.deputy.text.isNullOrEmpty()
            ) {
                binding.textInputLayoutAbsenceEditDeputy.error = errorTxt
                error = true
            } else {
                binding.textInputLayoutAbsenceEditDeputy.error = null
            }
        }

        return !error
    }

    private fun checkVacationVisibility() {
        binding.textInputLayoutAbsenceEditHours.visibility = View.GONE
        viewModel.viewModelScope.launch {
            if (viewModel.permission("urlaub.stunden.use") == "true") {
                requireActivity().runOnUiThread {
                    binding.checkFullDay.visibility = View.VISIBLE
                    binding.checkHalfDay.visibility = View.GONE
                    binding.linearLayoutTimeFromTo.visibility = View.VISIBLE
                }
            } else {
                requireActivity().runOnUiThread {
                    binding.checkFullDay.visibility = View.GONE
                    binding.checkHalfDay.visibility = View.VISIBLE
                    binding.linearLayoutTimeFromTo.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: Long, editorId: String, absenceTypeId: Long) =
            AbsenceFormFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                    putString(ARG_EDITOR_ID, editorId)
                    putLong(ARG_ABSENCE_TYPE_ID, absenceTypeId)
                }
            }

        fun newInstance(
            userId: Long,
            editorId: String,
            absenceTypeId: Long,
            entity: AbsenceEntryEntity
        ) =
            AbsenceFormFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                    putString(ARG_EDITOR_ID, editorId)
                    putLong(ARG_ABSENCE_TYPE_ID, absenceTypeId)
                    putParcelable(ARG_ENTITY, entity)
                }
            }
    }
}