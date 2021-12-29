import groovy.transform.ToString
import groovy.util.logging.Slf4j
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
@Grab('com.squareup.retrofit2:retrofit:2.9.0')
import retrofit2.Call
import retrofit2.Retrofit
@Grab('com.squareup.retrofit2:converter-gson:2.9.0')
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
@Grab('org.slf4j:slf4j-api:1.7.32')
import java.util.function.Supplier

@ToString
class User {
  String id
  String username
  enum State {
    IDLE, WAITING, PLAYING
  }
  State state
  String activeGameId
}

class GameState {
  int stackSize
  int topCard
  int tokensOnCard
  boolean turn
  boolean canPass
  Result result
  List<Integer> myCards
  int myTokens
  List<OtherPlayer> otherCards

  @Override
  String toString() {
    """
    [$topCard] ${"X" * tokensOnCard} ... $stackSize
    Me: ${"X" * myTokens} ${myCards?.join(", ")}
    Others:
    """.stripIndent(4) + otherCards.join("\n")
  }

  @ToString
  static class Result {
    String winner
    List<Score> scores

    static class Score {
      String name
      int score

      @Override
      String toString() {
        return "   - $name: $score"
      }
    }

    @Override
    String toString() {
      """
      Winner: $winner
      Scores:
      """.stripIndent(6) + scores.join("\n")
    }
  }

  static class OtherPlayer {
    String name
    List<Integer> cards

    @Override
    String toString() {
      "  - ${name} - ${cards?.join(", ")}"
    }
  }
}

interface NoThanksService {
  @POST("/v1/users/create")
  Call<User> register(@Query("username") String username, @Query("password") String password)

  @GET("/v1/game/status")
  Call<User> status()

  @POST("/v1/game/play")
  Call<User> play()

  @GET("/v1/game/state")
  Call<GameState> gameState(@Query("gameId") String gameId)

  @POST("/v1/game/take")
  Call<GameState> take(@Query("gameId") String gameId)

  @POST("/v1/game/pass")
  Call<GameState> pass(@Query("gameId") String gameId)

}

@Slf4j
class NoThanksCli {
  String username
  String password

  def <T> T mustSucceed(String operation, Supplier<Call<T>> supplier) {
    log.debug "Performing $operation..."
    def response = supplier.get().execute()
    if (response.code() == 200) {
      log.debug "$operation succeeded"
      return response.body()
    } else {
      log.error "$operation failed with status ${response.code()}"
      throw new RuntimeException("Unexpected response: " + response.errorBody().string())
    }
  }


  def run() {
    NoThanksService signupClient = signupClient()
    NoThanksService client = client()

    def testAuth = client.status().execute()

    if (testAuth.code() == 401) {
      log.info("Auth failed. Maybe user doesn't exist. Attempting to register...")
      def register = signupClient.register(username, password).execute()
      if (register.code() != 200) {
        log.error("Failed to register: " + register.errorBody().string())
        System.exit(1)
      }
      log.info("Successfully registered user.")
    }

    def userStatus = mustSucceed("status check") { client.status() }
    assert userStatus.state == User.State.IDLE

    def play = mustSucceed("request game") { client.play() }
    assert play.state == User.State.WAITING

    while (true) {
      userStatus = mustSucceed("status check") { client.status() }
      if (userStatus.state == User.State.WAITING) {
        log.debug "Waiting for players to join..."
        Thread.sleep(1000)
      } else if (userStatus.state == User.State.PLAYING) {
        log.info "Playing can start"
        break
      } else {
        log.error "Error: User is not in WAITING or PLAYING state"
        System.exit(6)
      }
    }

    while (true) {
      def gameState = mustSucceed("game status") { client.gameState(userStatus.activeGameId) }
      if (gameState.result != null) {
        log.info "Game over"
        println gameState.result
        break
      }
      if (gameState.turn) {
        println gameState
        def input = getInput()
        if (input == "y") {
          mustSucceed("take") { client.take(userStatus.activeGameId) }
        } else {
          mustSucceed("take") { client.pass(userStatus.activeGameId) }
        }
      }
      sleep(1000)
    }

  }

  private String getInput() {
    while(true) {
      println "Do you want to take this card? (y/n)"
      def input = System.in.newReader().readLine()

      if (input.toLowerCase() == "y") {
        return "y"
      } else if (input.toLowerCase() == "n") {
        return "n"
      } else {
        println "Invalid input. Please enter 'y' or 'n'"
      }
    }
  }

  private NoThanksService client() {
    NoThanksService client = new Retrofit.Builder()
        .client(new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
              @Override
              Response intercept(Interceptor.Chain chain) throws IOException {
                def request = chain.request().newBuilder()
                    .addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                    .build()
                return chain.proceed(request)
              }
            })
            .build())
        .baseUrl("http://localhost:8080")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NoThanksService.class)
    client
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  private NoThanksService signupClient() {
    NoThanksService signupClient = new Retrofit.Builder()
        .baseUrl("http://localhost:8080")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NoThanksService.class)
    signupClient
  }
}

if (args.length != 2) {
  println("Usage: groovy human.groovy <username> <password>")
  System.exit(1)
}

def username = args[0]
def password = args[1]

new NoThanksCli(username: username, password: password).run()
