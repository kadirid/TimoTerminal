package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentAbsenceListBinding
import com.timo.timoterminal.entityAdaptor.AbsenceListAdapter
import com.timo.timoterminal.entityAdaptor.FilterEntityAdapter
import com.timo.timoterminal.entityClasses.AbsenceEntryEntity
import com.timo.timoterminal.repositories.AbsenceDeputyRepository
import com.timo.timoterminal.repositories.AbsenceEntryRepository
import com.timo.timoterminal.repositories.AbsenceTypeMatrixRepository
import com.timo.timoterminal.repositories.AbsenceTypeRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.AbsenceListViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AbsenceListFragment : Fragment() {
    private lateinit var binding: FragmentAbsenceListBinding
    private var userId: String? = null
    private var editorId: String? = null
    private var recyclerViewState: Parcelable? = null

    private val userRepository: UserRepository by inject()
    private val absenceTypeRepository: AbsenceTypeRepository by inject()
    private val absenceTypeMatrixRepository: AbsenceTypeMatrixRepository by inject()
    private val absenceDeputyRepository: AbsenceDeputyRepository by inject()
    private val absenceEntryRepository: AbsenceEntryRepository by inject()
    private val languageService: LanguageService by inject()
    private var adapt: FilterEntityAdapter? = null
    private var first: Boolean = true
    private var currentSort: Int = 0

    private val viewModel = AbsenceListViewModel(absenceEntryRepository)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAbsenceListBinding.inflate(inflater, container, false)

        userId = arguments?.getString("userId")
        editorId = arguments?.getString("editorId")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUp()
    }

    override fun onResume() {
        super.onResume()
        first = true

        setAdapter()
    }

    private fun setUp() {
        binding.fragmentAbsenceListEmptyListTextView.text =
            languageService.getText("#NoEntriesForBookedTimes")
        binding.textInputLayoutAbsenceListDropdown.hint =
            languageService.getText("ALLGEMEIN#Filtern")
        binding.textInputLayoutAbsenceListFilterText.hint =
            languageService.getText("ALLGEMEIN#Suchtext")
        binding.textInputLayoutAbsenceListDateRange.hint =
            languageService.getText("ALLGEMEIN#Suchtext")
        binding.buttonSortDate.text = languageService.getText("ALLGEMEIN#Datum")
        binding.buttonSortEmployee.text = languageService.getText("ALLGEMEIN#Mitarbeiter")
        binding.buttonSortAbsenceType.text = languageService.getText("ALLGEMEIN#Abwesenheitsart")
        "${languageService.getText("Leistungsnachweis#Sortierung")}:".also { binding.textViewSortLabel.text = it }

        binding.absenceListDropdown.setOnItemClickListener { parent, _, position, _ ->
            (activity as MainActivity?)?.restartTimer()
            val selected = parent.getItemAtPosition(position) as FilterEntityAdapter.FilterEntity
            if (selected.id == 1) {
                binding.textInputLayoutAbsenceListDateRange.visibility = View.VISIBLE
                binding.absenceListDateRange.text = null
                binding.textInputLayoutAbsenceListFilterText.visibility = View.GONE
            } else {
                binding.textInputLayoutAbsenceListDateRange.visibility = View.GONE
                binding.absenceListFilterText.text = null
                binding.textInputLayoutAbsenceListFilterText.visibility = View.VISIBLE
            }
        }
        binding.absenceListDateRange.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(languageService.getText("#DateRange"))
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second
                val formatted =
                    "${Utils.getDateFromTimestamp(startDate)} - ${Utils.getDateFromTimestamp(endDate)}"
                binding.absenceListDateRange.setText(formatted)

                val adapter =
                    binding.viewRecyclerAbsenceEntries.adapter as AbsenceListAdapter?
                adapter?.filter(startDate, endDate)
            }

            picker.show(parentFragmentManager, picker.toString())
        }
        binding.absenceListFilterText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                (activity as MainActivity?)?.restartTimer()
            }

            override fun afterTextChanged(s: Editable) {
                val filterId = adapt?.getIdByText(binding.absenceListDropdown.text.toString())
                val filterText = binding.absenceListFilterText.text.toString()

                val adapter =
                    binding.viewRecyclerAbsenceEntries.adapter as AbsenceListAdapter?
                adapter?.filter(filterId ?: -1, filterText)
            }
        })

        binding.fragmentAbsenceListRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        binding.buttonClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setUpSortButtons()
    }

    private fun setUpSortButtons() {
        binding.buttonSortDate.setOnClickListener {
            sort(1)

            (activity as MainActivity?)?.restartTimer()
        }

        binding.buttonSortEmployee.setOnClickListener {
            sort(2)

            (activity as MainActivity?)?.restartTimer()
        }

        binding.buttonSortAbsenceType.setOnClickListener {
            sort(3)

            (activity as MainActivity?)?.restartTimer()
        }
    }

    private fun sort(sortId: Int) {
        val theme = requireContext().theme
        val bgTint =
            ResourcesCompat.getColorStateList(resources, R.color.md_theme_surfaceVariant, theme)
        val textColor = ResourcesCompat.getColor(resources, R.color.md_theme_onSurface, theme)
        val inBgTint =
            ResourcesCompat.getColorStateList(resources, R.color.md_theme_background, theme)
        val inTextColor = ResourcesCompat.getColor(resources, R.color.md_theme_onBackground, theme)

        currentSort = sortId
        val arrowDown =
            ResourcesCompat.getDrawable(resources, R.drawable.baseline_arrow_down, theme)
        val adapter = binding.viewRecyclerAbsenceEntries.adapter as AbsenceListAdapter?

        if (sortId != 1) {
            binding.buttonSortDate.icon = arrowDown?.constantState?.newDrawable()?.mutate()
            binding.buttonSortDate.iconTint = inBgTint
            binding.buttonSortDate.setResId(null)
            binding.buttonSortDate.backgroundTintList = inBgTint
            binding.buttonSortDate.setTextColor(inTextColor)
        } else {
            binding.buttonSortDate.backgroundTintList = bgTint
            binding.buttonSortDate.setTextColor(textColor)
            if (binding.buttonSortDate.getResId() == R.drawable.baseline_arrow_up) {
                binding.buttonSortDate.icon = arrowDown?.constantState?.newDrawable()?.mutate()
                binding.buttonSortDate.setResId(R.drawable.baseline_arrow_down)
                adapter?.sort(1, false)
            } else {
                binding.buttonSortDate.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.baseline_arrow_up,
                        theme
                    )
                binding.buttonSortDate.setResId(R.drawable.baseline_arrow_up)
                adapter?.sort(1, true)
            }
            binding.buttonSortDate.setIconTintResource(R.color.md_theme_onSurface)
        }

        if (sortId != 2) {
            binding.buttonSortEmployee.icon = arrowDown?.constantState?.newDrawable()?.mutate()
            binding.buttonSortEmployee.iconTint = inBgTint
            binding.buttonSortEmployee.setResId(null)
            binding.buttonSortEmployee.backgroundTintList = inBgTint
            binding.buttonSortEmployee.setTextColor(inTextColor)
        } else {
            binding.buttonSortEmployee.backgroundTintList = bgTint
            binding.buttonSortEmployee.setTextColor(textColor)
            if (binding.buttonSortEmployee.getResId() == R.drawable.baseline_arrow_up) {
                binding.buttonSortEmployee.icon = arrowDown?.constantState?.newDrawable()?.mutate()
                binding.buttonSortEmployee.setResId(R.drawable.baseline_arrow_down)
                adapter?.sort(2, false)
            } else {
                binding.buttonSortEmployee.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.baseline_arrow_up,
                        theme
                    )
                binding.buttonSortEmployee.setResId(R.drawable.baseline_arrow_up)
                adapter?.sort(2, true)
            }
            binding.buttonSortEmployee.setIconTintResource(R.color.md_theme_onSurface)
        }

        if (sortId != 3) {
            binding.buttonSortAbsenceType.icon = arrowDown?.constantState?.newDrawable()?.mutate()
            binding.buttonSortAbsenceType.iconTint = inBgTint
            binding.buttonSortAbsenceType.setResId(null)
            binding.buttonSortAbsenceType.backgroundTintList = inBgTint
            binding.buttonSortAbsenceType.setTextColor(inTextColor)
        } else {
            binding.buttonSortAbsenceType.backgroundTintList = bgTint
            binding.buttonSortAbsenceType.setTextColor(textColor)
            if (binding.buttonSortAbsenceType.getResId() == R.drawable.baseline_arrow_up) {
                binding.buttonSortAbsenceType.icon = arrowDown?.constantState?.newDrawable()?.mutate()
                binding.buttonSortAbsenceType.setResId(R.drawable.baseline_arrow_down)
                adapter?.sort(3, false)
            } else {
                binding.buttonSortAbsenceType.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.baseline_arrow_up,
                        theme
                    )
                binding.buttonSortAbsenceType.setResId(R.drawable.baseline_arrow_up)
                adapter?.sort(3, true)
            }
            binding.buttonSortAbsenceType.setIconTintResource(R.color.md_theme_onSurface)
        }
    }

    private fun resort(){
        val adapter = binding.viewRecyclerAbsenceEntries.adapter as AbsenceListAdapter?
        when (currentSort) {
            1 -> {
                if (binding.buttonSortDate.getResId() == R.drawable.baseline_arrow_up) {
                    adapter?.sort(1, true)
                } else {
                    adapter?.sort(1, false)
                }
            }
            2 -> {
                if (binding.buttonSortEmployee.getResId() == R.drawable.baseline_arrow_up) {
                    adapter?.sort(2, true)
                } else {
                    adapter?.sort(2, false)
                }
            }
            3 -> {
                if (binding.buttonSortAbsenceType.getResId() == R.drawable.baseline_arrow_up) {
                    adapter?.sort(3, true)
                } else {
                    adapter?.sort(3, false)
                }
            }
            else -> {}
        }
    }


    private fun setAdapter() {
        adapt = FilterEntityAdapter(
            requireContext(),
            R.layout.dropdown_small,
            languageService.getText("ALLGEMEIN#Datum"),
            languageService.getText("ALLGEMEIN#Mitarbeiter"),
            languageService.getText("ALLGEMEIN#Abwesenheitsart"),
            null, null
        )
        binding.absenceListDropdown.setAdapter(adapt)

        binding.viewRecyclerAbsenceEntries.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (activity as MainActivity?)?.restartTimer()
                if (dy > 0 && !recyclerView.canScrollVertically(1)) { // Scrolled to bottom
                    viewModel.loadMoreItems(userId)
                }
            }
        })

        viewModel.items.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) return@observe
            viewModel.viewModelScope.launch {
                val userEntities = userRepository.getAllAsList()
                val absenceTypeEntities = absenceTypeRepository.getAll()
                val absenceTypeMatrixEntities = absenceTypeMatrixRepository.getAll()
                val absenceDeputyEntities = absenceDeputyRepository.getAll()

                val userMap = HashMap<String, String>()
                val absenceMap = HashMap<String, String>()
                val matrixMap = HashMap<String, String>()
                val deputyMap = HashMap<String, String>()

                for (user in userEntities) {
                    userMap[user.id.toString()] = user.name()
                }
                for (absence in absenceTypeEntities) {
                    absenceMap[absence.id.toString()] = absence.name
                }
                for (matrix in absenceTypeMatrixEntities) {
                    matrixMap[matrix.id.toString()] = matrix.name
                }
                for (deputy in absenceDeputyEntities) {
                    var c = 0
                    while (c < deputy.deputies.length()) {
                        val uDeputy = deputy.deputies.getJSONObject(c)
                        if (uDeputy != null) {
                            deputyMap[uDeputy.getString("id")] = uDeputy.getString("name")
                        }
                        c++
                    }
                }

                recyclerViewState =
                    binding.viewRecyclerAbsenceEntries.layoutManager?.onSaveInstanceState()

                binding.viewRecyclerAbsenceEntries.adapter =
                    AbsenceListAdapter(
                        items,
                        absenceMap,
                        userMap,
                        matrixMap,
                        deputyMap,
                        object : AbsenceListAdapter.OnItemClickListener {
                            override fun onItemClick(entity: AbsenceEntryEntity) {
                                (activity as MainActivity?)?.restartTimer()
                                val absenceTypeId = entity.absenceTypeId
                                val user =
                                    userEntities.find { user -> user.id == entity.userId.toLong() }

                                if (user != null) {
                                    parentFragmentManager.commit {
                                        addToBackStack(null)
                                        replace(
                                            R.id.fragment_container_view,
                                            AbsenceFormFragment.newInstance(
                                                user.id,
                                                editorId ?: user.id.toString(),
                                                absenceTypeId.toLong(),
                                                entity
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        languageService.getText("#Absence", " ").substring(0, 1),
                        languageService.getText("#Matrix", " ").substring(0, 1),
                        languageService.getText("#Deputy", " ").substring(0, 1),
                        languageService.getText("#Ganzer Tag")
                    )

                binding.viewRecyclerAbsenceEntries.layoutManager?.onRestoreInstanceState(
                    recyclerViewState
                )
                if (items.isNotEmpty()) {
                    binding.viewRecyclerAbsenceEntries.visibility = View.VISIBLE
                    binding.fragmentAbsenceListEmptyListTextView.visibility = View.GONE
                }
                resort()
            }
            if(first) {
                first = false
                sort(1)
            }
        }
        viewModel.loadMoreItems(userId)
    }
}