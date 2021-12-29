package com.github.rahulsom.nothanks.game

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

internal class GameFactoryTest {
  @ParameterizedTest(name = "{index} Creating a game with {0} players fails")
  @ValueSource(ints = [1, 2, 8, 9, 10])
  fun createGameWithIllegalNumberOfPlayersFails(players: Int) {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      GameFactory().create(players)
    }
    assertEquals(exception.message, "Require 3 <= players <= 7")
  }

  @ParameterizedTest(name = "{index} Creating a game with {0} players succeeds and each player has {1} tokens")
  @MethodSource("validPlayerParameters")
  fun createGameWithValidPlayersSucceeds(players: Int, tokens: Int) {
    val game = GameFactory().create(players)
    assertNotNull(game)
    assertEquals(game.stack.size, 24)
    assertEquals(game.players.size, players)
    game.players.forEach {
      assertEquals(tokens, it.tokens)
      assertEquals(0, it.cards.size)
    }
  }

  companion object {
    @JvmStatic
    fun validPlayerParameters() = Stream.of(
      Arguments.of(3, 11),
      Arguments.of(4, 11),
      Arguments.of(5, 11),
      Arguments.of(6, 9),
      Arguments.of(7, 7),
    )
  }
}