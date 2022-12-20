plugins {
  id ("com.gradle.enterprise") version "3.12.1"
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlways()

    buildScanPublished {
      file("build").mkdirs()
      file("build/gradle-scan.md").writeText(
        """Gradle Build Scan - [`${this.buildScanId}`](${this.buildScanUri})"""
      )
    }
  }
}

rootProject.name = "nothanks"

include("nothanks-server")
include("nothanks-client")
include("nothanks-cli")
