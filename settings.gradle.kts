pluginManagement {
	repositories {
		mavenLocal()
		gradlePluginPortal()
		maven("https://maven.fabricmc.net")
		maven("https://server.bbkr.space/artifactory/libs-release")
		maven("https://maven.quiltmc.org/repository/release")
	}
}
plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0" }
