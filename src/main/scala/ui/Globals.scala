package ui

object Globals {
  val uiFont: Int = 16

  def tryWithResource[A <: AutoCloseable, B](closeable: A)(f: A => B): B = {
    try {
      f(closeable)
    } finally {
      closeable.close()
    }
  }
}
