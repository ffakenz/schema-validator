# Schema Validator
[![ZIO][Badge-ZIO]][Link-Zio]
[![CI][Badge-CI]][Link-CI]

|Technology   | Version   |
|-------------|---------- |
|Scala        | 2.13      |
|SBT          | 1.7.1     |
|JAVA         | 17        |
|ZIO          | 2.0.2     |

### Repo setup
Something handy to have in your git hooks is the one in `./scripts/githooks/pre-commit`
which validates coding standards when trying to run a `git commit` command.

install pre-commit: https://pre-commit.com/

and launch:
```shell
$ cd scripts/githooks
$ pre-commit install
```

[Badge-ZIO]: https://img.shields.io/badge/zio-2.0-red
[Link-ZIO]: https://zio.dev/
[Badge-CI]: https://github.com/ffakenz/schema-validator/actions/workflows/ci.yml/badge.svg
[Link-CI]: https://github.com/ffakenz/schema-validator/actions/workflows/ci.yml

### Watch Mode
You can start the server and run it in watch mode using `~ reStart` command on the SBT console.
