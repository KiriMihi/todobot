package http

import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder, Uri}
import org.http4s.client.Client
import org.http4s.circe._
import todo.TodoError
import todo.TodoError.{NotFound, UnexpectedError}
import zio.interop.catz._
import zio.{IO, Task, ZIO}

private[http] final case class Http4s(client: Client[Task])
    extends HttpClient.Service {
  implicit def entityDecoder[A](implicit
      decoder: Decoder[A]
  ): EntityDecoder[Task, A] = jsonOf[Task, A]
  implicit def entityEncoder[A](implicit
      encoder: Encoder[A]
  ): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]

  override def get[T](uri: String)(implicit d: Decoder[T]): IO[TodoError, T] = {
    def call(uri: Uri): IO[TodoError, T] =
      client
        .expect[T](uri)
        .foldM(
          _ => IO.fail(NotFound(uri.renderString)),
          result => ZIO.succeed(result)
        )

    Uri.fromString(uri).fold(_ => IO.fail(UnexpectedError(uri)), call)
  }
}
