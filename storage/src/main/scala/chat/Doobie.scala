package chat

import chat.ChatStorage.Service
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import todo.Repository.Name
import todo.{ChatID, Repository, TodoTask}
import zio.Task
import zio.interop.catz._

private[chat] final case class Doobie(xa: Transactor[Task]) extends Service {
  override def add(chatID: ChatID, name: Repository.Name): Task[Unit] =
    SQL
      .create(chatID, name)
      .withUniqueGeneratedKeys[Long]("Id")
      .transact(xa)
      .unit
      .orDie

  override def remove(chatID: ChatID, name: Repository.Name): Task[Unit] =
    SQL
      .delete(chatID, name)
      .run
      .transact(xa)
      .unit
      .orDie

  override def listTasks(chatID: ChatID): Task[Set[Repository.Name]] =
    SQL.getByChat(chatID).to[Set].map(_.map(_.name)).transact(xa).orDie
}

private object SQL {
  def create(chatID: ChatID, name: Name): Update0 =
    sql"""Insert INTO Task (Chat_ID, Task_Name)
        values (${chatID.value},${name.value})
       """.update

  def delete(chatID: ChatID, name: Name): Update0 =
    sql"""
         Delete from Task Where Chat_id = ${chatID.value} and Task_Name = ${name.value}
         """.update

  def getByChat(chatID: ChatID): Query0[TodoTask] =
    sql"""Select * from Task where Chat_id - ${chatID.value}
         |""".query[TodoTask]
}
