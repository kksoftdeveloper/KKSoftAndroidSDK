import java.util.Properties

apply(plugin = "maven-publish")
apply(plugin = "signing")

val githubProperties = readProperties(file("../build-logic/github.properties"))
println("githubProperties usr: ${githubProperties["gpr.usr"]}")
println("githubProperties key: ${githubProperties["gpr.key"]}")

val getGroupId = project.findProperty("groupId")?.toString()
val getArtifactId = project.findProperty("artifactId")?.toString()
val getVersion = project.findProperty("version")?.toString()

println("getGroupId: $getGroupId")
println("getArtifactId: $getArtifactId")
println("getVersion: $getVersion")

configure<PublishingExtension> {
  publications {
    create<MavenPublication>("aar") {
      groupId = getGroupId
      artifactId = getArtifactId
      version = getVersion
      afterEvaluate {
        from(components["release"])
      }
    }
  }

  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/knmobileapp/android-Game-SDK")

      credentials {
        username = githubProperties["gpr.usr"] as? String ?: System.getenv("GPR_USER")
        password = githubProperties["gpr.key.publish"] as? String ?: System.getenv("GPR_API_KEY")
      }
    }
  }
}

fun readProperties(propertiesFile: File) = Properties().apply {
  propertiesFile.inputStream().use { fis ->
    load(fis)
  }
}
