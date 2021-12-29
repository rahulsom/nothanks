package com.github.rahulsom.nothanks.app

import com.github.rahulsom.nothanks.app.TestUtil.basic
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class UserControllerTest(private val noThanksClient: NoThanksClient) {

  @Test
  fun testList() {
    val exception = assertThrows(HttpClientResponseException::class.java) {
      noThanksClient.listUsers(basic("admin", "admin"))
    }

    assertEquals(exception.status.code, 401)

    val admin = noThanksClient.createUser("admin", "admin")
    assertEquals(admin.username, "admin")

    var list = noThanksClient.listUsers(basic("admin", "admin"))
    assertTrue(list.any { it.username == "admin" })
    assertTrue(list.none { it.username == "user" })

    val user = noThanksClient.createUser("user", "user")
    assertEquals(user.username, "user")

    list = noThanksClient.listUsers(basic("admin", "admin"))
    assertTrue(list.any { it.username == "admin" })
    assertTrue(list.any { it.username == "user" })

    val otherException = assertThrows(HttpClientResponseException::class.java) {
      noThanksClient.listUsers(basic("invalid", "user"))
    }

    assertEquals(otherException.status.code, 401)
  }

  @Test
  fun `should update password`() {
    val user = noThanksClient.createUser("foo", "foo")
    assertEquals(user.username, "foo")

    val updateUser = noThanksClient.updateUser(basic("foo", "foo"), "bar")
    assertEquals(updateUser.username, "foo")

    val failUpdate = assertThrows(HttpClientResponseException::class.java) {
      noThanksClient.updateUser(basic("foo", "foo"), "bar")
    }
    assertEquals(failUpdate.status.code, 401)
  }
}

