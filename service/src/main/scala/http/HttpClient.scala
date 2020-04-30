package http

import io.circe.Decoder
import org.http4s.client.Client
import zio.{Has, IO, URLayer, ZLayer, Task}
import todo.TodoError

object HttpClient {
  type HttpClient = Has[Service]

  trait Service {
    def get[T](uri: String)(implicit d: Decoder[T]): IO[TodoError, T]
  }

  def http4s: URLayer[Has[Client[Task]], Has[Service]] =
    ZLayer.fromService[Client[Task], Service] { http4sClient: Client[Task] =>
      Http4s(http4sClient)
    }
}
