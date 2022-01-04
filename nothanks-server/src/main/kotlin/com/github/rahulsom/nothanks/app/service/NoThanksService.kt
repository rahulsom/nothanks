package com.github.rahulsom.nothanks.app.service

import com.github.rahulsom.nothanks.app.components.UuidGenerator
import com.github.rahulsom.nothanks.app.dao.AppUserRepository
import com.github.rahulsom.nothanks.app.dao.GameWrapperRepository
import com.github.rahulsom.nothanks.app.dto.UserDto
import com.github.rahulsom.nothanks.app.model.AppUser
import com.github.rahulsom.nothanks.app.model.GameWrapper
import com.github.rahulsom.nothanks.game.Game
import com.github.rahulsom.nothanks.game.GameFactory
import com.github.rahulsom.nothanks.game.PlayerView
import jakarta.inject.Singleton
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import java.util.*

@Singleton
class NoThanksService(
  val userRepository: AppUserRepository,
  val uuidGenerator: UuidGenerator,
  val gameFactory: GameFactory,
  val gameWrapperRepository: GameWrapperRepository,
) {
  fun createUser(username: String, password: String): UserDto {
    val appUser = AppUser(uuidGenerator.generate(), username, password)
    try {
      return userRepository.save(appUser).toDto()
    } catch (e: javax.persistence.PersistenceException) {
      if (e.cause is ConstraintViolationException) {
        throw NoThanksException("Username already taken")
      } else {
        throw NoThanksException("Error creating user $username. ${e.message}")
      }
    } catch (e: Exception) {
      LoggerFactory.getLogger(javaClass).error("Error creating user $username", e)
      throw NoThanksException("Error creating user $username. ${e.message}")
    }
  }

  fun updatePassword(username: String, password: String): UserDto {
    val appUser = userRepository.findByUsername(username) ?: throw NoThanksException("User $username does not exist")
    appUser.password = password
    val user = userRepository.update(appUser)
    return user.toDto()
  }

  fun listUsers() = userRepository.findAll().map(AppUser::toDto).toList()

  fun requestPlay(username: String): UserDto {
    val user = userRepository.findByUsername(username) ?: throw NoThanksException("User $username does not exist")
    if (user.state == AppUser.UserState.IDLE) {
      user.state = AppUser.UserState.WAITING
      user.activeGameId = null
      userRepository.update(user)
      return user.toDto()
    } else {
      throw NoThanksException("User is already ${user.state}")
    }
  }

  fun getUser(username: String) =
    userRepository.findByUsername(username)?.let(AppUser::toDto)

  fun getQueuedUsers() =
    userRepository.findAllByState(AppUser.UserState.WAITING).toList().map(AppUser::toDto)

  fun createGames() {
    val waitingUsers = userRepository.findAllByState(AppUser.UserState.WAITING)
    createGames(waitingUsers.shuffled())
  }

  private tailrec fun createGames(waitingUsers: List<AppUser>, gamesCreated: Int = 0): Int {
    when {
      waitingUsers.size < 3 -> {
        LoggerFactory.getLogger(javaClass)
          .info("Not enough users to start a game. ${waitingUsers.size} waiting. At least 3 required")
        return 0
      }
      waitingUsers.size in (3..5) -> {
        return createGame(waitingUsers.map { it.username })
      }
      waitingUsers.size in (6..7) -> {
        createGame(waitingUsers.map { it.username }.take(3))
        return createGames(waitingUsers.drop(3), gamesCreated + 1)
      }
      waitingUsers.size == (8) -> {
        createGame(waitingUsers.map { it.username }.take(4))
        return createGames(waitingUsers.drop(4), gamesCreated + 1)
      }
      else -> {
        createGame(waitingUsers.map { it.username }.take(5))
        return createGames(waitingUsers.drop(5), gamesCreated + 1)
      }
    }
  }

  private fun createGame(players: List<String>): Int {
    val game = gameFactory.create(players)
    val saved = gameWrapperRepository.save(GameWrapper(uuidGenerator.generate(), game.toJson(), false))
    players.forEach {
      val user = userRepository.findByUsername(it)!!
      user.state = AppUser.UserState.PLAYING
      user.activeGameId = saved.id
      userRepository.update(user)
    }
    return 1
  }

  fun getGameState(gameId: UUID, name: String): PlayerView {
    val gameWrapper = gameWrapperRepository.findById(gameId).get()
    return Game.fromJson(gameWrapper.game).view(name)
  }

  fun take(gameId: UUID, name: String): PlayerView {
    val gameWrapper = gameWrapperRepository.findById(gameId).get()
    val game = Game.fromJson(gameWrapper.game)
    if (game.view(name).turn) {
      game.take()
      gameWrapper.game = game.toJson()
      gameWrapper.finished = true
      gameWrapperRepository.update(gameWrapper)
      if (game.winner() != null) {
        game.players.map { it.name }.forEach { playerName ->
          userRepository.findByUsername(playerName)?.let { user ->
            user.state = AppUser.UserState.IDLE
            user.activeGameId = null
            userRepository.update(user)
          }
        }
      }
      return game.view(name)
    } else {
      throw NoThanksException("It's not your turn")
    }
  }

  fun pass(gameId: UUID, name: String): PlayerView {
    val gameWrapper = gameWrapperRepository.findById(gameId).get()
    val game = Game.fromJson(gameWrapper.game)
    if (game.view(name).turn && game.view(name).canPass) {
      game.pass()
      gameWrapper.game = game.toJson()
      gameWrapperRepository.update(gameWrapper)
      return game.view(name)
    } else {
      throw NoThanksException("It's not your turn")
    }
  }
}