package todo
import todo.client.ChatID
import todo.client.Repository.Name
import zio.Task
object TodoLogic {

  trait Service {
    def add(chatID: ChatID, name: Name): Task[Unit]
    def remove(chatID: ChatID, name: Name): Task[Unit]
    def listTasks(chatID: ChatID): Task[Set[Name]]

  }
}
