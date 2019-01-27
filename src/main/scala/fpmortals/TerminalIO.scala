package fpmortals

import scala.io.StdIn._

object TerminalIO {
  import Terminal._

  final class IO[A](val interpret: () => A) {
    def map[B](f: A => B): IO[B] =
      IO(f(interpret()))
    def flatMap[B](f: A => IO[B]): IO[B] =
      IO(f(interpret()).interpret())
  }

  object IO {
    def apply[A](a: => A): IO[A] = new IO(() => a)
  }

  import IO._

  implicit object TerminalIO extends Terminal[IO] {
    def read(): IO[String] = IO(readLine)
    def write(in: String): IO[Unit] = IO(println(in))
  }

  implicit object ExecutionIO extends Execution[IO] {
    def chain[A, B](c: IO[A])(f: A => IO[B]): IO[B] = c.flatMap(f)
    def create[A](a: A): IO[A] = IO(a)
  }

  val delayed: IO[String] = echo[IO]

}
