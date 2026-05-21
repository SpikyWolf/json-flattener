plugins {
    java
    application
    id("com.gluonhq.gluonfx-gradle-plugin") version "1.0.28"
}

group = "com.spiky"
version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

java { toolchain { languageVersion = JavaLanguageVersion.of(23) } }

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

application { mainClass.set("com.spiky.jsonflattener.Launcher") }

val junitVersion = "5.12.1"

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

tasks.withType<Test> { useJUnitPlatform() }

gluonfx {
    reflectionList = listOf(
        "com.spiky.jsonflattener.JsonFlattenerApplication",
        "javafx.scene.control.SplitPane",
        "javafx.scene.layout.AnchorPane",
        "javafx.scene.layout.VBox",
        "javafx.scene.layout.HBox",
        "javafx.scene.layout.StackPane",
        "javafx.scene.layout.Region",
        "javafx.scene.control.Button",
        "javafx.scene.control.Label",
        "javafx.scene.control.TextField",
        "javafx.scene.image.ImageView",
        "javafx.scene.image.Image",
        "javafx.geometry.Insets",
        "javafx.scene.Cursor",
        "javafx.scene.text.Font"
    )
    compilerArgs = listOf("--enable-native-access=ALL-UNNAMED")
}