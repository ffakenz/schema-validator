package model

object domain {

  trait URI
  trait SchemaSpec

  trait Document[A] {
    def value: A
  }

  trait Schema[A] {
    def uri: URI
    def spec: SchemaSpec
  }
}
