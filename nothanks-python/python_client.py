import os
import sys
import time
import requests
from requests.auth import HTTPBasicAuth
from tensorforce import Agent

assert len(sys.argv) == 6
base_url = sys.argv[1]
username = sys.argv[2]
password = sys.argv[3]
brain = sys.argv[4]
if brain not in ['AlwaysTake', 'PreferPass', 'random', 'tensorforce', 'ppo', 'dqn', 'ddqn']:
    raise ValueError(f'brain must be one of: {brain}')
runs = int(sys.argv[5])

auth = HTTPBasicAuth(username, password)
should_print_game = False
if os.environ.get('PRINT_GAME') == '1':
    should_print_game = True


def signin_or_signup():
    """
    Attempts to sign in with known username/password.
    If sign in fails, signs up.
    If sign up fails, aborts.
    """
    test_auth = requests.get(f'{base_url}/v1/users/status', auth=auth)
    if test_auth.status_code == 401:
        signup = requests.post(f'{base_url}/v1/users/create?username={username}&password={password}')
        if signup.status_code != 200:
            raise Exception(f"Unexpected response: {signup.status_code}")
        print("Signup succeeded")
    else:
        print("Login succeeded")


def user_should_be_idle():
    '''
    Checks user state.
    If user is not IDLE, aborts.
    '''
    status = requests.get(f'{base_url}/v1/game/status', auth=auth).json()
    if status['state'] != 'IDLE':
        raise Exception(f"Unexpected state: {status['state']}")


def start_new_game():
    '''
    Requests to start a new game.
    If response doesn't say 'WAITING', aborts.
    '''
    status = requests.post(f'{base_url}/v1/game/play', auth=auth).json()
    if status['state'] != 'WAITING':
        raise Exception(f"Unexpected state: {status['state']}")


def wait_for_game_to_start():
    '''
    Waits until game starts.
    :return: active game id
    '''
    while True:
        time.sleep(0.2)
        status = requests.get(f'{base_url}/v1/game/status', auth=auth).json()
        if status['state'] == 'PLAYING':
            return status['activeGameId']


def should_take(game_state):
    return True


def to_tf_state(game_state):
    '''
    Converts game state to tensorforce state.

    It's a dictionary with observation=tuple()
    The values of the tuple are as follows:

    * 0-32: cards on the stack where

      * 0: me
      * 1-6: other players
      * 54: top card
      * 55: hidden card
    * 33: tokens on current card
    * 34: my tokens

    :param game_state: as returned by api call
    :return: game state to be fed to tensorforce
    '''
    ret = [54] * 35
    top_card_idx = game_state['topCard'] - 3
    ret[top_card_idx] = 53
    ret[34] = game_state['myTokens']
    ret[33] = game_state['tokensOnCard']

    if 'myCards' in game_state:
        for card in game_state['myCards']:
            ret[card - 3] = 0

    other_player_idx = 0
    for other_player in game_state['otherCards']:
        other_player_idx = other_player_idx + 1
        if 'cards' in other_player:
            for card in other_player['cards']:
                ret[card - 3] = other_player_idx

    return dict(observation=tuple(ret))


def to_row(wi, u, a, c, t, scores):
    if wi == u:
        if u == username:
            w = "++"
        else:
            w = "+"
    else:
        w = " "

    my_score = ''.rjust(4)
    if scores is not None:
        for s in scores:
            if s['name'] == u:
                my_score = str(s['score']).rjust(4)

    ua = f"{u} ({a})"
    if t > 0:
        ts = f" - {'*' * t}"
    else:
        ts = ""

    return f"{w.ljust(3)}{ua.ljust(25)}{my_score} - {c}{ts}"


def print_game(game_state):
    '''
    Turns game_state to a string that can be printed.
    :param game_state: game state as received from api
    :return: String representation of game state
    '''
    if 'result' in game_state:
        winner = game_state['result']['winner']
        scores = game_state['result']['scores']
    else:
        winner = None
        scores = None

    ret = ''
    ret = ret + ('X' * game_state['stackSize'])
    if 'topCard' in game_state:
        ret = ret + ' ' + str(game_state['topCard']) + ' ' + ('*' * game_state['tokensOnCard']) + '\n'

    if 'myCards' in game_state:
        ret = ret + to_row(winner, username, brain, game_state['myCards'], game_state['myTokens'], scores)
    else:
        ret = ret + to_row(winner, username, brain, [], game_state['myTokens'], scores)

    for other_player in game_state['otherCards']:
        if 'cards' in other_player:
            ret = ret + '\n' + to_row(winner, other_player['name'], '', other_player['cards'], 0, scores)
        else:
            ret = ret + '\n' + to_row(winner, other_player['name'], '', [], 0, scores)
    return ret


