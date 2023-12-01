package com.timo.timoterminal.fragmentViews

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.databinding.FragmentAttendanceBinding
import com.timo.timoterminal.modalBottomSheets.MBSheetFingerprintCardReader
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.AttendanceFragmentViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.interfaces.RfidListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import kotlin.math.abs

class AttendanceFragment : Fragment(), RfidListener, FingerprintListener {

    private val sharedPrefService: SharedPrefService by inject()
    private val userRepository: UserRepository by inject()
    private var _broadcastReceiver: BroadcastReceiver? = null
    private var lastClick: Long = 0
    private lateinit var binding: FragmentAttendanceBinding
    private val httpService: HttpService = HttpService()
    private val viewModel = AttendanceFragmentViewModel(sharedPrefService, userRepository)
    private var funcCode = -1
    private val mbSheetFingerprintCardReader = MBSheetFingerprintCardReader {
        ->
        this.setListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false)

        setOnClickListeners()
        adaptLottieAnimationTime()
        setText()
        return binding.root
    }

    private fun setText() {
        val gc = GregorianCalendar()
        binding.textViewDateTimeViewContainer.text = Utils.getDateWithNameFromGC(gc)
        binding.textViewTimeTimeViewContainer.text = Utils.getTimeFromGC(gc)
    }

    override fun onStart() {
        super.onStart()
        _broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent) {
                if (intent.action!!.compareTo(Intent.ACTION_TIME_TICK) == 0){
                    val gc = GregorianCalendar()
                    binding.textViewDateTimeViewContainer.text = Utils.getDateWithNameFromGC(gc)
                    binding.textViewTimeTimeViewContainer.text = Utils.getTimeFromGC(gc)
                }
            }
        }
        requireContext().registerReceiver(_broadcastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }

    override fun onStop() {
        super.onStop()
        if (_broadcastReceiver != null) requireContext().unregisterReceiver(_broadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        setListener()
        adaptLottieAnimationTime()
    }

    // remove listener on pause
    override fun onPause() {
        RfidService.unregister()
        FingerprintService.unregister()

        super.onPause()
    }

    // set booking code and start listening
    private fun setOnClickListeners() {
        binding.buttonKommen.setOnClickListener {
            if (abs(lastClick - SystemClock.elapsedRealtime()) > 500) {
                funcCode = 100
                executeClick()
                lastClick = SystemClock.elapsedRealtime()
            }
        }
        binding.buttonPauseAnfang.setOnClickListener {
            if (abs(lastClick - SystemClock.elapsedRealtime()) > 500) {
                funcCode = 110
                executeClick()
                lastClick = SystemClock.elapsedRealtime()
            }
        }
        binding.buttonGehen.setOnClickListener {
            if (abs(lastClick - SystemClock.elapsedRealtime()) > 500) {
                funcCode = 200
                executeClick()
                lastClick = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun executeClick() {
        RfidService.unregister()
        FingerprintService.unregister()
        val bundle = Bundle()
        bundle.putInt("status", funcCode)
        mbSheetFingerprintCardReader.arguments = bundle
        mbSheetFingerprintCardReader.show(parentFragmentManager, MBSheetFingerprintCardReader.TAG)

    }

    // start listening to card reader
    private fun setListener() {
        RfidService.setListener(this)
        RfidService.register()
        FingerprintService.setListener(this)
        FingerprintService.register()
    }

    // send all necessary information to timo to create a booking
    private fun sendBooking(card: String, inputCode: Int) {
        val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        viewModel.viewModelScope.launch {
            val url = viewModel.getURl()
            val company = viewModel.getCompany()
            val terminalId = viewModel.getTerminalID()
            if (!company.isNullOrEmpty() && terminalId != null && terminalId > -1) {
                httpService.post(
                    "${url}services/rest/zktecoTerminal/bookingWithoutType",
                    mapOf(
                        Pair("card", card),
                        Pair("firma", company),
                        Pair("date", dateFormatter.format(Date())),
                        Pair("inputCode", "$inputCode"),
                        Pair("terminalId", "$terminalId")
                    ),
                    requireContext(),
                    { obj, _, msg ->
                        if (obj != null) {
                            viewModel.showMessage(
                                this@AttendanceFragment,
                                card,
                                obj.getInt("funcCode"),
                                obj.getBoolean("success"),
                                obj.getString("message")
                            )
                        }
                        if (!msg.isNullOrEmpty()) {
                            activity?.runOnUiThread {
                                Utils.showMessage(parentFragmentManager, msg)
                            }
                        }
                    }
                )
            }
        }
    }

    companion object {
        const val TAG = "AttendanceFragmentTag"
    }

    // get code of scanned card
    override fun onRfidRead(rfidInfo: String) {
        val rfidCode = rfidInfo.toLongOrNull(16)
        if (rfidCode != null) {
            var oct = rfidCode.toString(8)
            while (oct.length < 9) {
                oct = "0$oct"
            }
            oct = oct.reversed()
            sendBooking(oct, 2)
            //binding.textViewAttendanceState.text = "Keine Anwesenheit ausgewählt"
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        Log.d("FP", fingerprint)
//        FingerprintService.unregister()
    }

    private fun adaptLottieAnimationTime() {
        super.onResume()
        val calendar: Calendar = Calendar.getInstance()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour < 7 || hour > 20) {
            binding.lottieAnimationView.setMinAndMaxFrame(0, 60)
            binding.lottieAnimationView.playAnimation()
        } else {
            binding.lottieAnimationView.setMinAndMaxFrame(60, 120)
            binding.lottieAnimationView.playAnimation()
        }
    }
}