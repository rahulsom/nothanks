package com.github.rahulsom.nothanks.app.model

import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.DateUpdated
import java.time.Instant
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class GameWrapper(
  @Id
  @Column(name = "id", length = 36) var id: UUID,
  var game: String,
  var finished: Boolean,
  @DateCreated var dateCreated: Instant? = null,
  @DateUpdated var dateUpdated: Instant? = null
)