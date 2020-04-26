package telegram
import canoe.api.{TelegramClient => Client}
import log.Logger
import log.Logger._
import telegram.CanoeScenarios.CanoeScenarious
import todo.ChatID
import zio._
import zio.macros.access.accessible

@accessible
object TelegramClient {
  type TelegramClient = Has[Service]

  trait Service {
    def start: Task[Unit]
    def broadcastMessage(receivers: Set[ChatID], mesaage: String): Task[Unit]
  }

  type CanoeDeps = Has[Client[Task]] with Logger with CanoeScenarious

  def canoe: URLayer[CanoeDeps, Has[Service]] =
    ZLayer.fromServices[Client[
      Task
    ], Logger.Service, CanoeScenarios.Service, Service] {
      (client, logger, canoeScenarios) => Canoe(logger, canoeScenarios, client)
    }
}
