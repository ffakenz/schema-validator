package service

import model.domain._

trait DocumentCleaner[A, F[_]] {

  def clean(document: Document[A]): F[Document[A]]

}
