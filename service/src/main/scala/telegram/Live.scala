package telegram

import canoe.api.{Scenario, TelegramClient}
import todo.TodoLogic.TodoLogic
import zio.Task
import canoe.api._
import canoe.models.Chat
import canoe.models.messages.TextMessage
import canoe.syntax._
import todo.TodoLogic

private[telegram] final case class Live(
    todoLogic: TodoLogic.Service,
    canoeClient: TelegramClient[Task]
) extends CanoeScenarios.Service {
  private implicit val client: TelegramClient[Task] = canoeClient
  override def start: Scenario[Task, Unit] =
    for {
      chat <- Scenario.expect(command("start").chat)
      _ <- broadcastHelp(chat)
    } yield ()

  override def help: Scenario[Task, Unit] =
    for {
      chat <- Scenario.expect(command("help").chat)
      _ <- broadcastHelp(chat)
    } yield ()

  private def broadcastHelp(chat: Chat): Scenario[Task, TextMessage] = {
    val helpText =
      """|/help Shows help menu
        |/add Add task
        |/del Remove task
        |/list List of all tasks
        """.stripMargin
    Scenario.eval(chat.send(helpText))
  }

  override def add: Scenario[Task, Unit] = ???

  override def del: Scenario[Task, Unit] = ???

  override def list: Scenario[Task, Unit] = ???
}
