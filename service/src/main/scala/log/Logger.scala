package log

import zio.test.FailureRenderer.FailureMessage.Message
import zio.{Has, UIO}

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
}
