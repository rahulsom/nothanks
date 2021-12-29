package com.github.rahulsom.nothanks.app

import com.github.rahulsom.nothanks.app.dto.UserDto
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("/")
interface NoThanksClient {
  @Get("/v1/users/list")
  fun listUsers(@Header authorization: String): List<UserDto>

  @Post("/v1/users/create")
  fun createUser(@QueryValue username: String, @QueryValue password: String): UserDto

  @Post("/v1/users/update")
  fun updateUser(@Header authorization: String, @QueryValue password: String): UserDto

  @Post("/v1/game/play")
  fun playGame(@Header authorization: String): UserDto

  @Get("/v1/game/status")
  fun getUserStatus(@Header authorization: String): UserDto

  @Get("/v1/game/queue")
  fun showGameQueue(@Header authorization: String): List<UserDto>
}