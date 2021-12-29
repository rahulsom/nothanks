package com.github.rahulsom.nothanks.game

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class GameTest {
  @Test
  fun `should declare a winner when no cards are left`() {
    val game = Game(
      listOf(
        Player("Player 1", listOf(5, 6), 3),
        Player("Player 2", listOf(3), 4),
        Player("Player 3", listOf(7), 4),
      ), listOf(), 1, 0
    )

    assertNotNull(game.winner())
  }

  @Test
  fun `should not declare a winner when cards are left`() {
    val game = Game(
      listOf(
        Player("Player 1", listOf(5, 6), 3),
        Player("Player 2", listOf(3), 4),
        Player("Player 3", listOf(7), 4),
      ), listOf(4), 1, 0
    )

    assertNull(game.winner())
  }

  @Test
  fun `canPass is true when player has tokens`() {
    val game = Game(
      listOf(
        Player("Player 1", listOf(5, 6), 3),
        Player("Player 2", listOf(3), 4),
        Player("Player 3", listOf(7), 4),
      ), listOf(4), 1, 0
    )

    assertTrue(game.canPass())
  }

  @Test
  fun `canPass is false when player has no tokens`() {
    val game = Game(
      listOf(
        Player("Player 1", listOf(5, 6), 3),
        Player("Player 2", listOf(3), 0),
        Player("Player 3", listOf(7), 4),
      ), listOf(4), 1, 0
    )

    assertFalse(game.canPass())
  }

  @ParameterizedTest(name = "view for {0} is correct")
  @MethodSource("viewProvider")
  fun `view for player is correct`(playerName: String, expectedView: PlayerView) {
    val game = Game(
      listOf(
        Player("Player 1", listOf(5, 6), 3),
        Player("Player 2", listOf(3), 0),
        Player("Player 3", listOf(7), 4),
      ), listOf(4), 1, 0
    )

    assertEquals(expectedView, game.view(playerName))
  }

  @Test
  fun `passing reduces tokens`() {
    val game = Game(
      listOf(
        Player("Player 1", listOf(5, 6), 3),
        Player("Player 2", listOf(3), 0),
        Player("Player 3", listOf(7), 4),
      ), listOf(4), 0, 0
    )

    game.pass()

    assertEquals(2, game.view("Player 1").myTokens)
  }

  @Test
  fun `passing throws an exception if you can't pass`() {
    val game = Game(
      listOf(
        Player("Player 1", listOf(5, 6), 3),
        Player("Player 2", listOf(3), 0),
        Player("Player 3", listOf(7), 4),
      ), listOf(4), 1, 0
    )

    val exception = assertThrows(IllegalStateException::class.java) {
      game.pass()
    }

    assertEquals("You can't pass", exception.message)
  }

  @Test
  fun `taking increases tokens and reduces cards`() {
    val game = Game(
      listOf(
        Player("Player 1", listOf(5, 6), 3),
        Player("Player 2", listOf(3), 0),
        Player("Player 3", listOf(7), 4),
      ), listOf(4, 8, 9), 0, 3
    )

    game.take()

    assertEquals(6, game.view("Player 1").myTokens)
    assertEquals(listOf(4, 5, 6), game.view("Player 1").myCards)
    assertEquals(listOf(8, 9), game.stack)
  }

  companion object {
    @JvmStatic
    fun viewProvider(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(
          "Player 1", PlayerView(
            1, 4, 0, false, true, null, listOf(5, 6), 3, listOf(
              PlayerView.OtherPlayer("Player 2", listOf(3)),
              PlayerView.OtherPlayer("Player 3", listOf(7)),
            )
          )
        ), Arguments.of(
          "Player 2", PlayerView(
            1, 4, 0, true, false, null, listOf(3), 0, listOf(
              PlayerView.OtherPlayer("Player 1", listOf(5, 6)),
              PlayerView.OtherPlayer("Player 3", listOf(7)),
            )
          )
        ), Arguments.of(
          "Player 3", PlayerView(
            1, 4, 0, false, true, null, listOf(7), 4, listOf(
              PlayerView.OtherPlayer("Player 1", listOf(5, 6)),
              PlayerView.OtherPlayer("Player 2", listOf(3)),
            )
          )
        )
      )
    }
  }
}