pluginManagement {
	repositories {
		mavenLocal()
		gradlePluginPortal()
		maven("https://jitpack.io")
	}
	resolutionStrategy.eachPlugin {
		if (requested.id.id == "io.github.forgestove.modaccessor") {
			useModule("com.github.ForgeStove.ModAccessor:build:+")
		}
	}
}
plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "+" }
