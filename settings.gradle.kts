pluginManagement {
    includeBuild("build-logic")
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
    }
}

rootProject.name = "TeamCompass"
include(":app")
include(":core")
include(":core:common")
include(":core:model")
include(":core:auth")
include(":core:realtime")
include(":core:telemetry")
include(":core:tactical")
include(":core:data")
include(":core:testing")
include(":core:designsystem")
include(":core:ui")
include(":core:datastore")
include(":core:network")
include(":core:database")
include(":feature:nav")
include(":feature:onboarding:api")
include(":feature:onboarding:impl")
include(":feature:auth:api")
include(":feature:auth:impl")
include(":feature:chats:api")
include(":feature:chats:impl")
include(":feature:teams:api")
include(":feature:teams:impl")
include(":feature:events:api")
include(":feature:events:impl")
include(":feature:marketplace:api")
include(":feature:marketplace:impl")
include(":feature:profile:api")
include(":feature:profile:impl")
include(":feature:tactical:api")
include(":feature:tactical:impl")
include(":infra:firebase")
