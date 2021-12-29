# NoThanks Server

A Server that exposes the NoThanks game as a REST API.

Humans can play this game too, but the objective is to make it possible for bots to play.

## Running locally

Launch PostgreSQL.

```shell
docker run --rm \
    --name postgres \
    -e POSTGRES_PASSWORD=mysecretpassword \
    -d \
    -p 5432:5432 \
    postgres
```

Run the server

```shell
./gradlew :nothanks-server:run
```

## Connecting

Visit the [api docs](http://localhost:8080/swagger/views/rapidoc/).

## Fastest way to connect

Build the client shadowjar.

```shell
./gradlew :nothanks-cli:clean; ./gradlew :nothanks-cli:shadowJar;

java \
    -jar nothanks-cli/build/libs/nothanks-*-all.jar \
    -u rahulsom1 -p rahulsom1 \
    -b <Human|AlwaysTake|PreferPass>
```

`Human` asks you what to do every time it's your turn.

`AlwaysTake` always takes the card.

`PreferPass` will pass as long as it has tokens.
If not, it will take.

You will need to run multiple clients - at least 3 for the game to start.