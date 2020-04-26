package log
import zio.UIO
import zio.clock._
import zio.console.{Console => ConsoleZIO}

private[log] final case class Console(
    clock: Clock.Service,
    console: ConsoleZIO.Service
) extends Logger.Service {
  override def trace(message: => String): UIO[Unit] = print(message)

  override def debug(message: => String): UIO[Unit] = print(message)

  override def info(message: => String): UIO[Unit] = print(message)

  override def warn(message: => String): UIO[Unit] = print(message)

  override def error(message: => String): UIO[Unit] = print(message)

  private def print(message: => String): UIO[Unit] =
    for {
      timestamp <- clock.currentDateTime.orDie
      _ <- console.putStrLn(s"[$timestamp] $message")
    } yield ()

  override def error(t: Throwable)(message: => String): UIO[Unit] =
    for {
      _ <- print(message)
      _ <- console.putStrLn("todo in Console.scala")
    } yield ()
}
