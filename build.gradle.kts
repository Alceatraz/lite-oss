val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val enableStatic: String by project

plugins {
  kotlin("jvm") version "1.9.24"
  id("io.ktor.plugin") version "2.3.11"
  id("org.graalvm.buildtools.native") version "0.9.28"
}

group = "top.btswork"
version = "1.0.1-SNAPSHOT"

application {
  mainClass.set("top.btswork.liteoss.ApplicationKt")
}

repositories {
  //maven("https://maven.aliyun.com/repository/public/")
  mavenLocal()
  mavenCentral()
}

dependencies {
  implementation("io.ktor:ktor-server-core")
  implementation("io.ktor:ktor-server-cio")
  implementation("ch.qos.logback:logback-classic:$logbackVersion")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
  testImplementation("io.ktor:ktor-server-tests-jvm")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}


graalvmNative {

  binaries {

    named("main") {
      fallback.set(false)
      verbose.set(true)
      if (enableStatic.toBoolean()) {
        buildArgs.add("--static")
        buildArgs.add("--libc=musl")
      }
      buildArgs.add("--initialize-at-build-time=ch.qos.logback")
      buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")
      buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")
      buildArgs.add("-H:+InstallExitHandlers")
      buildArgs.add("-H:+ReportExceptionStackTraces")
      buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
      imageName.set("liteoss")
    }

    named("test") {
      fallback.set(false)
      verbose.set(true)
      buildArgs.add("--initialize-at-build-time=ch.qos.logback")
      buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")
      buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")
      buildArgs.add("-H:+InstallExitHandlers")
      buildArgs.add("-H:+ReportExceptionStackTraces")
      buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
      val path = "${projectDir}/src/test/resources/META-INF/native-image/"
      buildArgs.add("-H:ResourceConfigurationFiles=${path}resource-config.json")
      buildArgs.add("-H:ReflectionConfigurationFiles=${path}reflect-config.json")
      imageName.set("liteoss-test")
    }

  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
      events("passed", "skipped", "failed")
    }
  }

}