pluginManagement {
	repositories {
		mavenLocal()
		gradlePluginPortal()
		maven("https://jitpack.io")
	}
	resolutionStrategy.eachPlugin {
		if(requested.id.id != "io.github.forgestove.modaccessor") return@eachPlugin
		useModule("com.github.ForgeStove.ModAccessor:build:${requested.version ?: "+"}")
	}
}
plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "+" }
