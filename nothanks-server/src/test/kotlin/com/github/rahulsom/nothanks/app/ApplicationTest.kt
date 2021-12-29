package com.github.rahulsom.nothanks.app

import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class ApplicationTest(private val embeddedServer: EmbeddedServer) {
  @Test
  fun testServerIsRunning() {
    assert(embeddedServer.isRunning)
  }
}