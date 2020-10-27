import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    application
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("org.asciidoctor.convert") version "1.5.8"
    kotlin("jvm") version "1.4.0"
    kotlin("plugin.jpa") version "1.4.0"
    kotlin("plugin.spring") version "1.4.0"
    kotlin("kapt") version "1.4.0"
}

group = "com.houkunlin.sms"
version = "0.0.1-SNAPSHOT"

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

kapt {
    useBuildCache = false
    correctErrorTypes = true

    javacOptions {
        option("-Xjsr305", "strict")
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    maven("https://repo1.maven.org/maven2")
    maven { setUrl("https://repo.typesafe.com/typesafe/maven-releases/") }
    mavenCentral()
    jcenter()
}

extra["snippetsDir"] = file("build/generated-snippets")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-freemarker")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")



    implementation("joda-time:joda-time:+")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:+")
    implementation("com.github.ben-manes.caffeine:caffeine")


    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("mysql:mysql-connector-java")
    runtimeOnly("org.xerial:sqlite-jdbc")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs = listOf("-Xjsr305=strict")
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

val snippetsDir = "build/snippetsDir"
tasks.test {
    outputs.dir(snippetsDir)
}

tasks.asciidoctor {
    inputs.dir(snippetsDir)
    dependsOn("test")
}
