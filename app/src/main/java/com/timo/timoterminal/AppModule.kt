package com.timo.timoterminal

import androidx.room.Room
import com.timo.timoterminal.database.ActivityTypeDatabase
import com.timo.timoterminal.database.ActivityTypeMatrixDatabase
import com.timo.timoterminal.database.BookingBUDatabase
import com.timo.timoterminal.database.BookingDatabase
import com.timo.timoterminal.database.ConfigDatabase
import com.timo.timoterminal.database.Customer2ProjectDatabase
import com.timo.timoterminal.database.Customer2TaskDatabase
import com.timo.timoterminal.database.CustomerDatabase
import com.timo.timoterminal.database.CustomerGroupDatabase
import com.timo.timoterminal.database.JourneyDatabase
import com.timo.timoterminal.database.LanguageDatabase
import com.timo.timoterminal.database.ProjectDatabase
import com.timo.timoterminal.database.ProjectTimeDatabase
import com.timo.timoterminal.database.SkillDatabase
import com.timo.timoterminal.database.TaskDatabase
import com.timo.timoterminal.database.TeamDatabase
import com.timo.timoterminal.database.TicketDatabase
import com.timo.timoterminal.database.User2TaskDatabase
import com.timo.timoterminal.database.UserDatabase
import com.timo.timoterminal.migration.ProjectTimeMigration
import com.timo.timoterminal.migration.UserMigration
import com.timo.timoterminal.repositories.ActivityTypeMatrixRepository
import com.timo.timoterminal.repositories.ActivityTypeRepository
import com.timo.timoterminal.repositories.BookingBURepository
import com.timo.timoterminal.repositories.BookingRepository
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.repositories.Customer2ProjectRepository
import com.timo.timoterminal.repositories.Customer2TaskRepository
import com.timo.timoterminal.repositories.CustomerGroupRepository
import com.timo.timoterminal.repositories.CustomerRepository
import com.timo.timoterminal.repositories.JourneyRepository
import com.timo.timoterminal.repositories.LanguageRepository
import com.timo.timoterminal.repositories.ProjectRepository
import com.timo.timoterminal.repositories.ProjectTimeRepository
import com.timo.timoterminal.repositories.SkillRepository
import com.timo.timoterminal.repositories.TaskRepository
import com.timo.timoterminal.repositories.TeamRepository
import com.timo.timoterminal.repositories.TicketRepository
import com.timo.timoterminal.repositories.User2TaskRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.BookingService
import com.timo.timoterminal.service.HeartbeatService
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.ProjectPrefService
import com.timo.timoterminal.service.ProjectService
import com.timo.timoterminal.service.ProjectTimeService
import com.timo.timoterminal.service.PropertyService
import com.timo.timoterminal.service.SettingsService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.service.WebSocketService
import com.timo.timoterminal.service.WorkerService
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.viewModel.AttendanceFragmentViewModel
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
import com.timo.timoterminal.viewModel.InternalTerminalSettingsFragmentViewModel
import com.timo.timoterminal.viewModel.LoginActivityViewModel
import com.timo.timoterminal.viewModel.LoginFragmentViewModel
import com.timo.timoterminal.viewModel.MBRemoteRegisterSheetViewModel
import com.timo.timoterminal.viewModel.MBSheetFingerprintCardReaderViewModel
import com.timo.timoterminal.viewModel.MBUserWaitSheetViewModel
import com.timo.timoterminal.viewModel.MainActivityViewModel
import com.timo.timoterminal.viewModel.ProjectFragmentViewModel
import com.timo.timoterminal.viewModel.SettingsFragmentViewModel
import com.timo.timoterminal.viewModel.UserSettingsFragmentViewModel
import com.timo.timoterminal.worker.HeartbeatWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.named
import org.koin.dsl.module

var appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            UserDatabase::class.java,
            "user_entity"
        )
            .addMigrations(UserMigration.MIGRATION_1_2)
            .build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            ConfigDatabase::class.java,
            "config_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            LanguageDatabase::class.java,
            "language_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            BookingDatabase::class.java,
            "booking_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            BookingBUDatabase::class.java,
            "booking_bu_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            ProjectDatabase::class.java,
            "project_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            TaskDatabase::class.java,
            "task_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            User2TaskDatabase::class.java,
            "user2task_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            CustomerDatabase::class.java,
            "customer_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            CustomerGroupDatabase::class.java,
            "customer_group_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            Customer2ProjectDatabase::class.java,
            "customer_2_project_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            Customer2TaskDatabase::class.java,
            "customer_2_task_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            ActivityTypeDatabase::class.java,
            "activity_type_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            ActivityTypeMatrixDatabase::class.java,
            "activity_type_matrix_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            TicketDatabase::class.java,
            "ticket_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            TeamDatabase::class.java,
            "team_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            SkillDatabase::class.java,
            "skill_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            JourneyDatabase::class.java,
            "journey_entity"
        ).build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            ProjectTimeDatabase::class.java,
            "project_time_entity"
        )
            .addMigrations(ProjectTimeMigration.MIGRATION_1_2)
            .build()
    }


    single { get<UserDatabase>().userDao() }
    single { get<ConfigDatabase>().configDao() }
    single { get<LanguageDatabase>().languageDao() }
    single { get<BookingDatabase>().bookingDao() }
    single { get<BookingBUDatabase>().bookingBUDao() }
    single { get<ProjectDatabase>().projectDao() }
    single { get<TaskDatabase>().taskDao() }
    single { get<User2TaskDatabase>().user2TaskDao() }
    single { get<CustomerDatabase>().customerDao() }
    single { get<CustomerGroupDatabase>().customerGroupDao() }
    single { get<Customer2ProjectDatabase>().customer2ProjectDao() }
    single { get<Customer2TaskDatabase>().customer2TaskDao() }
    single { get<ActivityTypeDatabase>().activityTypeDao() }
    single { get<ActivityTypeMatrixDatabase>().activityTypeMatrixDao() }
    single { get<TicketDatabase>().ticketDao() }
    single { get<TeamDatabase>().teamDao() }
    single { get<SkillDatabase>().skillDao() }
    single { get<JourneyDatabase>().journeyDao() }
    single { get<ProjectTimeDatabase>().projectTimeDao() }


    single { UserRepository(get()) }
    single { ConfigRepository(get()) }
    single { LanguageRepository(get()) }
    single { BookingRepository(get()) }
    single { BookingBURepository(get()) }
    single { ProjectRepository(get()) }
    single { TaskRepository(get()) }
    single { User2TaskRepository(get()) }
    single { CustomerRepository(get()) }
    single { CustomerGroupRepository(get()) }
    single { Customer2ProjectRepository(get()) }
    single { Customer2TaskRepository(get()) }
    single { ActivityTypeRepository(get()) }
    single { ActivityTypeMatrixRepository(get()) }
    single { TicketRepository(get()) }
    single { TeamRepository(get()) }
    single { SkillRepository(get()) }
    single { JourneyRepository(get()) }
    single { ProjectTimeRepository(get()) }


    single { HttpService() }
    single { WebSocketService() }
    single { HeartbeatService() }
    single { PropertyService(androidContext()) }
    single { SharedPrefService(androidContext()) }
    single { ProjectPrefService(get(),androidContext()) }
    single { WorkerService(get()) }
    single { SoundSource(get(), androidContext()) }
    single { ProjectService(get(), get()) }
    single { SettingsService(get(), get()) }
    single { UserService(get(), get(), get()) }
    single { LanguageService(get(), get(), get()) }
    single { BookingService(get(), get(), get(), get()) }
    single { ProjectTimeService(get(), get(), get(), get()) }
    single { LoginService(get(), get(), get(), get(), get(), get(), get()) }

    viewModel { LoginActivityViewModel(get(), get(), get(), get()) }
    viewModel { LoginFragmentViewModel(get(), get(), get(), get()) }
    viewModel { MBUserWaitSheetViewModel(get(), get(), get(), get()) }
    viewModel { SettingsFragmentViewModel(get(), get(), get(), get()) }
    viewModel { InternalTerminalSettingsFragmentViewModel(get(), get()) }
    viewModel { AttendanceFragmentViewModel(get(), get(), get(), get()) }
    viewModel { InfoFragmentViewModel(get(), get(), get(), get(), get()) }
    viewModel { MainActivityViewModel(get(), get(), get(), get(), get(), get()); }
    viewModel { UserSettingsFragmentViewModel(get(), get(), get(), get()) }
    viewModel { MBRemoteRegisterSheetViewModel(get(), get(), get(), get()) }
    viewModel { MBSheetFingerprintCardReaderViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel {
        ProjectFragmentViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    worker(named<HeartbeatWorker>()) {
        HeartbeatWorker(
            context = androidContext(),
            workerParameters = get()
        )
    }

}