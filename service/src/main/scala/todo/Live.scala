package todo

import chat.ChatStorage
import chat.ChatStorage.ChatStorage
import log.Logger
import todo.Repository.Name
import zio.Task

private[todo] final case class Live(
    logger: Logger.Service,
    chatStorage: ChatStorage.Service
) extends TodoLogic.Service {
  override def add(chatID: ChatID, name: Name): Task[Unit] = ???

  override def remove(chatID: ChatID, name: Repository.Name): Task[Unit] = ???

  override def listTasks(chatID: ChatID): Task[Set[Repository.Name]] = ???
}
