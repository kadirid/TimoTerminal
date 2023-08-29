val maven_base_url = "http://server.zkteco.eu:8081"
val maven_url_repository_public = "$maven_base_url/repository/public"


pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io" ) }
        maven {
            url = uri(maven_url_repository_public)
            isAllowInsecureProtocol = true
            metadataSources {
                mavenPom()
                artifact()
            }
        }
    }
}

rootProject.name = "TimoTerminal"
include(":app")
