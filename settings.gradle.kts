plugins {
  id ("com.gradle.enterprise") version "3.8"
}

gradleEnterprise {
  buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"

    publishAlways()
  }
}

rootProject.name = "nothanks"

include("nothanks-server")
include("nothanks-client")
include("nothanks-cli")
