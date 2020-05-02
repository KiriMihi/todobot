package todo
import chat.ChatStorage
import chat.ChatStorage.ChatStorage
import log.Logger
import log.Logger.Logger
import Repository.Name
import zio.{Has, IO, Task, URLayer, ZLayer}
object TodoLogic {
  type TodoLogic = Has[Service]

  trait Service {
    def add(chatID: ChatID, name: Name, numberOfTask: NumberOfTask): Task[Unit]
    def remove(chatID: ChatID, numberOfTask: NumberOfTask): Task[Unit]
    def listTasks(chatID: ChatID): Task[Set[TodoTask]]
    def count(chatID: ChatID): Task[Int]
    def update(
        chatId: ChatID,
        numberOfTask: NumberOfTask,
        name: Name
    ): Task[Unit]
  }

  type LiveDeps = Logger with ChatStorage

  def live: URLayer[LiveDeps, Has[Service]] =
    ZLayer.fromServices[Logger.Service, ChatStorage.Service, Service] {
      (logger, chatStorage) => Live(logger, chatStorage)
    }
}
