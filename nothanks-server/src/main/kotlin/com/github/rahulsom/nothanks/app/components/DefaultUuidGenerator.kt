package com.github.rahulsom.nothanks.app.components

import jakarta.inject.Singleton
import java.util.*

@Singleton
class DefaultUuidGenerator: UuidGenerator {
  override fun generate(): UUID = UUID.randomUUID()
}