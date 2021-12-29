package com.github.rahulsom.nothanks.app.rest

import com.github.rahulsom.nothanks.app.service.NoThanksException
import com.github.rahulsom.nothanks.app.service.NoThanksService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import java.security.Principal
import java.util.*

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/v1/game")
class GameController(private val noThanksService: NoThanksService) {
  @Post("/play")
  fun play(principal: Principal) =
    noThanksService.requestPlay(principal.name)

  @Get("/status")
  fun getStatus(principal: Principal) =
    noThanksService.getUser(principal.name)

  @Secured("ADMIN")
  @SecurityRequirement(name = "ADMIN")
  @Get("/queue")
  fun gameQueue() =
    noThanksService.getQueuedUsers()

  @Get("/state")
  fun getState(@QueryValue gameId: UUID, principal: Principal) =
    noThanksService.getGameState(gameId, principal.name)

  @Post("/take")
  fun take(@QueryValue gameId: UUID, principal: Principal) =
    noThanksService.take(gameId, principal.name)

  @Post("/pass")
  fun pass(@QueryValue gameId: UUID, principal: Principal) =
    noThanksService.pass(gameId, principal.name)

  @Error(exception = NoThanksException::class)
  fun handleNoThanksException(ex: NoThanksException) =
    HttpResponse.badRequest(mapOf("message" to ex.message))
}