package com.github.rahulsom.nothanks.game

import java.util.*

class Player(val name: String, var cards: List<Int>, var tokens: Int) {
  fun score(): Int {
    val cl = Stack<Int>()
    cards.forEach { cl.push(it) }
    var score = 0
    while (cl.isNotEmpty()) {
      var last = cl.pop()
      while (cl.isNotEmpty() && cl.peek() == last - 1) {
        last = cl.pop()
      }
      score += last
    }
    score -= tokens
    return score
  }

  override fun toString() = "Player(name='$name', cards=$cards, tokens=$tokens)"
}