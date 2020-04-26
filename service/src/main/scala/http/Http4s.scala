//package http
//
//import io.circe.Decoder
//import org.http4s.Uri
//import org.http4s.client.Client
//import todo.TodoError
//import todo.TodoError.NotFound
//import zio.interop.catz._
//import zio.{IO, Task, ZIO}
//
//private[http] final case class Http4s(client: Client[Task]) extends HttpClient.Service {
//  override def get[T](uri: String)(implicit d: Decoder[T]): IO[TodoError, T] = {
//    def call(uri: Uri) : IO[TodoError, T] =
//      client.expect[T](uri)
//      .foldM(_ => IO.fail(NotFound(uri.renderString)), result => ZIO.succeed(result))
//
//    Uri.fromString(uri)
//      .fold(_ => IO.fail(Mailform)
//  }
//}
