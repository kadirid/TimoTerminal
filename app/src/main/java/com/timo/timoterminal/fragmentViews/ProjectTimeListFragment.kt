package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentProjectTimeListBinding
import com.timo.timoterminal.entityAdaptor.ProjectTimeEntityAdapter
import com.timo.timoterminal.entityClasses.ProjectTimeEntity
import com.timo.timoterminal.repositories.CustomerGroupRepository
import com.timo.timoterminal.repositories.CustomerRepository
import com.timo.timoterminal.repositories.ProjectRepository
import com.timo.timoterminal.repositories.ProjectTimeRepository
import com.timo.timoterminal.repositories.TaskRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.viewModel.ProjectTimeListFragmentViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ProjectTimeListFragment : Fragment() {
    private lateinit var binding: FragmentProjectTimeListBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectTimeListBinding.inflate(inflater, container, false)
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
            languageService.getText("#NoEntriesForProjectTimes")
        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.radioFilter.setOnClickListener {
            binding.buttonSearch.visibility = View.GONE
        }
        binding.radioSort.setOnClickListener {
            binding.buttonSearch.visibility = View.VISIBLE
        }
    }

    private fun setAdapter() {
        binding.viewRecyclerProjectTimeList.adapter = ProjectTimeEntityAdapter(
            emptyList(),
            emptyMap(),
            emptyMap(),
            emptyMap(),
            emptyMap(),
            object : ProjectTimeEntityAdapter.OnItemClickListener {
                override fun onItemClick(entity: ProjectTimeEntity) {}
            },
            languageService.getText("CLIENT#CompData_ManDay"),
            languageService.getText("ALLGEMEIN#Projekt", " ").substring(0, 1),
            languageService.getText("ALLGEMEIN#Vorgang", " ").substring(0, 1),
            languageService.getText("ALLGEMEIN#Kunde", " ").substring(0, 1)
        )

        binding.viewRecyclerProjectTimeList.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (activity as MainActivity?)?.restartTimer()
                if (dy > 0 && !recyclerView.canScrollVertically(1)) { // Scrolled to bottom
                    projectTimeListFragmentViewModel.loadMoreItems()
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

        projectTimeListFragmentViewModel.loadMoreItems()
    }
}