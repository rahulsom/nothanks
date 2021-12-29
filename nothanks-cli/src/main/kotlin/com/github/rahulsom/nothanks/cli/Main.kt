package com.github.rahulsom.nothanks.cli

import com.github.rahulsom.nothanks.client.ApiClient
import com.github.rahulsom.nothanks.client.api.DefaultApi
import com.github.rahulsom.nothanks.client.model.AppUserUserState
import com.github.rahulsom.nothanks.client.model.PlayerView
import okhttp3.logging.HttpLoggingInterceptor
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import org.slf4j.LoggerFactory
import picocli.CommandLine
import retrofit2.Call
import java.io.BufferedReader
import java.util.*
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@CommandLine.Command(
  name = "nothanks-cli",
  description = ["A command line client for the NoThanks API"],
)
class Main : Callable<Unit> {

  private val log = LoggerFactory.getLogger(javaClass)!!

  @CommandLine.Option(names = ["-u", "--user"], description = ["The user to use for the API"], required = true)
  var user: String = ""

  @CommandLine.Option(names = ["-p", "--password"], description = ["The password to use for the API"], required = true)
  var password: String = ""

  @CommandLine.Option(names = ["-b", "--brain"], description = ["The brain to use for playing game"], required = true)
  var brain: Brain = Brain.Human

  var brainInstance: IBrain? = null

  enum class Brain {
    Human, AlwaysTake, PreferPass
  }

  private fun <T> mustSucceed(operation: String, block: () -> Call<T>): T {
    log.debug("Performing $operation...")
    val response = block().execute()
    if (response.code() == 200) {
      log.debug("$operation succeeded")
      return response.body()!!
    } else {
      log.error("$operation failed with status ${response.code()}")
      throw RuntimeException("Unexpected response: " + response.errorBody()?.string())
    }
  }

  private fun buildClient(transform: ((ApiClient) -> Unit)? = null): DefaultApi {
    val client = ApiClient()
      .also { it.adapterBuilder.baseUrl("http://localhost:8080") }
    val httpLoggingInterceptor = HttpLoggingInterceptor { LoggerFactory.getLogger(DefaultApi::class.java).debug(it) }
      .also { it.level = HttpLoggingInterceptor.Level.BODY }
    client.okBuilder.addInterceptor(httpLoggingInterceptor)
    transform?.let { it(client) }
    return client.createService(DefaultApi::class.java)
  }

  override fun call() {
    pickBrain()
    val signupClient = buildClient()
    val client = buildClient(::configureBasicAuth)

    signInOrSignUp(client, signupClient)
    userShouldBeIdle(client)
    startNewGame(client)
    val gameId = waitForGameToStart(client)

    while (true) {
      val gameState = mustSucceed("game status") { client.getState(gameId) }
      printGameState(gameState)

      if (gameState.result != null) {
        printResult(gameState)
        break
      }

      if (gameState.turn) {
        if (handleTurn(gameState)) {
          mustSucceed("take card") { client.take(gameId) }
        } else {
          mustSucceed("pass") { client.pass(gameId) }
        }
      }

      Thread.sleep(1000)
    }
  }

  private fun configureBasicAuth(apiClient: ApiClient) {
    val basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString("$user:$password".toByteArray())
    apiClient.addAuthorization("Basic") { chain ->
      chain.proceed(
        chain.request().newBuilder()
          .header("Authorization", basicAuthHeader)
          .build()
      )
    }
  }

  private fun pickBrain() {
    brainInstance = when (brain) {
      Brain.Human -> HumanBrain()
      Brain.AlwaysTake -> AlwaysTakeBrain()
      Brain.PreferPass -> PreferPassBrain()
    }
  }

  private fun handleTurn(gameState: PlayerView): Boolean {
    return brainInstance!!.shouldTakeCard(gameState)
  }

  private fun printResult(gameState: PlayerView) {
    println("")
    println("Game Over! Winner: ${gameState.result?.winner}")
    gameState.result?.scores?.forEach {
      println(" - ${it.name}: ${it.score}")
    }
  }

  private fun printGameState(gameState: PlayerView) {
    println(Ansi.ansi().eraseScreen())
    val cardStack =
      if (gameState.topCard != null)
        " ${gameState.topCard} ]".padStart(gameState.stackSize, '[')
      else
        ""
    val tokensOnCard = "  ".padStart(gameState.tokensOnCard, '*')
    println(cardStack + tokensOnCard)
    println("")
    gameState.otherCards.forEach { println("  " + it.name + " - " + it.cards.joinToString()) }
    println("".padEnd(80, '-'))
    println("  " + gameState.myCards?.joinToString() + " - " + "".padEnd(gameState.myTokens ?: 0, '*'))
  }

  private fun waitForGameToStart(client: DefaultApi): UUID {
    while (true) {
      val status = mustSucceed("status") { client.status }
      if (status.state == AppUserUserState.PLAYING) {
        return status.activeGameId!!
      }
      Thread.sleep(1000)
    }
  }

  private fun startNewGame(client: DefaultApi) {
    val play = mustSucceed("play") { client.play() }
    if (play.state != AppUserUserState.WAITING) {
      throw RuntimeException("Unexpected state: ${play.state}")
    }
  }

  private fun userShouldBeIdle(client: DefaultApi) {
    val status = mustSucceed("status") { client.status }
    if (status.state != AppUserUserState.IDLE) {
      throw RuntimeException("Unexpected state: ${status.state}")
    }
  }

  private fun signInOrSignUp(client: DefaultApi, signupClient: DefaultApi) {
    val testAuth = client.status.execute()

    if (testAuth.code() == 401) {
      val signup = signupClient.create(user, password).execute()
      if (signup.code() != 200) {
        log.error("Signup failed with status ${signup.code()}")
        throw RuntimeException("Unexpected response: " + signup.errorBody()?.string())
      }
      log.info("Signup succeeded")
    }
  }
}

fun main(args: Array<String>) {
  AnsiConsole.systemInstall()
  exitProcess(CommandLine(Main()).execute(*args))
}
