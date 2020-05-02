package telegram

import canoe.api.models.ChatApi
import log.Logger
import log.Logger.Logger
import canoe.api.{Bot, TelegramClient => Client}
import canoe.models.PrivateChat
import canoe.models.outgoing.TextContent
import todo.ChatID
import zio.{Task, ZIO}
import zio.interop.catz._

private[telegram] final case class Canoe(
    logger: Logger.Service,
    canoeScenarios: CanoeScenarios.Service,
    canoeClient: Client[Task]
) extends TelegramClient.Service {

  implicit val canoe: Client[Task] = canoeClient

  override def start: Task[Unit] =
    logger.info("Start telegram polling") *>
      Bot
        .polling[Task]
        .follow(
          canoeScenarios.add,
          canoeScenarios.del,
          canoeScenarios.help,
          canoeScenarios.start,
          canoeScenarios.list,
          canoeScenarios.update
        )
        .compile
        .drain

  override def broadcastMessage(
      receivers: Set[ChatID],
      message: String
  ): Task[Unit] =
    ZIO
      .foreach(receivers) { chatID =>
        val api = new ChatApi(PrivateChat(chatID.value, None, None, None))
        api.send(TextContent(message))
      }
      .unit
}
