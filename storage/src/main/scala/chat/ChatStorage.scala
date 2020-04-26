package chat

import doobie.util.transactor.Transactor
import todo.Repository.Name
import chat.Doobie
import todo.ChatID
import zio.{Has, Ref, Task, ZLayer}

object ChatStorage {

  type ChatStorage = Has[Service]
  type TodoMap = Map[ChatID, Set[Name]]

  trait Service {
    def add(chatID: ChatID, name: Name): Task[Unit]
    def remove(chatID: ChatID, name: Name): Task[Unit]
    def listTasks(chatID: ChatID): Task[Set[Name]]

  }
  val doobie: ZLayer[Has[Transactor[Task]], Nothing, Has[Service]] =
    ZLayer.fromService[Transactor[Task], Service] { xa: Transactor[Task] =>
      Doobie(xa)
    }

}
