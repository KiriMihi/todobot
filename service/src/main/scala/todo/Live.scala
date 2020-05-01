package todo

import chat.ChatStorage
import chat.ChatStorage.ChatStorage
import log.Logger
import todo.Repository.Name
import zio.{IO, Task}

private[todo] final case class Live(
    logger: Logger.Service,
    chatStorage: ChatStorage.Service
) extends TodoLogic.Service {
  override def add(chatID: ChatID, name: Name): Task[Unit] =
    logger.info(s"$chatID added to ${name.value} ") *>
      chatStorage.add(chatID, name)

  override def remove(chatID: ChatID, numberOfTask: NumberOfTask): Task[Unit] =
    logger.info(s"Chat $chatID removed ${numberOfTask}") *>
      chatStorage.remove(chatID, numberOfTask)

  override def listTasks(chatID: ChatID): Task[Set[TodoTask]] =
    logger.info(s"ChatId $chatID requested tasks") *>
      chatStorage.listTasks(chatID)

  override def hasTaskExist(
      numberOfTask: NumberOfTask
  ): IO[TodoError, TodoTask] = ???
}
