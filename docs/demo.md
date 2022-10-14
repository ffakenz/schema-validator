# Demo

## Use case: schema validation
The potential user has two JSON files in the `./docs` folder:

  - Document: `config.json`

  - Schema: `config-schema.json`

And it expects the Document conforms to the Schema.

To check that it really fits the Schema:

  1. The user should upload the JSON Schema:
  ```shell
  $ curl http://localhost:8080/schema/schema-id -X POST -d @config-schema.json
  ```

  2. The server should respond with status code 201 and:
  ```json
  {"action": "upload", "id": "schema-id", "status": "success"}
  ```

  3. The user should upload the JSON Document to validate it:
  ```shell
  $ curl http://localhost:8080/validate/schema-id -X POST -d @config.json
  ```

  4. The server should "clean" the uploaded JSON Document to remove keys for which the value is `null`

  5. The server should respond with status code 200 and:
  ```json
  {"action": "validate", "id": "schema-id", "status": "success"}
  ```
