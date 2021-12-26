package com.github.rahulsom.nothanks.game

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class PlayerTest {
  @ParameterizedTest(name = "Cards {0} and tokens {1} result in score {2}")
  @MethodSource("scoreSource")
  fun `score is correctly calculated`(cards: List<Int>, tokens: Int, score: Int) {
    val player = Player("Player Name", cards, tokens)
    assertEquals(score, player.score())
  }

  companion object {
    @JvmStatic
    fun scoreSource(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(listOf<Int>(), 0, 0),
        Arguments.of(listOf<Int>(), 3, -3),
        Arguments.of(listOf(3), 3, 0),
        Arguments.of(listOf(1, 3), 3, 1),
        Arguments.of(listOf(35), 3, 32),
        Arguments.of(listOf(1, 3, 4), 0, 4), // 4 is masked by 3
        Arguments.of(listOf(1, 3, 4, 5, 6), 0, 4), // 3 masks 4, 5, 6
        Arguments.of(listOf(1, 3, 4, 5, 6, 8), 0, 12), // 3 masks 4, 5, 6
      )
    }
  }
}