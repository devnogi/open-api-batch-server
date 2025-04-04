import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.asciidoctor.jvm.convert")
    id("com.diffplug.spotless")
}

group = "${property("projectGroup")}"
version = "${property("applicationVersion")}"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of("${property("javaVersion")}")
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Spring Web View (Thymeleaf)
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

    // Data
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${property("jwtTokenVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jwtTokenVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jwtTokenVersion")}")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("swaggerVersion")}")

    // p6spy
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:${property("p6spyVersion")}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    // REST Docs
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

    // TestContainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:${property("testContainersVersion")}")
    testImplementation("org.flywaydb.flyway-test-extensions:flyway-spring-test:${property("flywayTestExtensionVersion")}")
}

spotless {
    java {
        googleJavaFormat().aosp()
        removeUnusedImports()
        importOrder()
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }

    format("misc") {
        target("*.gradle.kts", ".gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
}

tasks.named("compileJava") {
    dependsOn("spotlessApply")
}

tasks.named("build") {
    dependsOn("spotlessApply")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// REST Docs Snippets
val asciidoctorExt: Configuration by configurations.creating
dependencies {
    asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
}

val snippetsDir: File by extra { file("build/generated-snippets") }
tasks {
    test {
        outputs.dir(snippetsDir)
    }

    asciidoctor {
        configurations(asciidoctorExt.name)
        dependsOn(test)

        doFirst {
            delete(file("src/main/resources/static/docs"))
        }

        inputs.dir(snippetsDir)

        doLast {
            copy {
                from("build/docs/asciidoc")
                into("src/main/resources/static/docs")
            }
        }
    }

    build {
        dependsOn(asciidoctor)
    }
}

// jar & bootJar
tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName = "DevnogiServer"
    archiveFileName = "DevnogiServer.jar"
}
