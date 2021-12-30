import sys
import time
import requests
from requests.auth import HTTPBasicAuth

assert len(sys.argv) == 4
base_url = sys.argv[1]
username = sys.argv[2]
password = sys.argv[3]

auth = HTTPBasicAuth(username, password)


def signin_or_signup():
    test_auth = requests.get(f'{base_url}/v1/users/status', auth=auth)
    if test_auth.status_code == 401:
        signup = requests.post(f'{base_url}/v1/users/create?username={username}&password={password}')
        if signup.status_code != 200:
            raise Exception(f"Unexpected response: {signup.status_code}")
        print("Signup succeeded")
    else:
        print("Login succeeded")


def user_should_be_idle():
    status = requests.get(f'{base_url}/v1/game/status', auth=auth).json()
    if status['state'] != 'IDLE':
        raise Exception(f"Unexpected state: {status['state']}")


def start_new_game():
    status = requests.post(f'{base_url}/v1/game/play', auth=auth).json()
    if status['state'] != 'WAITING':
        raise Exception(f"Unexpected state: {status['state']}")


def wait_for_game_to_start():
    while True:
        status = requests.get(f'{base_url}/v1/game/status', auth=auth).json()
        if status['state'] == 'PLAYING':
            return status['activeGameId']
        time.sleep(0.2)


def should_take(game_state):
    return True


def play_game():
    user_should_be_idle()
    start_new_game()
    game_id = wait_for_game_to_start()
    last_state = {}
    print("")
    while True:
        game_state = requests.get(f'{base_url}/v1/game/state?gameId={game_id}', auth=auth).json()
        if last_state != game_state:
            print(game_state)
            last_state = game_state
        if 'result' in game_state:
            if game_state['result']['winner'] == username:
                return 1
            else:
                return -1
        else:
            if game_state['turn']:
                if should_take(game_state):
                    requests.post(f'{base_url}/v1/game/take?gameId={game_id}', auth=auth)
                else:
                    requests.post(f'{base_url}/v1/game/pass?gameId={game_id}', auth=auth)


def wait_to_idle():
    while True:
        if requests.get(f'{base_url}/v1/game/status', auth=auth).json()['state'] == 'IDLE':
            break
        time.sleep(0.1)


def main():
    signin_or_signup()
    game_count = 0
    my_score = 0
    wins = 0
    while game_count < 20:
        game_count = game_count + 1
        game_score = play_game()
        if game_score > 0:
            wins = wins + 1
        my_score = my_score + game_score
        wait_to_idle()
    print("")
    print(f"Wins: {wins}/{game_count} Plays")
    print(f"My Score: {my_score}")


main()
