import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.asciidoctor.jvm.convert") version "4.0.2"
    id("com.diffplug.spotless") version "6.25.0"
    id("jacoco")
}

group = property("projectGroup") as String
version = property("applicationVersion") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(property("javaVersion") as String))
    }
}




configurations {
    compileOnly {
        extendsFrom(annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${property("jwtTokenVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${property("jwtTokenVersion")}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${property("jwtTokenVersion")}")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    // Map Struct
    implementation("org.mapstruct:mapstruct:1.5.5.Final") // 최신 안정 버전
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    // Spring Boot + MapStruct 통합 시 필요
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // P6Spy
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:${property("p6spyVersion")}")

    // QueryDSL (with Jakarta API)
    implementation("com.querydsl:querydsl-core:5.1.0")
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")

    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api:2.1.1")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-inline:5.2.0")   // final/record 목킹용
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:${property("testContainersVersion")}")
    testImplementation("org.flywaydb.flyway-test-extensions:flyway-spring-test:${property("flywayTestExtensionVersion")}")
}

extensions.configure<JacocoPluginExtension>("jacoco") {
    toolVersion = "0.8.10"
}



tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport") // 테스트 끝나면 커버리지 리포트 생성
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    classDirectories.setFrom(
        fileTree("${buildDir}/classes/java/main") {
            exclude(
                "**/config/**",
                "**/dto/**",
                "**/entity/**",
                "**/exception/**"
            )
        }
    )

    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(buildDir).include("jacoco/test.exec"))
}

// Spotless
spotless {
    java {
        googleJavaFormat().aosp()
        removeUnusedImports()
        importOrder()
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("misc") {
        target("*.gradle.kts", ".gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.named("compileJava") {
    dependsOn("spotlessApply")
}

tasks.named("build") {
    dependsOn("spotlessApply")
}

// REST Docs
val snippetsDir = layout.buildDirectory.dir("generated-snippets")
val asciidoctorExt by configurations.creating

dependencies {
    asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }
    outputs.dir(snippetsDir)
}

tasks.named("asciidoctor") {
    dependsOn(tasks.named("test"))
    inputs.dir(snippetsDir)
    doFirst {
        delete("src/main/resources/static/docs")
    }
    doLast {
        copy {
            from("build/docs/asciidoc")
            into("src/main/resources/static/docs")
        }
    }
}

tasks.named("build") {
    dependsOn("asciidoctor")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<BootJar>("bootJar") {
    archiveBaseName.set("DevnogiServer")
    archiveFileName.set("DevnogiServer.jar")
}
