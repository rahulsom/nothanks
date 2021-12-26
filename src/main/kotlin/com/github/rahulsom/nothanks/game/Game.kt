package com.github.rahulsom.nothanks.game

class Game(var players: List<Player>, var stack: List<Int>, var turn: Int, var tokensOnCard: Int) {
  fun view(player: String): PlayerView {
    return PlayerView(
      stack.size,
      if (stack.isNotEmpty()) stack.first() else null,
      tokensOnCard,
      players[turn].name == player,
      (players.find { it.name == player }?.tokens ?: 0) > 0,
      if (stack.isNotEmpty()) null else PlayerView.Result(
        players.map { PlayerView.Result.Score(it.name, it.score()) },
        players.minByOrNull { it.score() }!!.name
      ),
      players.find { it.name == player }?.cards,
      players.find { it.name == player }?.tokens,
      players.filter { it.name != player }.map { PlayerView.OtherPlayer(it.name, it.cards) }
    )
  }

  override fun toString() =
    "  Game(stack=$stack, turn=$turn, tokensOnCard=$tokensOnCard)\n${players.joinToString("\n") { "    $it" }}"

  fun winner() = when {
      stack.isNotEmpty() -> null
      else -> players.sortedBy { it.score() }.first().name
  }

  fun canPass() = players[turn].tokens > 0

  fun pass() {
    if (!canPass()) {
      throw IllegalStateException("You can't pass")
    }
    println("${players[turn].name} passes")
    players[turn].tokens--
    tokensOnCard++
    turn = (turn + 1) % players.size
    println(this)
  }

  fun take() {
    println("${players[turn].name} takes")
    val topCard = stack[0]
    stack = stack.drop(1)
    players[turn].tokens += tokensOnCard
    tokensOnCard = 0
    players[turn].cards += topCard
    players[turn].cards = players[turn].cards.sorted()
    turn = (turn + 1) % players.size
    println(this)
  }
}