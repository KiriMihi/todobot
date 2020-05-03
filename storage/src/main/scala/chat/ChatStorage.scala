package chat

import doobie.util.transactor.Transactor
import todo.Repository.Name
import chat.Doobie
import todo.{ChatID, NumberOfTask, TodoTask, UserID}
import zio.{Has, Ref, Task, ZLayer}

object ChatStorage {

  type ChatStorage = Has[Service]
  type TodoMap = Map[ChatID, Set[Name]]

  trait Service {
    def add(
        chatID: ChatID,
        name: Name,
        numberOfTask: NumberOfTask,
        userID: UserID
    ): Task[Unit]
    def remove(
        chatID: ChatID,
        numberOfTask: NumberOfTask,
        userID: UserID
    ): Task[Unit]
    def listTasks(chatID: ChatID): Task[Set[TodoTask]]
    def listUserTasks(chatID: ChatID, userID: UserID): Task[Set[TodoTask]]
    def hasTaskExist(
        chatID: ChatID,
        numberOfTask: NumberOfTask
    ): Task[Set[TodoTask]]

    def count(chatID: ChatID): Task[Int]
    def update(
        chatID: ChatID,
        name: Name,
        numberOfTask: NumberOfTask,
        userID: UserID
    ): Task[Unit]

  }
  val doobie: ZLayer[Has[Transactor[Task]], Nothing, Has[Service]] =
    ZLayer.fromService[Transactor[Task], Service] { xa: Transactor[Task] =>
      Doobie(xa)
    }

}
