// Repositorios utilizados por Gradle para resolver plugins.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Centraliza los repositorios de dependencias de todos los módulos.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Nombre del proyecto y módulo principal.
rootProject.name = "EcoResiduos"
include(":app")
