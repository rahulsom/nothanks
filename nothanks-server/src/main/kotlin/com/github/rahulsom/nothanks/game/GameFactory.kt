package com.github.rahulsom.nothanks.game

class GameFactory {
  fun create(players: Int) = create((1..players).map { "Player $it" }.toList())
  fun create(players: List<String>): Game {
    val stack = (3..35).toList().shuffled().take(24)
    val tokensPerPlayer = when (players.size) {
      3 -> 11
      4 -> 11
      5 -> 11
      6 -> 9
      7 -> 7
      else -> throw IllegalArgumentException("Require 3 <= players <= 7")
    }
    val game = Game(players.map { Player(it, emptyList(), tokensPerPlayer) }, stack, 0, 0)
    println("Created\n$game")
    return game
  }
}
