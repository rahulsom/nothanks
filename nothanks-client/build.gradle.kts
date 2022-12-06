plugins {
  id("org.openapi.generator")
  `java-library`
}

openApiGenerate {
  inputSpec.set("${rootDir}/nothanks-server/build/tmp/kapt3/classes/main/META-INF/swagger/nothanks-1.0.yml")
  generatorName.set("java")
  configOptions.put("library", "retrofit2")
  configOptions.put("artifactId", "nothanks-api")
  configOptions.put("invokerPackage", "com.github.rahulsom.nothanks.client")
  configOptions.put("apiPackage", "com.github.rahulsom.nothanks.client.api")
  configOptions.put("modelPackage", "com.github.rahulsom.nothanks.client.model")
  configOptions.put("groupId", project.group.toString())
}

sourceSets {
  main { java.srcDir("build/generate-resources/main/src/main/java") }
  test { java.srcDir("build/generate-resources/main/src/test/java") }
}
tasks.getByName("openApiGenerate")
  .dependsOn(":nothanks-server:compileKotlin")

tasks.getByName("compileJava").dependsOn("openApiGenerate")

dependencies {
  api("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
  implementation("com.squareup.retrofit2:converter-gson:2.9.0")
  implementation("io.swagger:swagger-annotations:1.6.9")
  implementation("com.google.code.findbugs:jsr305:3.0.2")
  implementation("org.apache.oltu.oauth2:org.apache.oltu.oauth2.client:1.0.1") {
    exclude("org.apache.oltu.oauth2", "org.apache.oltu.oauth2.common")
  }
  implementation("io.gsonfire:gson-fire:1.8.5")
  implementation("org.threeten:threetenbp:1.6.5")
  implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
  implementation("javax.annotation:javax.annotation-api:1.3.2")
  implementation("org.openapitools:jackson-databind-nullable:0.2.4")

  testImplementation("junit:junit:4.13.2")
}

