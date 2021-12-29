package com.github.rahulsom.nothanks.app.rest

import com.github.rahulsom.nothanks.app.service.NoThanksException
import com.github.rahulsom.nothanks.app.service.NoThanksService
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import java.security.Principal

@Controller("/v1/users")
class UserController(val noThanksService: NoThanksService) {
  @Secured(SecurityRule.IS_ANONYMOUS, SecurityRule.IS_AUTHENTICATED)
  @Post(uri = "/create", produces = ["application/json"])
  fun create(@QueryValue username: String, @QueryValue password: String) =
    HttpResponse.ok(noThanksService.createUser(username, password))

  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Post(uri = "/update", produces = ["application/json"])
  fun update(@QueryValue password: String, principal: Principal) =
    HttpResponse.ok(noThanksService.updatePassword(principal.name, password))

  @Secured("ADMIN")
  @SecurityRequirement(name = "ADMIN")
  @Get(uri = "/list", produces = ["application/json"])
  fun list() = noThanksService.listUsers()

  @Error(exception = NoThanksException::class)
  fun handleNoThanksException(ex: NoThanksException) =
    HttpResponse.badRequest(mapOf("message" to ex.message))
}