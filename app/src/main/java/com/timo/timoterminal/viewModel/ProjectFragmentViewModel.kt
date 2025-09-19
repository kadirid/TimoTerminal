package com.timo.timoterminal.viewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityAdaptor.CustomerEntityAdapter
import com.timo.timoterminal.entityClasses.ActivityTypeEntity
import com.timo.timoterminal.entityClasses.ActivityTypeMatrixEntity
import com.timo.timoterminal.entityClasses.Customer2ProjectEntity
import com.timo.timoterminal.entityClasses.Customer2TaskEntity
import com.timo.timoterminal.entityClasses.CustomerEntity
import com.timo.timoterminal.entityClasses.CustomerGroupEntity
import com.timo.timoterminal.entityClasses.JourneyEntity
import com.timo.timoterminal.entityClasses.ProjectEntity
import com.timo.timoterminal.entityClasses.ProjectTimeTrackSetting
import com.timo.timoterminal.entityClasses.SkillEntity
import com.timo.timoterminal.entityClasses.TaskEntity
import com.timo.timoterminal.entityClasses.TeamEntity
import com.timo.timoterminal.entityClasses.TicketEntity
import com.timo.timoterminal.entityClasses.User2TaskEntity
import com.timo.timoterminal.repositories.ActivityTypeMatrixRepository
import com.timo.timoterminal.repositories.ActivityTypeRepository
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.repositories.Customer2ProjectRepository
import com.timo.timoterminal.repositories.Customer2TaskRepository
import com.timo.timoterminal.repositories.CustomerGroupRepository
import com.timo.timoterminal.repositories.CustomerRepository
import com.timo.timoterminal.repositories.JourneyRepository
import com.timo.timoterminal.repositories.ProjectRepository
import com.timo.timoterminal.repositories.SkillRepository
import com.timo.timoterminal.repositories.TaskRepository
import com.timo.timoterminal.repositories.TeamRepository
import com.timo.timoterminal.repositories.TicketRepository
import com.timo.timoterminal.repositories.User2TaskRepository
import com.timo.timoterminal.service.ProjectPrefService
import com.timo.timoterminal.service.ProjectService
import com.timo.timoterminal.service.ProjectTimeService
import com.timo.timoterminal.service.SettingsService
import com.timo.timoterminal.utils.Utils
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.GregorianCalendar

