package chat

import chat.ChatStorage.Service
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import todo.Repository.Name
import todo.{ChatID, NumberOfTask, Repository, TodoTask}
import zio.{IO, Task, ZIO}
import zio.interop.catz._

private[chat] final case class Doobie(xa: Transactor[Task]) extends Service {
  override def add(chatID: ChatID, name: Repository.Name): Task[Unit] =
    SQL
      .create(chatID, name)
      .withUniqueGeneratedKeys[Long]("Id")
      .transact(xa)
      .unit
      .orDie

  override def remove(chatID: ChatID, numberOfTask: NumberOfTask): Task[Unit] =
    SQL
      .delete(chatID, numberOfTask)
      .run
      .transact(xa)
      .unit
      .orDie

  override def listTasks(chatID: ChatID): Task[Set[TodoTask]] =
    SQL.getByChat(chatID).to[Set].transact(xa).orDie

  override def hasTaskExist(
      chatID: ChatID,
      numberOfTask: NumberOfTask
  ): Task[Set[TodoTask]] =
    SQL.getByChat(chatID).to[Set].transact(xa).orDie
}

private object SQL {
  def create(chatID: ChatID, name: Name): Update0 =
    sql"""Insert INTO Task (Chat_ID, Task_Name)
        values (${chatID.value},${name.value})
       """.update

  def delete(chatID: ChatID, numberOfTask: NumberOfTask): Update0 =
    sql"""
         Delete from Task Where Chat_id = ${chatID.value} and Task_Name = ${numberOfTask.value}
         """.update

  def getByChat(chatID: ChatID): Query0[TodoTask] =
    sql"""Select * from Task where CHAT_ID = ${chatID.value}"""
      .query[TodoTask]
}
