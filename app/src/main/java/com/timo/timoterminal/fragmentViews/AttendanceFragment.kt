package com.timo.timoterminal.fragmentViews

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentAttendanceBinding
import com.timo.timoterminal.modalBottomSheets.MBSheetFingerprintCardReader
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.AttendanceFragmentViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AttendanceFragment : Fragment(), TimoRfidListener, FingerprintListener {

    private val sharedPrefService: SharedPrefService by inject()
    private val userRepository: UserRepository by inject()
    private val languageService: LanguageService by inject()
    private val soundSource: SoundSource by inject()

    private var _broadcastReceiver: BroadcastReceiver? = null
    private lateinit var binding: FragmentAttendanceBinding
    private val httpService: HttpService = HttpService()
    private val viewModel = AttendanceFragmentViewModel(sharedPrefService, userRepository)
    private var funcCode = -1
    private val mbSheetFingerprintCardReader = MBSheetFingerprintCardReader {
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
        soundSource.loadForAttendance()
        return binding.root
    }

    private fun setText() {
        val gc = Utils.getCal()
        binding.textViewDateTimeViewContainer.text = Utils.getDateWithNameFromGC(gc)
        binding.textViewTimeTimeViewContainer.text = Utils.getTimeFromGC(gc)
    }

    override fun onStart() {
        super.onStart()
        _broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent) {
                if (intent.action!!.compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    val gc = Utils.getCal()
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
        binding.buttonKommen.setSafeOnClickListener {
            funcCode = 100
            executeClick()
        }
        binding.buttonPauseAnfang.setSafeOnClickListener {
            funcCode = 110
            executeClick()
        }
        binding.buttonGehen.setSafeOnClickListener {
            funcCode = 200
            executeClick()
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
        RfidService.unregister()
        FingerprintService.unregister()
        RfidService.setListener(this)
        RfidService.register()
        FingerprintService.setListener(this)
        FingerprintService.register()
    }

    // send all necessary information to timo to create a booking
    private fun sendBooking(card: String, inputCode: Int) {
        if (Utils.isOnline(requireContext())) {
            val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            viewModel.viewModelScope.launch {
                val url = viewModel.getURl()
                val company = viewModel.getCompany()
                val terminalId = viewModel.getTerminalID()
                val token = viewModel.getToken()
                if (!company.isNullOrEmpty() && terminalId > 0 && token.isNotEmpty()) {
                    httpService.post(
                        "${url}services/rest/zktecoTerminal/bookingWithoutType",
                        mapOf(
                            Pair("card", card),
                            Pair("firma", company),
                            Pair("date", dateFormatter.format(Date())),
                            Pair("inputCode", "$inputCode"),
                            Pair("terminalId", "$terminalId"),
                            Pair("token", token)
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
                                soundSource.playSound(SoundSource.failedSound)
                                activity?.runOnUiThread {
                                    Utils.showMessage(parentFragmentManager, msg)
                                }
                            }
                            (activity as MainActivity?)?.hideLoadMask()
                        }, { e, res, context, output ->
                            (activity as MainActivity?)?.hideLoadMask()
                            HttpService.handleGenericRequestError(
                                e,
                                res,
                                context,
                                output,
                                languageService.getText("#TimoServiceNotReachable")
                            )
                        }
                    )
                } else {
                    (activity as MainActivity?)?.hideLoadMask()
                    Utils.showMessage(
                        parentFragmentManager,
                        languageService.getText("#InternetRequired")
                    )
                }
            }
        } else {
            (activity as MainActivity?)?.hideLoadMask()
            Utils.showMessage(parentFragmentManager, languageService.getText("#InternetRequired"))
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
            (activity as MainActivity?)?.showLoadMask()
            sendBooking(oct, 1)
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        Log.d("FP", fingerprint)
        // get Key associated to the fingerprint
        FingerprintService.identify(template)?.run {
            Log.d("FP Key", this)
            val id = this.substring(0, this.length - 2).toLong()
            viewModel.viewModelScope.launch {
                val user = viewModel.getUser(id)
                if (user != null) {
                    (activity as MainActivity?)?.showLoadMask()
                    sendBooking(user.card, 2)
                } else {
                    soundSource.playSound(SoundSource.authenticationFailed)
                }
            }
        }
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