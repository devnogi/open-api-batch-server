rootProject.name = "devnogi-server"

pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val asciidoctorJvmConvertPluginVersion: String by settings

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useVersion(springBootVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
                "org.asciidoctor.jvm.convert" -> useVersion(asciidoctorJvmConvertPluginVersion)
            }
        }
    }
}
