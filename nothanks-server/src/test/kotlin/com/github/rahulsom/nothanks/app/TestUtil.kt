package com.github.rahulsom.nothanks.app

import java.util.*

object TestUtil {
  fun basic(username: String, password: String) =
    "Basic ${"$username:$password".toBase64()}"

  private fun String.toBase64() =
    toByteArray(Charsets.UTF_8).encodeBase64()

  private fun ByteArray.encodeBase64() =
    Base64.getEncoder().encodeToString(this)
}