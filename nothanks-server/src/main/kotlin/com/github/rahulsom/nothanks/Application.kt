package com.github.rahulsom.nothanks

import com.github.rahulsom.nothanks.game.GameFactory
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.runtime.Micronaut.*
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.info.*

@OpenAPIDefinition(
    info = Info(
            title = "NoThanks",
            version = "1.0"
    )
)
@Factory
class Application {
  @Bean
  fun gameFactory(): GameFactory = GameFactory()
}
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("com.github.rahulsom.nothanks")
		.start()
}

