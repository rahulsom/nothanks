package com.github.rahulsom.nothanks.app.model

import com.github.rahulsom.nothanks.app.dto.UserDto
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
data class AppUser(
  @Id
  @Column(name = "id", length = 36) var id: UUID,
  @Column(unique = true, updatable = false) var username: String,
  var password: String,
  @Enumerated(EnumType.STRING)
  var state: UserState = UserState.IDLE,
  var activeGameId: UUID? = null,
  @DateCreated var dateCreated: Instant? = null,
  @DateUpdated var dateUpdated: Instant? = null,
) {
  fun toDto() = UserDto(id, username, state, activeGameId)

  enum class UserState { IDLE, WAITING, PLAYING }
}