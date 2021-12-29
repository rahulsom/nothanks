package com.github.rahulsom.nothanks.game

data class PlayerView(
  val stackSize: Int,
  val topCard: Int?,
  val tokensOnCard: Int,
  val turn: Boolean,
  val canPass: Boolean,
  val result: Result?,
  val myCards: List<Int>?,
  val myTokens: Int?,
  val otherCards: List<OtherPlayer>
) {
  data class Result(val scores: List<Score>, val winner: String) {
    data class Score(val name: String, val score: Int)
  }

  data class OtherPlayer(val name: String, val cards: List<Int>)
}