package model

object domain {

  trait URI

  trait Document[A] {
    def value: A
  }

  trait Schema[A] {
    def uri: URI
    def spec: A
  }
}
