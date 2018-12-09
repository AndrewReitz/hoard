import com.netflix.nebula.interop.action
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `java-library`
  groovy
  id("org.jetbrains.kotlin.jvm") version "1.3.11"
  id("nebula.optional-base") version "4.0.1"
  id("com.github.ben-manes.versions") version "0.20.0"
  id("com.gradle.build-scan") version "2.0.2"
}

repositories {
  jcenter()
}

group = "io.github.roguesdev"
version = "0.3.0"

java {
  sourceCompatibility = JavaVersion.VERSION_1_6
  targetCompatibility = JavaVersion.VERSION_1_6
}

buildScan {
  setTermsOfServiceUrl("https://gradle.com/terms-of-service")
  setTermsOfServiceAgree("yes")

  publishAlways()
}

configurations.create("ktlint")

val optional: groovy.lang.Closure<Any?> by extra

dependencies {
  implementation( "org.reactivestreams:reactive-streams:1.0.2")
  implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
  implementation("com.google.code.findbugs:jsr305:3.0.2")
  implementation("com.squareup.okio:okio:2.1.0") // todo update everything to use okio

  implementation("com.squareup.moshi:moshi:1.8.0", { optional(this) })
  implementation("com.google.code.gson:gson:2.8.5", { optional(this) })
  implementation("io.reactivex:rxjava-reactive-streams:1.2.1", { optional(this) })
  implementation("io.reactivex.rxjava2:rxjava:2.2.4", { optional(this) })

  testImplementation("org.spockframework:spock-core:1.2-groovy-2.5")
  testImplementation("org.codehaus.groovy:groovy:2.5.4")
  testImplementation("net.bytebuddy:byte-buddy:1.9.5")
  testImplementation("org.objenesis:objenesis:3.0.1")

  testImplementation("org.amshove.kluent:kluent:1.44")
  testImplementation("org.assertj:assertj-core:2.9.1")

  // needed to test encryption on jvm
  testImplementation( "org.bouncycastle:bcprov-jdk15on:1.60")

  add("ktlint", "com.github.shyiko:ktlint:0.29.0")
}

task<JavaExec>("ktlint") {
  group = "verification"
  description = "Check Kotlin code style."
  classpath = configurations.getByName("ktlint")
  main = "com.github.shyiko.ktlint.Main"
  args = listOf("src/**/*.kt")
}.also { ktlint -> tasks["check"].dependsOn(ktlint) }

task<JavaExec>("ktlintFormat") {
  group = "formatting"
  description = "Fix Kotlin code style deviations."
  classpath = configurations.getByName("ktlint")
  main = "com.github.shyiko.ktlint.Main"
  args = listOf("-F", "src/**/*.kt")
}
