package com.github.rahulsom.nothanks.app.dto

import com.github.rahulsom.nothanks.app.model.AppUser
import java.util.*

data class UserDto(
  val id: UUID,
  val username: String,
  val state: AppUser.UserState,
  val activeGameId: UUID?
)