class ProjectFragmentViewModel(
    private val projectService: ProjectService,
    private val settingsService: SettingsService,
    private val projectRepository: ProjectRepository,
    private val taskRepository: TaskRepository,
    private val user2TaskRepository: User2TaskRepository,
    private val customerRepository: CustomerRepository,
    private val customerGroupRepository: CustomerGroupRepository,
    private val customer2ProjectRepository: Customer2ProjectRepository,
    private val customer2TaskRepository: Customer2TaskRepository,
    private val configRepository: ConfigRepository,
    private val projectPrefService: ProjectPrefService,
    private val activityTypeRepository: ActivityTypeRepository,
    private val activityTypeMatrixRepository: ActivityTypeMatrixRepository,
    private val ticketRepository: TicketRepository,
    private val teamRepository: TeamRepository,
    private val skillRepository: SkillRepository,
    private val journeyRepository: JourneyRepository,
    private val projectTimeService: ProjectTimeService
) : ViewModel() {
    val liveHideMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowOfflineNotice: MutableLiveData<Boolean> = MutableLiveData()
    val liveProjectEntities: MutableLiveData<List<ProjectEntity>> = MutableLiveData()
    val liveTaskEntities: MutableLiveData<List<TaskEntity>> = MutableLiveData()
    val liveCustomerEntities: MutableLiveData<List<CustomerEntityAdapter.CustomerComboEntity>> =
        MutableLiveData()
    val liveProjectTimeTrackSetting: MutableLiveData<ProjectTimeTrackSetting> = MutableLiveData()
    val liveActivityTypeEntities: MutableLiveData<List<ActivityTypeEntity>> = MutableLiveData()
    val liveSkillEntities: MutableLiveData<List<SkillEntity>> = MutableLiveData()
    val liveTeamEntities: MutableLiveData<List<TeamEntity>> = MutableLiveData()
    val liveJourneyEntities: MutableLiveData<List<JourneyEntity>> = MutableLiveData()
    val liveMessage: MutableLiveData<String> = MutableLiveData()

    var userId: Long = -1L
    var isCustomerTimeTrack: Boolean = false
    private var repoLoad: Long = 0L

    private var projectEntities: List<ProjectEntity> = emptyList()
    private var taskEntities: List<TaskEntity> = emptyList()
    private var customerEntities: List<CustomerEntity> = emptyList()
    private var customerGroupEntities: List<CustomerGroupEntity> = emptyList()
    private var customer2ProjectEntities: List<Customer2ProjectEntity> = emptyList()
    private var customer2TaskEntities: List<Customer2TaskEntity> = emptyList()
    private var user2TaskEntities: List<User2TaskEntity> = emptyList()
    private var activityTypeEntities: List<ActivityTypeEntity> = emptyList()
    private var activityTypeMatrixEntities: List<ActivityTypeMatrixEntity> = emptyList()
    private var ticketEntities: List<TicketEntity> = emptyList()
    private var teamEntities: List<TeamEntity> = emptyList()
    private var skillEntities: List<SkillEntity> = emptyList()
    private var journeyEntities: List<JourneyEntity> = emptyList()

    fun loadForProjectTimeTrack() {
        liveShowMask.postValue(true)
        repoLoad = 0
        projectService.getValuesForProjectTimeTrack { success, obj ->
            if (success) {
                // Do stuff to convert obj to values and save them async
                projectEntities = ProjectEntity.parseFromJson(obj ?: JSONObject())
                taskEntities = TaskEntity.parseFromJson(obj ?: JSONObject())
                customerEntities = CustomerEntity.parseFromJson(obj ?: JSONObject())
                customerGroupEntities = CustomerGroupEntity.parseFromJson(obj ?: JSONObject())
                customer2ProjectEntities = Customer2ProjectEntity.parseFromJson(obj ?: JSONObject())
                customer2TaskEntities = Customer2TaskEntity.parseFromJson(obj ?: JSONObject())
                user2TaskEntities = User2TaskEntity.parseFromJson(obj ?: JSONObject())
                activityTypeEntities = ActivityTypeEntity.parseFromJson(obj ?: JSONObject())
                activityTypeMatrixEntities =
                    ActivityTypeMatrixEntity.parseFromJson(obj ?: JSONObject())
                ticketEntities = TicketEntity.parseFromJson(obj ?: JSONObject())
                teamEntities = TeamEntity.parseFromJson(obj ?: JSONObject())
                skillEntities = SkillEntity.parseFromJson(obj ?: JSONObject())

                if (isCustomerTimeTrack) {
                    val customersForTask =
                        mutableListOf<CustomerEntityAdapter.CustomerComboEntity>()
                    val forProject = mutableListOf<Customer2ProjectEntity>()
                    user2TaskEntities.forEach { u2T ->
                        if (u2T.userId == userId && u2T.projectId >= 0) {
                            forProject.addAll(customer2ProjectEntities.filter { it.projectId == u2T.projectId })
                        }
                    }
                    customerEntities.forEach { customer ->
                        if (forProject.any { it.customerId == customer.customerId }) {
                            customersForTask.add(
                                CustomerEntityAdapter.CustomerComboEntity(
                                    "kid_${customer.customerId}",
                                    customer.customerName
                                )
                            )
                        }
                    }
                    customerGroupEntities.forEach { group ->
                        if (forProject.any { it.groupId == group.customerGroupId }) {
                            customersForTask.add(
                                CustomerEntityAdapter.CustomerComboEntity(
                                    "kgid_${group.customerGroupId}",
                                    "[${group.customerGroupName}]"
                                )
                            )
                        }
                    }
                    liveCustomerEntities.postValue(customersForTask)
                } else {
                    liveProjectEntities.postValue(
                        projectEntities.filter { project ->
                            user2TaskEntities.any { u2T ->
                                u2T.userId == userId && u2T.projectId == project.projectId
                            }
                        }
                    )
                }
                liveActivityTypeEntities.postValue(activityTypeEntities)
                liveSkillEntities.postValue(skillEntities)
                liveTeamEntities.postValue(teamEntities)
                viewModelScope.launch {
                    projectRepository.insertAllProjects(projectEntities)
                }
                viewModelScope.launch {
                    taskRepository.insertAllTasks(taskEntities)
                }
                viewModelScope.launch {
                    user2TaskRepository.insertAllUser2Tasks(user2TaskEntities)
                }
                viewModelScope.launch {
                    customerRepository.insertAllCustomers(customerEntities)
                }
                viewModelScope.launch {
                    customerGroupRepository.insertAllCustomerGroups(customerGroupEntities)
                }
                viewModelScope.launch {
                    customer2ProjectRepository.insertAllCustomer2Projects(customer2ProjectEntities)
                }
                viewModelScope.launch {
                    customer2TaskRepository.insertAllCustomer2Tasks(customer2TaskEntities)
                }
                viewModelScope.launch {
                    activityTypeRepository.insertAllActivityTypes(activityTypeEntities)
                }
                viewModelScope.launch {
                    activityTypeMatrixRepository.insertAllActivityTypeMatrices(
                        activityTypeMatrixEntities
                    )
                }
                viewModelScope.launch {
                    ticketRepository.insertAllTickets(ticketEntities)
                }
                viewModelScope.launch {
                    teamRepository.insertAllTeams(teamEntities)
                }
                viewModelScope.launch {
                    skillRepository.insertAllSkills(skillEntities)
                }
                liveHideMask.postValue(true)
            } else {
                liveHideMask.postValue(true)
                liveShowOfflineNotice.postValue(true)
                viewModelScope.launch {
                    projectEntities = projectRepository.getAllProjects()
                    if (!isCustomerTimeTrack)
                        onRepoLoaded()
                }
                viewModelScope.launch {
                    taskEntities = taskRepository.getAllTasks()
                }
                viewModelScope.launch {
                    user2TaskEntities = user2TaskRepository.getAllUser2Tasks()
                    onRepoLoaded()
                }
                viewModelScope.launch {
                    customerEntities = customerRepository.getAllCustomers()
                    if (isCustomerTimeTrack)
                        onRepoLoaded()
                }
                viewModelScope.launch {
                    customerGroupEntities = customerGroupRepository.getAllCustomerGroups()
                    if (isCustomerTimeTrack)
                        onRepoLoaded()
                }
                viewModelScope.launch {
                    customer2ProjectEntities = customer2ProjectRepository.getAllCustomer2Projects()
                    if (isCustomerTimeTrack)
                        onRepoLoaded()
                }
                viewModelScope.launch {
                    customer2TaskEntities = customer2TaskRepository.getAllCustomer2Tasks()
                }
                viewModelScope.launch {
                    activityTypeEntities = activityTypeRepository.getAllActivityTypes()
                    liveActivityTypeEntities.postValue(activityTypeEntities)
                }
                viewModelScope.launch {
                    activityTypeMatrixEntities =
                        activityTypeMatrixRepository.getAllActivityTypeMatrices()
                }
                viewModelScope.launch {
                    ticketEntities = ticketRepository.getAllTickets()
                }
                viewModelScope.launch {
                    teamEntities = teamRepository.getAllTeams()
                    liveTeamEntities.postValue(teamEntities)
                }
                viewModelScope.launch {
                    skillEntities = skillRepository.getAllSkills()
                    liveSkillEntities.postValue(skillEntities)
                }
            }
        }

        val setting = projectPrefService.getProjectTimeTrackSetting(false)
        liveProjectTimeTrackSetting.postValue(setting)

        getJourneys(Utils.getDateFromGC(Utils.getCal()))
    }

    fun showFilteredProjectsForCustomer(customerId: String) {
        val isCustomer = customerId.startsWith("kid_")
        val id = if (isCustomer) {
            customerId.removePrefix("kid_").toLong()
        } else {
            customerId.removePrefix("kgid_").toLong()
        }
        liveProjectEntities.postValue(
            projectEntities.filter { project ->
                user2TaskEntities.any { u2T ->
                    u2T.userId == userId && u2T.projectId == project.projectId
                } && ((isCustomer && customer2ProjectEntities.any { c2P ->
                    c2P.customerId == id && c2P.projectId == project.projectId
                }) || (!isCustomer && customer2ProjectEntities.any { c2P ->
                    c2P.groupId == id && c2P.projectId == project.projectId
                }))
            })
    }

    fun showFilteredTasksForProject(projectId: Long) {
        liveTaskEntities.postValue(
            taskEntities.filter { task ->
                task.projectId == projectId && user2TaskEntities.any { u2T ->
                    u2T.userId == userId && u2T.taskId == task.taskId
                }
            })
    }

    fun showCustomers(task: TaskEntity) {
        if (isCustomerTimeTrack) {
            return
        }
        val projectId = task.projectId
        val forTask = customer2TaskEntities.filter { it.taskId == task.taskId }
        val forProject = customer2ProjectEntities.filter { it.projectId == projectId }

        val customersForTask = mutableListOf<CustomerEntityAdapter.CustomerComboEntity>()
        if (forTask.isEmpty()) {
            customerEntities.forEach { customer ->
                if (forProject.any { it.customerId == customer.customerId }) {
                    customersForTask.add(
                        CustomerEntityAdapter.CustomerComboEntity(
                            "kid_${customer.customerId}",
                            customer.customerName
                        )
                    )
                }
            }
            customerGroupEntities.forEach { group ->
                if (forProject.any { it.groupId == group.customerGroupId }) {
                    customersForTask.add(
                        CustomerEntityAdapter.CustomerComboEntity(
                            "kgid_${group.customerGroupId}",
                            "[${group.customerGroupName}]"
                        )
                    )
                }
            }
        } else {
            customerEntities.forEach { customer ->
                if (forTask.any { it.customerId == customer.customerId }) {
                    customersForTask.add(
                        CustomerEntityAdapter.CustomerComboEntity(
                            "kid_${customer.customerId}",
                            customer.customerName
                        )
                    )
                }
            }
            customerGroupEntities.forEach { group ->
                if (forTask.any { it.groupId == group.customerGroupId }) {
                    customersForTask.add(
                        CustomerEntityAdapter.CustomerComboEntity(
                            "kgid_${group.customerGroupId}",
                            "[${group.customerGroupName}]"
                        )
                    )
                }
            }
        }
        liveCustomerEntities.postValue(customersForTask)
    }

    fun saveProjectTime(data: HashMap<String, String>, context: Context) {
        liveShowMask.postValue(true)
        projectTimeService.saveProjectTime(data, context, viewModelScope, liveHideMask, liveMessage)
    }

    suspend fun permission(name: String): String {
        return configRepository.getPermissionValue(name)
    }

    private fun onRepoLoaded() {
        repoLoad++
        if (!isCustomerTimeTrack) {
            if (repoLoad == 2L) {
                liveProjectEntities.postValue(
                    projectEntities.filter { project ->
                        user2TaskEntities.any { u2T ->
                            u2T.userId == userId && u2T.projectId == project.projectId
                        }
                    }
                )
                liveHideMask.postValue(true)
            }
        } else {
            if (repoLoad == 4L) {
                val customersForTask =
                    mutableListOf<CustomerEntityAdapter.CustomerComboEntity>()
                val forProject = mutableListOf<Customer2ProjectEntity>()
                user2TaskEntities.forEach { u2T ->
                    if (u2T.userId == userId && u2T.projectId >= 0) {
                        forProject.addAll(customer2ProjectEntities.filter { it.projectId == u2T.projectId })
                    }
                }
                customerEntities.forEach { customer ->
                    if (forProject.any { it.customerId == customer.customerId }) {
                        customersForTask.add(
                            CustomerEntityAdapter.CustomerComboEntity(
                                "kid_${customer.customerId}",
                                customer.customerName
                            )
                        )
                    }
                }
                customerGroupEntities.forEach { group ->
                    if (forProject.any { it.groupId == group.customerGroupId }) {
                        customersForTask.add(
                            CustomerEntityAdapter.CustomerComboEntity(
                                "kgid_${group.customerGroupId}",
                                "[${group.customerGroupName}]"
                            )
                        )
                    }
                }
                liveCustomerEntities.postValue(customersForTask)
                liveHideMask.postValue(true)
            }
        }
    }

    fun getActivityTypeId(taskId: Long): String {
        val user2Task = user2TaskEntities.firstOrNull { it.userId == userId && it.taskId == taskId }
        if (user2Task?.activityType != null && user2Task.activityType!! > 0L
            && activityTypeEntities.firstOrNull { it.activityTypeId == user2Task.activityType } != null
        ) {
            return activityTypeEntities.first { it.activityTypeId == user2Task.activityType }.activityTypeName
        }

        val task = taskEntities.firstOrNull { it.taskId == taskId }
        if (task?.activityType != null && task.activityType > 0L
            && activityTypeEntities.firstOrNull { it.activityTypeId == task.activityType } != null
        ) {
            return activityTypeEntities.first { it.activityTypeId == task.activityType }.activityTypeName
        }
        return ""
    }

    fun getActivityTypeMatrixId(activityId: Long): List<ActivityTypeMatrixEntity> {
        return activityTypeMatrixEntities.filter {
            it.activityTypeMatrixActivityTypeId == activityId || it.activityTypeMatrixActivityTypeId == -1L
        }
    }

    fun getTickets(taskId: Long): List<TicketEntity> {
        return ticketEntities.filter { it.taskId == taskId || it.taskId == -1L }
    }

    fun getJourneys(gc: String) {
        projectService.getJourneysForProjectTimeTrack(userId.toInt(), gc) { success, obj ->
            if (success) {
                if (obj == null) {
                    return@getJourneysForProjectTimeTrack
                }
                journeyEntities = JourneyEntity.parseFromJson(obj)
                viewModelScope.launch {
                    journeyRepository.insertAllJourneys(journeyEntities)
                }
                liveJourneyEntities.postValue(journeyEntities)
            } else {
                val date = Utils.parseGCFromDate(gc)
                val start = date.clone() as GregorianCalendar
                val end = date.clone() as GregorianCalendar
                start.add(GregorianCalendar.DAY_OF_MONTH, -7)
                end.add(GregorianCalendar.DAY_OF_MONTH, 7)
                viewModelScope.launch {
                    journeyEntities = journeyRepository.getJourneysByDateRange(
                        start.time.time,
                        end.time.time,
                        userId.toInt()
                    )
                    liveJourneyEntities.postValue(journeyEntities)
                }
            }
        }
    }
}