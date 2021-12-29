package com.github.rahulsom.nothanks.app.dao

import com.github.rahulsom.nothanks.app.model.GameWrapper
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.PageableRepository
import java.util.*

@Repository
interface GameWrapperRepository : PageableRepository<GameWrapper, UUID>