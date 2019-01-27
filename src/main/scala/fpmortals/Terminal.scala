package fpmortals

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn._

object Terminal {

  trait Terminal[C[_]] {
    def read(): C[String]
    def write(in: String): C[Unit]
  }

  type Now[X] = X

  object TerminalSync extends Terminal[Now] {
    def read(): Now[String] = readLine
    def write(in: String): Now[Unit] = println(read())
  }

  object TerminalAsync extends Terminal[Future] {
    def read(): Future[String] = Future(readLine)
    def write(in: String): Future[Unit] = read().flatMap(s => Future(println(s)))
  }

  trait Execution[C[_]] {
    def chain[A, B](c: C[A])(f: A => C[B]): C[B]
    def create[A](a: A): C[A]
  }

  object Execution {
    implicit class Ops[A, C[_]](c: C[A]) {
      def flatMap[B](f: A => C[B])(implicit e: Execution[C]): C[B] =
        e.chain[A, B](c)(f)
      def map[B](f: A => B)(implicit e: Execution[C]): C[B] =
        e.chain[A, B](c)((a: A) => e.create(f(a)))
    }
  }

  import Execution._

  object ExecutionTerminalSync extends Execution[Now] {
    def chain[A, B](c: A)(f: A => B): B = f(c)
    def create[A](a: A): A = a
  }

  object ExecutionTerminalAsync extends Execution[Future] {
    def chain[A, B](c: Future[A])(f: A => Future[B]): Future[B] = c.flatMap(f)
    def create[A](a: A): Future[A] = Future(a)
  }

  def echo[C[_]](implicit t: Terminal[C], e: Execution[C]): C[String] =
    t.read.flatMap {in: String => t.write(in).map(_ => in) }

  def echo2[C[_]](implicit t: Terminal[C], e: Execution[C]): C[String] =
    for {
      in <- t.read
      _ <- t.write(in)
    } yield in
  
}
