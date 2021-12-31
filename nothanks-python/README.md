# No Thanks Python Client

This is an autonomous player for No Thanks

## Setting up

```shell
python3 -m venv venv
./venv/bin/pip3 install -r requirements.txt
```

## Running

```shell
./venv/bin/python3 python_client.py http://localhost:8080 <username> <password> <brain> <runs>
```

where `brain` is one of

* `AlwaysTake` - Takes any card presented to it
* `PreferPass` - Prefer to pass if possible. Otherwise take any card.
* `random` - Randomly choose to take or pass.
* `tensorforce` - Use TensorForce to choose to take or pass.
* `ppo` - Use PPO to choose to take or pass.