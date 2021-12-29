package com.github.rahulsom.nothanks.app.job

import com.github.rahulsom.nothanks.app.service.NoThanksService
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton

@Singleton
class GameCreationJob(val noThanksService: NoThanksService) {
  @Scheduled(fixedRate = "5s", initialDelay = "30s")
  fun process() {
    noThanksService.createGames()
  }
}
