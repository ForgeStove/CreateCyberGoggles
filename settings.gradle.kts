pluginManagement {
	repositories {
		mavenLocal()
		gradlePluginPortal()
		maven("https://raw.githubusercontent.com/ForgeStove/Maven/main/releases")
	}
}
plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "+" }
