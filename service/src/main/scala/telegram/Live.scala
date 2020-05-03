package telegram

import canoe.api.{Scenario, TelegramClient, _}
import todo.{ChatID, NumberOfTask, TodoLogic, UserID}
import zio.{Task, ZIO}
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
      info <- Scenario.expect(command("add"))
      _ <- Scenario.eval(info.chat.send("Please add your task"))
      userInput <- Scenario.expect(text)
      lastNumberOfTask <- Scenario.eval(todoLogic.count(ChatID(info.chat.id)))
      _ <- Scenario.eval(
        info.chat.send("task is added") *> todoLogic
          .add(
            ChatID(info.chat.id),
            Name(userInput),
            NumberOfTask(lastNumberOfTask + 1),
            UserID(info.from.get.id)
          )
      )
    } yield ()

  override def del: Scenario[Task, Unit] =
    for {
      info <- Scenario.expect(command("del"))
      _ <- Scenario.eval(
        info.chat.send("Please write number of your task to delete")
      )
      result <- ifExists(info.chat)
      _ <- Scenario.eval(
        info.chat.send("Removing your task") *> todoLogic
          .remove(
            ChatID(info.chat.id),
            NumberOfTask(result._2),
            UserID(info.from.get.id)
          )
      )
    } yield ()

  override def list: Scenario[Task, Unit] =
    for {
      info <- Scenario.expect(command("list"))
      tasks <- Scenario.eval(
        todoLogic.listUserTasks(ChatID(info.chat.id), UserID(info.from.get.id))
      )
      _ <- {
        val result =
          if (tasks.isEmpty) info.chat.send("You don't tasks set")
          else {
            //val listTasks = ZIO.foreach(tasks)(task => task.taskName.value)
            info.chat.send("Listing your tasks") *> ZIO.foreach(tasks)(task =>
              info.chat.send(
                task.ordering.value.toString + " - " + task.taskName.value
              )
            )
          }
        Scenario.eval(result)
      }
    } yield ()

  override def update: Scenario[Task, Unit] =
    for {
      info <- Scenario.expect(command("update"))
      _ <- Scenario.eval(
        info.chat.send("Please write number of your task to update")
      )
      result <- ifExists(info.chat)
      _ <- Scenario.eval(info.chat.send("Please write a new name of the task"))
      secondInput <- enterText()
      _ <- Scenario.eval(
        info.chat.send("Updating your task") *> todoLogic
          .update(
            ChatID(info.chat.id),
            NumberOfTask(result._2),
            Name(secondInput),
            UserID(info.from.get.id)
          )
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

  override def alltasks: Scenario[Task, Unit] =
    for {
      chat <- Scenario.expect(command("alltasks").chat)
      tasks <- Scenario.eval(todoLogic.listTasks(ChatID(chat.id)))
      _ <- {
        val result =
          if (tasks.isEmpty) chat.send("You don't tasks set")
          else
            chat.send("Listing all tasks") *> ZIO.foreach(tasks)(task =>
              chat.send(task.taskName.value)
            )
        Scenario.eval(result)
      }
    } yield ()
}
