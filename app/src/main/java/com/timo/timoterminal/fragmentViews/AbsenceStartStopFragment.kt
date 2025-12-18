package com.timo.timoterminal.fragmentViews

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentAbsenceStartStopBinding
import com.timo.timoterminal.entityAdaptor.AbsenceDeputyEntityAdapter
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.AbsenceEditViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject

private const val ARG_USERID = "userId"
private const val ARG_ABSENCE_TYPE_ID = "absenceTypeId"
private const val ARG_ABSENCE_OBJ = "absenceObj"

class AbsenceStartStopFragment : Fragment() {
    private val handler = Handler(Looper.getMainLooper())
    private val languageService: LanguageService by inject()
    private var userId: Long? = null
    private var absenceTypeId: Long? = null
    private var absenceObj: JSONObject? = null
    private var uptime = SystemClock.uptimeMillis()
    private var startTime: Long = 0
    private var inTime: Long = 0
    private var sendTime: Long = 0
    private var wtId : Long = -1
    private var id : Long = -1
    private var offlineSaved : Boolean = false

    private lateinit var binding: FragmentAbsenceStartStopBinding

    private val viewModel: AbsenceEditViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USERID)
            absenceTypeId = it.getLong(ARG_ABSENCE_TYPE_ID)
            val objStr = it.getString(ARG_ABSENCE_OBJ)
            if (!objStr.isNullOrEmpty()) {
                absenceObj = JSONObject(objStr)
                if (absenceObj?.has("createdTime") == true){
                    val date = absenceObj?.optString("date") ?: ""
                    val from = absenceObj?.optString("from") ?: ""
                    if (date.isNotEmpty() && from.isNotEmpty()) {
                        val time = "$date $from:00"
                        Utils.parseDBValue(time).let { gc ->
                            inTime = (gc.timeInMillis / 60000) * 60
                        }
                    }
                } else if(absenceObj?.has("date") == true) {
                    Utils.parseDBDateTime(absenceObj?.optString("date") ?: "").let { time ->
                        inTime = time.timeInMillis / 1000
                    }
                }
                id = absenceObj?.optLong("id", -1) ?: -1
                wtId = absenceObj?.optLong("wtId", -1) ?: -1
                offlineSaved = id != -1L
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        binding = FragmentAbsenceStartStopBinding.inflate(inflater, container, false)

        viewModel.absenceTypeId = absenceTypeId ?: -1L
        viewModel.userId = userId ?: -1L

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUp()
    }

    private fun setUp() {
        binding.fragmentAbsenceRootLayout.setOnClickListener {
            (activity as? MainActivity)?.restartTimer()
        }

        binding.descriptionCloseButton.setOnClickListener {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.description.windowToken, 0)
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.goToSecondPageButton.setOnClickListener {
            (activity as? MainActivity)?.restartTimer()

            binding.firstPageDetailLayout.visibility = View.GONE
            binding.secondPageContainer.visibility = View.VISIBLE
        }

        binding.startAbsenceButton.setOnClickListener {
            startAbsenceTime()
        }

        binding.stopAbsenceButton.setOnClickListener {
            stopAbsenceTime()
        }

        setUpOnItemClickListeners()

        setLanguageTexts()

        addTextWatcher()

        observerLiveData()

        viewModel.loadValuesForStartStop()

        handler.post(updateTimeRunnable)

        if (wtId != -1L || offlineSaved) {
            binding.description.setText(absenceObj?.optString("description") ?: "")
            binding.deputy.setText(absenceObj?.optString("deputy") ?: "")

            viewModel.liveSwitchView.postValue(true)
        }
    }

    private fun setUpOnItemClickListeners() {
        binding.deputy.setOnItemClickListener { _, _, _, _ ->
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.deputy.windowToken, 0)
            (activity as MainActivity?)?.restartTimer()
        }
    }

    private fun setLanguageTexts() {
        binding.textInputLayoutAbsenceStartStopDeputy.hint = languageService.getText("#Deputy")

        binding.startAbsenceButton.text = languageService.getText("#StartAbsence")
        binding.stopAbsenceButton.text = languageService.getText("#StopAbsence")
    }

    private fun observerLiveData() {
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
                binding.absenceTypeText.text = it.name
                binding.secondAbsenceTypeText.text = it.name
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
        viewModel.livePostWtId.value = -1L
        viewModel.livePostWtId.observe(viewLifecycleOwner) {
            if (it != -1L) {
                wtId = it
            }
        }
        viewModel.liveSwitchView.observe(viewLifecycleOwner) {
            startTime = if(it) if(sendTime != 0L) sendTime else inTime else 0L
            binding.stopAbsenceButton.visibility = if(it) View.VISIBLE else View.GONE
            binding.startAbsenceButton.visibility = if(it) View.GONE else View.VISIBLE
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

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            uptime += 1000
            handler.postAtTime(this, uptime)

            viewModel.viewModelScope.launch {
                updateTimeValues()
            }
        }
    }

    private fun startAbsenceTime() {
        val data = HashMap<String, String?>()
        val cal = Utils.getCal()
        sendTime = (cal.timeInMillis / 60000) * 60

        data["userId"] = userId.toString()
        data["absenceTypeId"] = absenceTypeId.toString()
        data["date"] = Utils.getDateFromGC(cal)
        data["from"] = Utils.getTimeFromGC(cal)
        data["description"] = binding.description.text.toString()
        val deputyEntityAdapter = binding.deputy.adapter as AbsenceDeputyEntityAdapter?
        data["deputy"] = deputyEntityAdapter?.getItemByName(binding.deputy.text.toString())
            ?.optLong("id", -1L)?.toString() ?: "-1"
        data["editorId"] = userId.toString()

        viewModel.startAbsenceTime(data, requireContext())

    }

    private fun stopAbsenceTime() {
        val data = HashMap<String, String?>()
        val cal = Utils.getCal()

        data["id"] = id.toString()
        data["userId"] = userId.toString()
        data["absenceTypeId"] = absenceTypeId.toString()
        data["wtId"] = wtId.toString()
        data["dateTo"] = Utils.getDateFromGC(cal)
        data["to"] = Utils.getTimeFromGC(cal)
        data["description"] = binding.description.text.toString()
        val deputyEntityAdapter = binding.deputy.adapter as AbsenceDeputyEntityAdapter?
        data["deputy"] = deputyEntityAdapter?.getItemByName(binding.deputy.text.toString())
            ?.optLong("id", -1L)?.toString() ?: "-1"
        data["editorId"] = userId.toString()

        viewModel.stopAbsenceTime(data, requireContext(), offlineSaved)
    }

    private fun updateTimeValues() {
        val cal = Utils.getCal()
        val currentTime = Utils.getTimeWithSecondsFormGC(cal)
        val currentDate = Utils.getDateFromGC(cal)
        val runningTime =Utils.convertTimeWithSeconds(
            if (startTime == 0L)
                0L
            else
                Utils.getCal().timeInMillis/1000 - startTime
        )

        if (binding.firstPageDetailLayout.visibility == View.VISIBLE) {
            binding.timeView.text = currentTime
            binding.dateView.text = currentDate
            binding.absenceTimeCounter.text = runningTime
        } else {
            binding.secondTimeView.text = currentTime
            binding.secondDateView.text = currentDate
            binding.secondTimeCounter.text = runningTime
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(userId: Long, absenceTypeId: Long) =
            AbsenceStartStopFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                    putLong(ARG_ABSENCE_TYPE_ID, absenceTypeId)
                }
            }

        fun newInstance(userId: Long, absenceTypeId: Long, obj:JSONObject) =
            AbsenceStartStopFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                    putLong(ARG_ABSENCE_TYPE_ID, absenceTypeId)
                    putString(ARG_ABSENCE_OBJ, obj.toString())
                }
            }
    }
}