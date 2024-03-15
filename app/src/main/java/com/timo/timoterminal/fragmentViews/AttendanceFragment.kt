package com.timo.timoterminal.fragmentViews

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentAttendanceBinding
import com.timo.timoterminal.modalBottomSheets.MBSheetFingerprintCardReader
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.AttendanceFragmentViewModel
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.Calendar

class AttendanceFragment : Fragment() {

    private val languageService: LanguageService by inject()
    private val soundSource: SoundSource by inject()
    private val httpService: HttpService by inject()

    private var _broadcastReceiver: BroadcastReceiver? = null
    private lateinit var binding: FragmentAttendanceBinding
    private val viewModel: AttendanceFragmentViewModel by sharedViewModel()
    private var funcCode = -1
    private var mbSheetFingerprintCardReader: MBSheetFingerprintCardReader? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false)

        setUpListeners()
        setText()
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
                    setText()
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
        viewModel.loadSoundForAttendance()
        adaptLottieAnimationTime()
    }

    // remove listener on pause
    override fun onPause() {
        RfidService.unregister()
        FingerprintService.unregister()

        super.onPause()
    }

    // set booking code and start listening
    private fun setUpListeners() {
        viewModel.viewModelScope.launch {
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

            viewModel.liveErrorMsg.value = ""
            viewModel.liveErrorMsg.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    notifyVerificationFailed(it)
                    viewModel.liveErrorMsg.value = ""
                }
            }
            viewModel.liveUserCard.value = Pair("", -1)
            viewModel.liveUserCard.observe(viewLifecycleOwner) {
                if (it.first.isNotEmpty()) {
                    sendBooking(it.first, it.second)
                    viewModel.liveUserCard.value = Pair("", -1)
                }
            }
        }
    }

    private fun executeClick() {
        RfidService.unregister()
        FingerprintService.unregister()
        val bundle = Bundle()
        bundle.putInt("status", funcCode)
        if (!(mbSheetFingerprintCardReader != null
                    && mbSheetFingerprintCardReader?.dialog != null
                    && mbSheetFingerprintCardReader?.dialog?.isShowing == true
                    && mbSheetFingerprintCardReader?.isRemoving == false)
        ) {

            if (mbSheetFingerprintCardReader == null) {
                mbSheetFingerprintCardReader = MBSheetFingerprintCardReader {
                    this.setListener()
                }
            }
            mbSheetFingerprintCardReader!!.arguments = bundle
            mbSheetFingerprintCardReader!!.show(
                parentFragmentManager,
                MBSheetFingerprintCardReader.TAG
            )
        }

    }

    // start listening to card reader
    private fun setListener() {
        RfidService.unregister()
        FingerprintService.unregister()
        RfidService.setListener(viewModel)
        RfidService.register()
        FingerprintService.setListener(viewModel)
        FingerprintService.register()
    }

    // send all necessary information to timo to create a booking
    private fun sendBooking(card: String, inputCode: Int) {
        (activity as MainActivity?)?.showLoadMask()
        if (Utils.isOnline(requireContext())) {
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
                            Pair("date", Utils.getDateTimeFromGC(Utils.getCal())),
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
                                activity?.runOnUiThread {
                                    notifyFailure(msg)
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
                    notifyFailure(languageService.getText("#InternetRequired"))
                }
            }
        } else {
            notifyFailure(languageService.getText("#InternetRequired"))
        }
    }

    companion object {
        const val TAG = "AttendanceFragmentTag"
    }

    private fun notifyFailure(msg: String) {
        (activity as MainActivity?)?.hideLoadMask()
        soundSource.playSound(SoundSource.failedSound)
        Utils.showMessage(parentFragmentManager, msg)
    }

    private fun notifyVerificationFailed(msg: String) {
        (activity as MainActivity?)?.hideLoadMask()
        soundSource.playSound(SoundSource.authenticationFailed)
        Utils.showMessage(parentFragmentManager, msg)
    }

    private fun adaptLottieAnimationTime() {
        viewModel.viewModelScope.launch {
            val hour: Int = Utils.getCal().get(Calendar.HOUR_OF_DAY)

            if (hour < 7 || hour > 20) {
                binding.lottieAnimationView.setMinAndMaxFrame(0, 60)
                binding.lottieAnimationView.playAnimation()
            } else {
                binding.lottieAnimationView.setMinAndMaxFrame(60, 120)
                binding.lottieAnimationView.playAnimation()
            }
        }
    }
}