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

# Run the server
You can start the server using:

```shell
$ sbt "server/run"
```

## Run the server using docker
You can publish the docker image of the server  using `docker:publishLocal` command on the SBT console. Once the image is successfully publish, simply run the server using the [docker-compose.yaml](./docker/docker-compose.yml)
```sh
$ .docker-compose run schema-validator
```

## Demo
Give the [demo](./docs/demo.md) a test drive!

## API docs
Run the server and next, fire up swagger-ui by using using the [docker-compose.yaml](./docker/docker-compose.yml)
```sh
$ .docker-compose run swagger
```
It will automatically pick up and load the [openapi.yaml](./docker/openapi.yaml) file.
Now you can give the demo a test drive using it.

To check the OpenApi docs are valid and well formed you can to install and run [dredd](https://dredd.org/en/latest/) as following:
```shell
$ npm install -g dredd

$ dredd openapi.yaml localhost:9090 --dry-run
```

> [wip] Generate OpenApi docs by calling the swagger endpoint at:
> - `/docs/schema-validator.yaml`
> - `/docs/health.yaml`
