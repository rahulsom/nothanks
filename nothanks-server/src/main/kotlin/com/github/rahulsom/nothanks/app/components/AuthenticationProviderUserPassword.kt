package com.github.rahulsom.nothanks.app.components

import com.github.rahulsom.nothanks.app.dao.AppUserRepository
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

@Singleton
class AuthenticationProviderUserPassword(val userRepository: AppUserRepository): AuthenticationProvider {

  override fun authenticate(httpRequest: HttpRequest<*>?,
                            authenticationRequest: AuthenticationRequest<*, *>): Publisher<AuthenticationResponse> {
    return Flux.create({ emitter: FluxSink<AuthenticationResponse> ->
      val identity = authenticationRequest.identity
      val roles = if (identity == "admin") listOf("ADMIN") else listOf("USER")
      val user = userRepository.findByUsername(identity as String)
      if (user != null && user.password == authenticationRequest.secret as String) {
        emitter.next(AuthenticationResponse.success(user.username, roles))
        emitter.complete()
      } else {
        emitter.error(AuthenticationResponse.exception())
      }
    }, FluxSink.OverflowStrategy.ERROR)
  }

}
