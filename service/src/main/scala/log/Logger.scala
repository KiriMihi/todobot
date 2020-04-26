package log

import zio.clock.Clock
import zio.{Has, UIO, URLayer, ZLayer}
import zio.console.{Console => ConsoleZIO}

object Logger {
  type Logger = Has[Service]

  trait Service {
    def trace(message: => String): UIO[Unit]
    def debug(message: => String): UIO[Unit]
    def info(message: => String): UIO[Unit]
    def warn(message: => String): UIO[Unit]
    def error(message: => String): UIO[Unit]
    def error(t: Throwable)(message: => String): UIO[Unit]

  }

  def console: URLayer[Clock with ConsoleZIO, Has[Service]] =
    ZLayer.fromServices[Clock.Service, ConsoleZIO.Service, Service] {
      (clock, console) => Console(clock, console)
    }
}
