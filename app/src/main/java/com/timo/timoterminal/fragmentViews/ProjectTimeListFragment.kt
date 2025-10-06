package com.timo.timoterminal.fragmentViews

import android.content.res.Resources
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
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
import com.timo.timoterminal.databinding.FragmentProjectTimeListBinding
import com.timo.timoterminal.entityAdaptor.FilterEntityAdapter
import com.timo.timoterminal.entityAdaptor.ProjectTimeEntityAdapter
import com.timo.timoterminal.entityClasses.ProjectTimeEntity
import com.timo.timoterminal.repositories.CustomerGroupRepository
import com.timo.timoterminal.repositories.CustomerRepository
import com.timo.timoterminal.repositories.ProjectRepository
import com.timo.timoterminal.repositories.ProjectTimeRepository
import com.timo.timoterminal.repositories.TaskRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.ProjectTimeListFragmentViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ProjectTimeListFragment : Fragment() {
    private lateinit var binding: FragmentProjectTimeListBinding
    private var userId: String? = null
    private val projectTimeRepository: ProjectTimeRepository by inject()
    private val userRepository: UserRepository by inject()
    private val projectRepository: ProjectRepository by inject()
    private val taskRepository: TaskRepository by inject()
    private val customerRepository: CustomerRepository by inject()
    private val customerGroupRepository: CustomerGroupRepository by inject()
    private val languageService: LanguageService by inject()
    private val projectTimeListFragmentViewModel =
        ProjectTimeListFragmentViewModel(projectTimeRepository)
    private var recyclerViewState: Parcelable? = null
    private var adapt: FilterEntityAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectTimeListBinding.inflate(inflater, container, false)

        userId = arguments?.getString("userId")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
        setUp()
    }

    private fun setUp() {
        binding.fragmentProjectTimeListEmptyListTextView.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        binding.fragmentProjectTimeListEmptyListTextView.text =
            languageService.getText("#NoEntriesForBookedTimes")
        binding.textInputLayoutProjectTimeListDropdown.hint =
            languageService.getText("ALLGEMEIN#Filtern")
        binding.textInputLayoutProjectTimeListFilterText.hint =
            languageService.getText("ALLGEMEIN#Suchtext")
        binding.textInputLayoutProjectTimeListDateRange.hint =
            languageService.getText("ALLGEMEIN#Suchtext")
        binding.buttonSortDate.text = languageService.getText("ALLGEMEIN#Datum")
        binding.buttonSortEmployee.text = languageService.getText("ALLGEMEIN#Mitarbeiter")
        binding.buttonSortProject.text = languageService.getText("ALLGEMEIN#Projekt")
        binding.buttonSortTask.text = languageService.getText("ALLGEMEIN#Vorgang")
        binding.buttonSortCustomer.text = languageService.getText("ALLGEMEIN#Kunde")
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.projectTimeListDropdown.setOnItemClickListener { parent, _, position, _ ->
            (activity as MainActivity?)?.restartTimer()
            val selected = parent.getItemAtPosition(position) as FilterEntityAdapter.FilterEntity
            if (selected.id == 1) {
                binding.textInputLayoutProjectTimeListDateRange.visibility = View.VISIBLE
                binding.projectTimeListDateRange.text = null
                binding.textInputLayoutProjectTimeListFilterText.visibility = View.GONE
            } else {
                binding.textInputLayoutProjectTimeListDateRange.visibility = View.GONE
                binding.projectTimeListFilterText.text = null
                binding.textInputLayoutProjectTimeListFilterText.visibility = View.VISIBLE
            }
        }
        binding.projectTimeListDateRange.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(languageService.getText("#DateRange"))
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second
                val formatted =
                    "${Utils.getDateFromTimestamp(startDate)} - ${Utils.getDateFromTimestamp(endDate)}"
                binding.projectTimeListDateRange.setText(formatted)

                val adapter =
                    binding.viewRecyclerProjectTimeList.adapter as ProjectTimeEntityAdapter?
                adapter?.filter(startDate, endDate)
            }

            picker.show(parentFragmentManager, picker.toString())
        }
        binding.projectTimeListFilterText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                (activity as MainActivity?)?.restartTimer()
            }

            override fun afterTextChanged(s: Editable) {
                val filterId = adapt?.getIdByText(binding.projectTimeListDropdown.text.toString())
                val filterText = binding.projectTimeListFilterText.text.toString()

                val adapter =
                    binding.viewRecyclerProjectTimeList.adapter as ProjectTimeEntityAdapter?
                adapter?.filter(filterId ?: -1, filterText)
            }
        })

        setUpSortButtons()
    }

    private fun setUpSortButtons() {
        val currentTheme = requireContext().theme

        binding.buttonSortDate.setOnClickListener {
            resetSortButtons(1, currentTheme)

            (activity as MainActivity?)?.restartTimer()
            val adapter = binding.viewRecyclerProjectTimeList.adapter as ProjectTimeEntityAdapter?
            when (binding.buttonSortDate.getResId()) {
                R.drawable.baseline_arrow_up -> {
                    binding.buttonSortDate.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_down,
                            currentTheme
                        )
                    binding.buttonSortDate.setResId(R.drawable.baseline_arrow_down)
                    adapter?.sort(1, false)
                }

                else -> {
                    binding.buttonSortDate.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_up,
                            currentTheme
                        )
                    binding.buttonSortDate.setResId(R.drawable.baseline_arrow_up)
                    adapter?.sort(1, true)
                }
            }
        }

        binding.buttonSortEmployee.setOnClickListener {
            resetSortButtons(2, currentTheme)

            (activity as MainActivity?)?.restartTimer()
            val adapter = binding.viewRecyclerProjectTimeList.adapter as ProjectTimeEntityAdapter?
            when (binding.buttonSortEmployee.getResId()) {
                R.drawable.baseline_arrow_up -> {
                    binding.buttonSortEmployee.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_down,
                            currentTheme
                        )
                    binding.buttonSortEmployee.setResId(R.drawable.baseline_arrow_down)
                    adapter?.sort(2, false)
                }

                else -> {
                    binding.buttonSortEmployee.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_up,
                            currentTheme
                        )
                    binding.buttonSortEmployee.setResId(R.drawable.baseline_arrow_up)
                    adapter?.sort(2, true)
                }
            }
        }

        binding.buttonSortProject.setOnClickListener {
            resetSortButtons(3, currentTheme)

            (activity as MainActivity?)?.restartTimer()
            val adapter = binding.viewRecyclerProjectTimeList.adapter as ProjectTimeEntityAdapter?
            when (binding.buttonSortProject.getResId()) {
                R.drawable.baseline_arrow_up -> {
                    binding.buttonSortProject.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_down,
                            currentTheme
                        )
                    binding.buttonSortProject.setResId(R.drawable.baseline_arrow_down)
                    adapter?.sort(3, false)
                }

                else -> {
                    binding.buttonSortProject.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_up,
                            currentTheme
                        )
                    binding.buttonSortProject.setResId(R.drawable.baseline_arrow_up)
                    adapter?.sort(3, true)
                }
            }
        }

        binding.buttonSortTask.setOnClickListener {
            resetSortButtons(4, currentTheme)

            (activity as MainActivity?)?.restartTimer()
            val adapter = binding.viewRecyclerProjectTimeList.adapter as ProjectTimeEntityAdapter?
            when (binding.buttonSortTask.getResId()) {
                R.drawable.baseline_arrow_up -> {
                    binding.buttonSortTask.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_down,
                            currentTheme
                        )
                    binding.buttonSortTask.setResId(R.drawable.baseline_arrow_down)
                    adapter?.sort(4, false)
                }

                else -> {
                    binding.buttonSortTask.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_up,
                            currentTheme
                        )
                    binding.buttonSortTask.setResId(R.drawable.baseline_arrow_up)
                    adapter?.sort(4, true)
                }
            }
        }

        binding.buttonSortCustomer.setOnClickListener {
            resetSortButtons(5, currentTheme)

            (activity as MainActivity?)?.restartTimer()
            val adapter = binding.viewRecyclerProjectTimeList.adapter as ProjectTimeEntityAdapter?
            when (binding.buttonSortCustomer.getResId()) {
                R.drawable.baseline_arrow_up -> {
                    binding.buttonSortCustomer.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_down,
                            currentTheme
                        )
                    binding.buttonSortCustomer.setResId(R.drawable.baseline_arrow_down)
                    adapter?.sort(5, false)
                }

                else -> {
                    binding.buttonSortCustomer.icon =
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.baseline_arrow_up,
                            currentTheme
                        )
                    binding.buttonSortCustomer.setResId(R.drawable.baseline_arrow_up)
                    adapter?.sort(5, true)
                }
            }
        }
    }

    private fun resetSortButtons(sortId: Int, theme: Resources.Theme) {
        val bgTint = ResourcesCompat.getColorStateList(resources, R.color.md_theme_primary, theme)
        val textColor = ResourcesCompat.getColor(resources, R.color.md_theme_onPrimary, theme)
        val inBgTint =
            ResourcesCompat.getColorStateList(resources, R.color.md_theme_surfaceVariant, theme)
        val inTextColor = ResourcesCompat.getColor(resources, R.color.md_theme_onSurface, theme)

        if (sortId != 1) {
            binding.buttonSortDate.icon = null
            binding.buttonSortDate.setResId(null)
            binding.buttonSortDate.backgroundTintList = inBgTint
            binding.buttonSortDate.setTextColor(inTextColor)
        } else {
            binding.buttonSortDate.backgroundTintList = bgTint
            binding.buttonSortDate.setTextColor(textColor)
        }

        if (sortId != 2) {
            binding.buttonSortEmployee.icon = null
            binding.buttonSortEmployee.setResId(null)
            binding.buttonSortEmployee.backgroundTintList = inBgTint
            binding.buttonSortEmployee.setTextColor(inTextColor)
        } else {
            binding.buttonSortEmployee.backgroundTintList = bgTint
            binding.buttonSortEmployee.setTextColor(textColor)
        }

        if (sortId != 3) {
            binding.buttonSortProject.icon = null
            binding.buttonSortProject.setResId(null)
            binding.buttonSortProject.backgroundTintList = inBgTint
            binding.buttonSortProject.setTextColor(inTextColor)
        } else {
            binding.buttonSortProject.backgroundTintList = bgTint
            binding.buttonSortProject.setTextColor(textColor)
        }

        if (sortId != 4) {
            binding.buttonSortTask.icon = null
            binding.buttonSortTask.setResId(null)
            binding.buttonSortTask.backgroundTintList = inBgTint
            binding.buttonSortTask.setTextColor(inTextColor)
        } else {
            binding.buttonSortTask.backgroundTintList = bgTint
            binding.buttonSortTask.setTextColor(textColor)
        }

        if (sortId != 5) {
            binding.buttonSortCustomer.icon = null
            binding.buttonSortCustomer.setResId(null)
            binding.buttonSortCustomer.backgroundTintList = inBgTint
            binding.buttonSortCustomer.setTextColor(inTextColor)
        } else {
            binding.buttonSortCustomer.backgroundTintList = bgTint
            binding.buttonSortCustomer.setTextColor(textColor)
        }
    }

    private fun setAdapter() {
        adapt = FilterEntityAdapter(
            requireContext(),
            R.layout.dropdown_small,
            languageService.getText("ALLGEMEIN#Datum"),
            languageService.getText("ALLGEMEIN#Mitarbeiter"),
            languageService.getText("ALLGEMEIN#Projekt"),
            languageService.getText("ALLGEMEIN#Vorgang"),
            languageService.getText("ALLGEMEIN#Kunde")
        )
        binding.projectTimeListDropdown.setAdapter(adapt)

        binding.viewRecyclerProjectTimeList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (activity as MainActivity?)?.restartTimer()
                if (dy > 0 && !recyclerView.canScrollVertically(1)) { // Scrolled to bottom
                    projectTimeListFragmentViewModel.loadMoreItems(userId)
                }
            }
        })

        projectTimeListFragmentViewModel.items.observe(viewLifecycleOwner) {
            projectTimeListFragmentViewModel.viewModelScope.launch {
                val userEntities = userRepository.getAllAsList()
                val projectEntities = projectRepository.getAllProjects()
                val taskEntities = taskRepository.getAllTasks()
                val customerEntities = customerRepository.getAllCustomers()
                val customerGroupEntities = customerGroupRepository.getAllCustomerGroups()

                val userMap = HashMap<String, String>()
                val projectMap = HashMap<String, String>()
                val taskMap = HashMap<String, String>()
                val customerMap = HashMap<String, String>()

                for (user in userEntities) {
                    userMap[user.id.toString()] = user.name()
                }
                for (project in projectEntities) {
                    projectMap[project.projectId.toString()] = project.projectName
                }
                for (task in taskEntities) {
                    taskMap[task.taskId.toString()] = task.taskName
                }
                for (customer in customerEntities) {
                    customerMap["kid_${customer.customerId}"] = customer.customerName
                }
                for (customerGroup in customerGroupEntities) {
                    customerMap["kgid_${customerGroup.customerGroupId}"] =
                        customerGroup.customerGroupName
                }

                recyclerViewState =
                    binding.viewRecyclerProjectTimeList.layoutManager?.onSaveInstanceState()
                binding.viewRecyclerProjectTimeList.adapter =
                    ProjectTimeEntityAdapter(
                        it, userMap, projectMap, taskMap, customerMap,
                        object : ProjectTimeEntityAdapter.OnItemClickListener {
                            override fun onItemClick(entity: ProjectTimeEntity) {
                                (activity as MainActivity?)?.restartTimer()
                                val timeEntryType = if (entity.manDays.isNotEmpty()) {
                                    6L
                                } else if (entity.hours.isNotEmpty()) {
                                    3L
                                } else {
                                    1L
                                }
                                val user =
                                    userEntities.find { user -> user.id == entity.userId.toLong() }

                                if (user != null) {
                                    parentFragmentManager.commit {
                                        addToBackStack(null)
                                        replace(
                                            R.id.fragment_container_view,
                                            ProjectFragment.newInstance(
                                                user.id,
                                                user.customerBasedProjectTime,
                                                timeEntryType,
                                                user.crossDay,
                                                entity
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        languageService.getText("CLIENT#CompData_ManDay"),
                        languageService.getText("ALLGEMEIN#Projekt", " ").substring(0, 1),
                        languageService.getText("ALLGEMEIN#Vorgang", " ").substring(0, 1),
                        languageService.getText("ALLGEMEIN#Kunde", " ").substring(0, 1)
                    )
                binding.viewRecyclerProjectTimeList.layoutManager?.onRestoreInstanceState(
                    recyclerViewState
                )
            }
            if (it.isNotEmpty()) {
                binding.viewRecyclerProjectTimeList.visibility = View.VISIBLE
                binding.fragmentProjectTimeListEmptyListTextView.visibility = View.GONE
            }
        }

        projectTimeListFragmentViewModel.loadMoreItems(userId)
    }
}