pluginManagement {
	repositories {
		mavenLocal()
		gradlePluginPortal()
		maven("https://jitpack.io")
		maven("https://maven.neoforged.net/releases")
	}
	resolutionStrategy.eachPlugin {
		if(requested.id.id != "io.github.forgestove.modaccessor") return@eachPlugin
		useModule("com.github.ForgeStove.ModAccessor:build:${requested.version ?: "+"}")
	}
}
