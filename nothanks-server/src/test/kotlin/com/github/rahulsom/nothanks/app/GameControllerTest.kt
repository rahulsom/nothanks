package com.github.rahulsom.nothanks.app

import com.github.rahulsom.nothanks.app.TestUtil.basic
import com.github.rahulsom.nothanks.app.model.AppUser
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@MicronautTest
class GameControllerTest(private val client: NoThanksClient) {
  @Test
  fun `User should be able to start a game`() {
    client.createUser("user", "user")
    val playGame = client.playGame(basic("user", "user"))
    assertTrue(playGame.state == AppUser.UserState.WAITING)
  }
}
