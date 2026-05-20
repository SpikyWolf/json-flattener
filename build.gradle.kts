plugins {
    java
    application
    id("org.graalvm.buildtools.native") version "1.1.0"
}

group = "com.spiky"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("com.spiky.jsonflattener.Launcher")
}

val jfxVersion = "25.0.3"
val osName = System.getProperty("os.name").lowercase()
val javaFxPlatform = when {
    osName.contains("win") -> "win"
    osName.contains("mac") -> "mac"
        else -> "linux"
}


dependencies {
    implementation("org.openjfx:javafx-base:$jfxVersion:$javaFxPlatform")
    implementation("org.openjfx:javafx-controls:$jfxVersion:$javaFxPlatform")
    implementation("org.openjfx:javafx-graphics:$jfxVersion:$javaFxPlatform")
    implementation("org.openjfx:javafx-fxml:$jfxVersion:$javaFxPlatform")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    compileOnly("org.jetbrains:annotations:26.1.0")
    implementation("tools.jackson.core:jackson-databind:3.1.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
graalvmNative {
    binaries.all {
        buildArgs.add("--enable-native-access=ALL-UNNAMED")
    }
}