package com.github.rahulsom.nothanks.app.web

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule

@Secured(SecurityRule.IS_ANONYMOUS, SecurityRule.IS_AUTHENTICATED)
@Controller("/")
class HomeController {

    @Get(uri="/", produces=["text/plain"])
    fun index(): String {
        return "Home Response"
    }
}