package com.github.rahulsom.nothanks.app.dao

import com.github.rahulsom.nothanks.app.model.AppUser
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository
import java.util.*

@Repository
interface AppUserRepository : PageableRepository<AppUser, UUID> {
  fun findByUsername(username: String): AppUser?
  fun findAllByState(state: AppUser.UserState): List<AppUser>
}