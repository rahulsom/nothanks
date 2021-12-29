package com.github.rahulsom.nothanks.app

import com.github.rahulsom.nothanks.app.TestUtil.basic
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest
class QueueTest(private val client: NoThanksClient) {
  @Test
  fun test() {
    client.createUser("admin", "admin")
    val showGameQueue1 = client.showGameQueue(basic("admin", "admin"))
    Assertions.assertEquals(0, showGameQueue1.size)

    client.createUser("user1", "user1")
    client.playGame(basic("user1", "user1"))

    val showGameQueue2 = client.showGameQueue(basic("admin", "admin"))
    Assertions.assertEquals(1, showGameQueue2.size)

    client.createUser("user2", "user2")
    client.playGame(basic("user2", "user2"))

    val showGameQueue3 = client.showGameQueue(basic("admin", "admin"))
    Assertions.assertEquals(2, showGameQueue3.size)
  }
}