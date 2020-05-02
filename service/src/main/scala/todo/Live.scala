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
  override def add(
      chatID: ChatID,
      name: Name,
      numberOfTask: NumberOfTask
  ): Task[Unit] =
    logger.info(s"$chatID added to ${name.value} ") *>
      chatStorage.add(chatID, name, numberOfTask)

  override def remove(chatID: ChatID, numberOfTask: NumberOfTask): Task[Unit] =
    logger.info(s"Chat $chatID removed ${numberOfTask}") *>
      chatStorage.remove(chatID, numberOfTask)

  override def listTasks(chatID: ChatID): Task[Set[TodoTask]] =
    logger.info(s"ChatId $chatID requested tasks") *>
      chatStorage.listTasks(chatID)

  override def count(chatID: ChatID): Task[Int] =
    logger.info(s"ChatId $chatID requested count of tasks") *>
      chatStorage.count(chatID)

  override def update(
      chatId: ChatID,
      numberOfTask: NumberOfTask,
      name: Name
  ): Task[Unit] =
    logger.info(s"ChatId $chatId requested to update") *> chatStorage.update(
      chatId,
      name,
      numberOfTask
    )
}
