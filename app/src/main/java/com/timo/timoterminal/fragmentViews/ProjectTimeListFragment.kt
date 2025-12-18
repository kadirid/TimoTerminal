package com.timo.timoterminal.fragmentViews

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
    private var editorId: String? = null
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
    private var first: Boolean = true
    private var currentSort: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectTimeListBinding.inflate(inflater, container, false)

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
        "${languageService.getText("Leistungsnachweis#Sortierung")}:".also { binding.textViewSortLabel.text = it }
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
        binding.buttonSortDate.setOnClickListener {
            sort(1)

            (activity as MainActivity?)?.restartTimer()
        }

        binding.buttonSortEmployee.setOnClickListener {
            sort(2)

            (activity as MainActivity?)?.restartTimer()
        }

        binding.buttonSortProject.setOnClickListener {
            sort(3)

            (activity as MainActivity?)?.restartTimer()
        }

        binding.buttonSortTask.setOnClickListener {
            sort(4)

            (activity as MainActivity?)?.restartTimer()
        }

        binding.buttonSortCustomer.setOnClickListener {
            sort(5)

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
        val adapter = binding.viewRecyclerProjectTimeList.adapter as ProjectTimeEntityAdapter?

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
            binding.buttonSortDate.setIconTintResource( R.color.md_theme_onSurface)
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
            binding.buttonSortEmployee.setIconTintResource( R.color.md_theme_onSurface)
        }

        if (sortId != 3) {
            binding.buttonSortProject.icon = arrowDown?.constantState?.newDrawable()?.mutate()
            binding.buttonSortProject.iconTint = inBgTint
            binding.buttonSortProject.setResId(null)
            binding.buttonSortProject.backgroundTintList = inBgTint
            binding.buttonSortProject.setTextColor(inTextColor)
        } else {
            binding.buttonSortProject.backgroundTintList = bgTint
            binding.buttonSortProject.setTextColor(textColor)
            if (binding.buttonSortProject.getResId() == R.drawable.baseline_arrow_up) {
                binding.buttonSortProject.icon = arrowDown?.constantState?.newDrawable()?.mutate()
                binding.buttonSortProject.setResId(R.drawable.baseline_arrow_down)
                adapter?.sort(3, false)
            } else {
                binding.buttonSortProject.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.baseline_arrow_up,
                        theme
                    )
                binding.buttonSortProject.setResId(R.drawable.baseline_arrow_up)
                adapter?.sort(3, true)
            }
            binding.buttonSortProject.setIconTintResource( R.color.md_theme_onSurface)
        }

        if (sortId != 4) {
            binding.buttonSortTask.icon = arrowDown?.constantState?.newDrawable()?.mutate()
            binding.buttonSortTask.iconTint = inBgTint
            binding.buttonSortTask.setResId(null)
            binding.buttonSortTask.backgroundTintList = inBgTint
            binding.buttonSortTask.setTextColor(inTextColor)
        } else {
            binding.buttonSortTask.backgroundTintList = bgTint
            binding.buttonSortTask.setTextColor(textColor)
            if (binding.buttonSortTask.getResId() == R.drawable.baseline_arrow_up) {
                binding.buttonSortTask.icon = arrowDown?.constantState?.newDrawable()?.mutate()
                binding.buttonSortTask.setResId(R.drawable.baseline_arrow_down)
                adapter?.sort(4, false)
            } else {
                binding.buttonSortTask.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.baseline_arrow_up,
                        theme
                    )
                binding.buttonSortTask.setResId(R.drawable.baseline_arrow_up)
                adapter?.sort(4, true)
            }
            binding.buttonSortTask.setIconTintResource( R.color.md_theme_onSurface)
        }

        if (sortId != 5) {
            binding.buttonSortCustomer.icon = arrowDown?.constantState?.newDrawable()?.mutate()
            binding.buttonSortCustomer.iconTint = inBgTint
            binding.buttonSortCustomer.setResId(null)
            binding.buttonSortCustomer.backgroundTintList = inBgTint
            binding.buttonSortCustomer.setTextColor(inTextColor)
        } else {
            binding.buttonSortCustomer.backgroundTintList = bgTint
            binding.buttonSortCustomer.setTextColor(textColor)
            if (binding.buttonSortCustomer.getResId() == R.drawable.baseline_arrow_up) {
                binding.buttonSortCustomer.icon = arrowDown?.constantState?.newDrawable()?.mutate()
                binding.buttonSortCustomer.setResId(R.drawable.baseline_arrow_down)
                adapter?.sort(5, false)
            } else {
                binding.buttonSortCustomer.icon =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.baseline_arrow_up,
                        theme
                    )
                binding.buttonSortCustomer.setResId(R.drawable.baseline_arrow_up)
                adapter?.sort(5, true)
            }
            binding.buttonSortCustomer.setIconTintResource( R.color.md_theme_onSurface)
        }
    }

    private fun resort(){
        val adapter = binding.viewRecyclerProjectTimeList.adapter as ProjectTimeEntityAdapter?
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
                if (binding.buttonSortProject.getResId() == R.drawable.baseline_arrow_up) {
                    adapter?.sort(3, true)
                } else {
                    adapter?.sort(3, false)
                }
            }
            4 -> {
                if (binding.buttonSortTask.getResId() == R.drawable.baseline_arrow_up) {
                    adapter?.sort(4, true)
                } else {
                    adapter?.sort(4, false)
                }
            }
            5 -> {
                if (binding.buttonSortCustomer.getResId() == R.drawable.baseline_arrow_up) {
                    adapter?.sort(5, true)
                } else {
                    adapter?.sort(5, false)
                }
            }
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
            if (it.isNullOrEmpty()) return@observe
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
                                val timeEntryType = entity.timeEntryType.toLong()
                                val user =
                                    userEntities.find { user -> user.id == entity.userId.toLong() }

                                if (user != null) {
                                    parentFragmentManager.commit {
                                        addToBackStack(null)
                                        replace(
                                            R.id.fragment_container_view,
                                            ProjectFragment.newInstance(
                                                editorId ?: user.id.toString(),
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
                if (it.isNotEmpty()) {
                    binding.viewRecyclerProjectTimeList.visibility = View.VISIBLE
                    binding.fragmentProjectTimeListEmptyListTextView.visibility = View.GONE
                }
                resort()
            }
            if(first) {
                first = false
                sort(1)
            }
        }

        projectTimeListFragmentViewModel.loadMoreItems(userId)
    }
}