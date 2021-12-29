package com.github.rahulsom.nothanks.cli

import com.github.rahulsom.nothanks.client.model.PlayerView
import java.io.BufferedReader
import java.util.*

interface IBrain {
  fun shouldTakeCard(gameState: PlayerView): Boolean
}

class HumanBrain: IBrain {
  override fun shouldTakeCard(gameState: PlayerView): Boolean {
    println("")
    while (true) {
      println("Do you want to take this card (y/n)?")
      val input = BufferedReader(System.`in`.reader()).readLine()
      if (input.lowercase(Locale.getDefault()) == "y") {
        return true
      } else if (input.lowercase(Locale.getDefault()) == "n") {
        return false
      } else {
        println("Invalid input. Try again.")
      }
    }
  }
}

class AlwaysTakeBrain: IBrain {
  override fun shouldTakeCard(gameState: PlayerView) = true
}

class PreferPassBrain: IBrain {
  override fun shouldTakeCard(gameState: PlayerView) = !gameState.canPass
}
