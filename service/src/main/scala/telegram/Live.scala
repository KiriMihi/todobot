package telegram

import canoe.api.{Scenario, TelegramClient}
import todo.{ChatID, NumberOfTask, TodoLogic}
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
        |/update Update task
        """.stripMargin
    Scenario.eval(chat.send(helpText))
  }

  override def add: Scenario[Task, Unit] =
    for {
      chat <- Scenario.expect(command("add").chat)
      _ <- Scenario.eval(chat.send("Please add your task"))
      userInput <- Scenario.expect(text)
      lastNumberOfTask <- Scenario.eval(todoLogic.count(ChatID(chat.id)))
      _ <- Scenario.eval(
        chat.send("task is added") *> todoLogic
          .add(
            ChatID(chat.id),
            Name(userInput),
            NumberOfTask(lastNumberOfTask + 1)
          )
      )
    } yield ()

  override def del: Scenario[Task, Unit] =
    for {
      chat <- Scenario.expect(command("del").chat)
      _ <-
        Scenario.eval(chat.send("Please write number of your task to delete"))
      result <- ifExists(chat)
      _ <- Scenario.eval(
        chat.send("Removing your task") *> todoLogic
          .remove(ChatID(chat.id), NumberOfTask(result._2))
      )
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
              chat.send(
                task.ordering.value.toString + " - " + task.taskName.value
              )
            )
        Scenario.eval(result)
      }
    } yield ()

  override def update: Scenario[Task, Unit] =
    for {
      chat <- Scenario.expect(command("update").chat)
      _ <-
        Scenario.eval(chat.send("Please write number of your task to update"))
      result <- ifExists(chat)
      _ <- Scenario.eval(chat.send("Please write a new name of the task"))
      secondInput <- enterText()
      _ <- Scenario.eval(
        chat.send("Updating your task") *> todoLogic
          .update(ChatID(chat.id), NumberOfTask(result._2), Name(secondInput))
      )
    } yield ()

  def provideDigit(chat: Chat): Scenario[Task, Int] =
    for {
      number <- enterText()
      r <-
        if (toInt(number) != None) Scenario.pure[Task](toInt(number).get)
        else
          Scenario.eval(
            chat.send("digit is not correct. Try again")
          ) >> provideDigit(chat)
    } yield r

  def ifExists(chat: Chat): Scenario[Task, (Boolean, Int)] =
    for {
      number <- provideDigit(chat)
      tasks <- Scenario.eval(todoLogic.listTasks(ChatID(chat.id)))
      r <-
        if (tasks.isEmpty)
          Scenario.eval(
            chat.send("Task does not exist. Try again")
          ) >> ifExists(chat)
        else if (tasks.filter(x => x.ordering.value == number).isEmpty)
          Scenario.eval(
            chat.send("Task does not exist. Try again")
          ) >> ifExists(chat)
        else Scenario.pure[Task](true, number)

    } yield r

  private def toInt(s: String): Option[Int] = {
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }
  }
  def enterText(): Scenario[Task, String] =
    for {
      message <- Scenario.expect(textMessage)
    } yield message.text

}
