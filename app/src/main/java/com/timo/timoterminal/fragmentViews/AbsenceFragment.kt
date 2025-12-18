package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.fragment.app.commit
import androidx.lifecycle.viewModelScope
import com.google.android.material.button.MaterialButton
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentAbsenceBinding
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.AbsenceFragmentViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val ARG_USERID = "userId"
private const val ARG_EDITOR_ID = "editorId"

class AbsenceFragment : Fragment() {
    private var userId: Long? = null
    private var editorId: String? = null
    private var first: Boolean = true

    private lateinit var binding: FragmentAbsenceBinding

    private val viewModel: AbsenceFragmentViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USERID)
            editorId = it.getString(ARG_EDITOR_ID)
        }
        viewModel.userId = userId ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAbsenceBinding.inflate(inflater, container, false)

        binding.fragmentAbsenceRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.absenceEditFragmentListButton.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            val frag = AbsenceListFragment()
            val bundle = Bundle()
            bundle.putString("userId", userId.toString())
            frag.arguments = bundle
            parentFragmentManager.commit {
                addToBackStack(null)
                replace(R.id.fragment_container_view, frag)
            }
        }

        observeLiveData()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)?.hideLoadMask()

        viewModel.loadForAbsence()
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
        viewModel.liveAbsenceTypes.value = emptyList()
        viewModel.liveAbsenceTypes.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                first = false
                val buttonStyle = ContextThemeWrapper(requireContext(), R.style.SettingsViewButtons)
                for (absenceType in it) {
                    val button = MaterialButton(buttonStyle)
                    button.text = absenceType.name
                    button.cornerRadius = 16.dpToPx()
                    button.textSize = 20f
                    val params = GridLayout.LayoutParams()
                    params.width = 330.dpToPx()
                    params.height = 70.dpToPx()
                    params.marginStart = 6.dpToPx()
                    params.marginEnd = 6.dpToPx()
                    button.layoutParams = params
                    button.setOnClickListener {
                        val frag = if (absenceType.startStop){
                            AbsenceStartStopFragment.newInstance(
                                userId = userId ?: -1L,
                                absenceTypeId = absenceType.id
                            )
                        } else {
                            AbsenceFormFragment.newInstance(
                                userId = userId ?: -1L,
                                editorId = editorId ?: "",
                                absenceTypeId = absenceType.id
                            )
                        }
                        parentFragmentManager.commit {
                            addToBackStack(null)
                            replace(R.id.fragment_container_view, frag)
                        }
                    }

                    binding.buttonContainer.addView(button)
                }
                viewModel.liveAbsenceTypes.value = emptyList()

                viewModel.viewModelScope.launch {
                    if (viewModel.hasUserErrorAbsenceEntries()){
                        binding.absenceEditFragmentListButton.visibility = View.VISIBLE
                    }
                }
            }
        }
        viewModel.liveLatestOpen.value = null
        viewModel.liveLatestOpen.observe(viewLifecycleOwner) {
            if (it != null) {
                if (first) {
                    first = false
                    val frag = AbsenceStartStopFragment.newInstance(
                        userId = userId ?: -1L,
                        absenceTypeId = it.getLong("absenceTypeId"),
                        it
                    )
                    parentFragmentManager.commit {
                        addToBackStack(null)
                        replace(R.id.fragment_container_view, frag)
                    }
                } else {
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun Int.dpToPx(): Int = Utils.dpToPx(this, resources.displayMetrics.density)

    companion object {
        @JvmStatic
        fun newInstance(userId: Long, editor: String) =
            AbsenceFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                    putString(ARG_EDITOR_ID, editor)
                }
            }
    }
}
