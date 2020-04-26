package todo
import chat.ChatStorage
import chat.ChatStorage.ChatStorage
import log.Logger
import log.Logger.Logger
import Repository.Name
import zio.{Has, Task, URLayer, ZLayer}
object TodoLogic {
  type TodoLogic = Has[Service]

  trait Service {
    def add(chatID: ChatID, name: Name): Task[Unit]
    def remove(chatID: ChatID, name: Name): Task[Unit]
    def listTasks(chatID: ChatID): Task[Set[Name]]
  }

  type LiveDeps = Logger with ChatStorage

  def live: URLayer[LiveDeps, Has[Service]] =
    ZLayer.fromServices[Logger.Service, ChatStorage.Service, Service] {
      (logger, chatStorage) => Live(logger, chatStorage)
    }
}