def play_game_with_agent(agent):
    game_id = wait_for_game_to_start()

    should_observe = False
    while True:
        game_state = requests.get(f'{base_url}/v1/game/state?gameId={game_id}', auth=auth).json()

        if 'result' in game_state:
            print(print_game(game_state))
            is_winner = game_state['result']['winner'] == username

            if is_winner:
                agent.observe(terminal=True, reward=1)
                return 1
            else:
                agent.observe(terminal=True, reward=-1)
                return -1

        else:
            if game_state['turn']:
                if should_print_game:
                    print(print_game(game_state))
                top_card = game_state['topCard']
                tokens_on_card = game_state['tokensOnCard']
                if game_state['canPass']:
                    if should_observe:
                        agent.observe(terminal=False)
                    action = agent.act(to_tf_state(game_state))[0]
                    if brain == 'AlwaysTake':
                        action = True
                    elif brain == 'PreferPass':
                        action = False

                    should_observe = True
                    if action:
                        # print(f"  -> Advice is Take {top_card} with {tokens_on_card} tokens.")
                        requests.post(f'{base_url}/v1/game/take?gameId={game_id}', auth=auth)
                    else:
                        # print(f"  -> Advice is Pass {top_card} with {tokens_on_card} + 1 tokens.")
                        requests.post(f'{base_url}/v1/game/pass?gameId={game_id}', auth=auth)

                else:
                    # print(f"  -> Cannot Pass. Will Take {top_card} with {tokens_on_card} tokens.")
                    requests.post(f'{base_url}/v1/game/take?gameId={game_id}', auth=auth)


def wait_to_idle():
    '''
    Waits until user is idle.
    '''
    while True:
        if requests.get(f'{base_url}/v1/game/status', auth=auth).json()['state'] == 'IDLE':
            break
        time.sleep(0.1)


def make_agent():
    if brain == 'tensorforce':
        return Agent.create(
            agent='tensorforce',
            states=dict(
                observation=dict(type="int", shape=35, num_values=55),
            ),
            actions=dict(type="bool", shape=1),
            update=dict(unit='timesteps', batch_size=64),
            optimizer=dict(type='adam', learning_rate=1e-3),
            objective='policy_gradient',
            reward_estimation=dict(horizon=20)
        )
    elif brain == 'ppo':
        return Agent.create(
            agent='ppo',
            states=dict(
                observation=dict(type="int", shape=35, num_values=55),
            ),
            actions=dict(type="bool", shape=1),
            batch_size=10, learning_rate=1e-3,
            max_episode_timesteps=100,
        )
    elif brain == 'dqn':
        return Agent.create(
            agent='dqn',
            memory=10000,
            states=dict(
                observation=dict(type="int", shape=35, num_values=55),
            ),
            actions=dict(type="bool", shape=1),
            batch_size=10, learning_rate=1e-3,
            max_episode_timesteps=100,
        )
    elif brain == 'ddqn':
        return Agent.create(
            agent='ddqn',
            memory=10000,
            states=dict(
                observation=dict(type="int", shape=35, num_values=55),
            ),
            actions=dict(type="bool", shape=1),
            batch_size=10, learning_rate=1e-3,
            max_episode_timesteps=100,
        )
    elif brain == 'random':
        return Agent.create(
            agent='random',
            states=dict(
                observation=dict(type="int", shape=35, num_values=55),
            ),
            actions=dict(type="bool", shape=1),
        )
    elif brain == 'AlwaysTake':
        return Agent.create(
            agent='constant',
            states=dict(
                observation=dict(type="int", shape=35, num_values=55),
            ),
            actions=dict(type="bool", shape=1),
        )
    elif brain == 'PreferPass':
        return Agent.create(
            agent='constant',
            states=dict(
                observation=dict(type="int", shape=35, num_values=55),
            ),
            actions=dict(type="bool", shape=1),
        )
    else:
        raise Exception(f'Unknown brain: {brain}')


def main():
    if runs > 0:
        signin_or_signup()
    game_count = 0
    wins = 0

    if os.path.exists(f'data/{brain}-{username}'):
        agent = Agent.load(f'data/{brain}-{username}')
    else:
        agent = make_agent()

    history = []

    while game_count < runs:
        game_count = game_count + 1
        user_should_be_idle()
        start_new_game()
        game_score = play_game_with_agent(agent)
        if game_score > 0:
            wins = wins + 1
            history.append("W")
        else:
            history.append(".")
        print(f"Wins: {wins}/{game_count}:  {''.join(history)}\n\n")
        wait_to_idle()

    agent.save(f'data/{brain}-{username}')
    agent.close()


if __name__ == '__main__':
    main()
