package telegram

import canoe.api.{Scenario, TelegramClient}
import todo.{ChatID, NumberOfTask, TodoLogic}
import todo.TodoLogic.TodoLogic
import zio.{Task, ZIO}
import canoe.api._
import canoe.models.Chat
import canoe.models.messages.TextMessage
import canoe.syntax._
import todo.Repository.Name

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

  override def add: Scenario[Task, Unit] =
    for {
      chat <- Scenario.expect(command("add").chat)
      _ <- Scenario.eval(chat.send("Please add your task"))
      userInput <- Scenario.expect(text)
      - <- Scenario.eval(
        chat.send("task is added") *> todoLogic
          .add(ChatID(chat.id), Name(userInput))
      )
    } yield ()

  override def del: Scenario[Task, Unit] =
    for {
      chat <- Scenario.expect(command("del").chat)
      - <- Scenario.eval(chat.send("Please write number of your task"))
      //tasks <- todoLogic.listTasks(ChatID(chat.id))
      //userInput <- Scenario.expect(text)
//      _ <- {
//        Scenario.eval {
//          val userInputFormatted = toInt(userInput);
//          val result =
//            if (userInputFormatted == None) chat.send("Wrong  digit")
//            else if (tasks.isEmpty) chat.send("Nothing to delete.")
//            else if (tasks.filter(x => x.id == userInput).isEmpty)
//              chat.send("Task does not exist")
//            else
//              chat.send("Removing your task") *> todoLogic.remove(
//                ChatID(chat.id),
//                NumberOfTask(userInputFormatted.get)
//              )
//          result
//        }
//      }
    } yield ()

  override def list: Scenario[Task, Unit] =
    for {
      chat <- Scenario.expect(command("list").chat)
      tasks <- Scenario.eval(todoLogic.listTasks(ChatID(chat.id)))
      _ <- {
        val result =
          if (tasks.isEmpty) chat.send("You don't tasks set")
          else
            chat.send("Listing your tasks") *> ZIO.foreach(tasks)(task =>
              chat.send(task.id.value + " - " + task.taskName.value)
            )
        Scenario.eval(result)
      }
    } yield ()

  private def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }
}
