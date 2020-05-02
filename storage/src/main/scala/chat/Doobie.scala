package chat

import chat.ChatStorage.Service
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import todo.Repository.Name
import todo.TodoError.UnexpectedError
import todo.{ChatID, NumberOfTask, Repository, TodoTask}
import zio.{Has, IO, Task, ZIO, ZLayer}
import zio.interop.catz._

private[chat] final case class Doobie(xa: Transactor[Task]) extends Service {
  override def add(
      chatID: ChatID,
      name: Repository.Name,
      numberOfTask: NumberOfTask
  ): Task[Unit] =
    SQL
      .create(chatID, name, numberOfTask)
      .withUniqueGeneratedKeys[Long]("Id")
      .transact(xa)
      .unit
      .orDie

  override def remove(
      chatID: ChatID,
      numberOfTask: NumberOfTask
  ): Task[Unit] = {
    SQL
      .delete(chatID, numberOfTask)
      .run
      .transact(xa)
      .unit
      .orDie
  }
  override def count(chatID: ChatID): Task[Int] =
    SQL
      .count(chatID)
      .option
      .transact(xa)
      .foldM(
        err => Task.fail(err),
        maybeCount =>
          Task.require(UnexpectedError(chatID.value.toString))(
            Task.succeed(maybeCount)
          )
      )

  override def listTasks(chatID: ChatID): Task[Set[TodoTask]] =
    SQL.getByChat(chatID).to[Set].transact(xa).orDie

  override def hasTaskExist(
      chatID: ChatID,
      numberOfTask: NumberOfTask
  ): Task[Set[TodoTask]] =
    SQL.getByChat(chatID).to[Set].transact(xa).orDie

  override def update(
      chatID: ChatID,
      name: Name,
      numberOfTask: NumberOfTask
  ): Task[Unit] =
    SQL.update(chatID, numberOfTask, name).run.transact(xa).unit.orDie

}

private object SQL {
  def create(chatID: ChatID, name: Name, numberOfTask: NumberOfTask): Update0 =
    sql"""Insert INTO Task (Chat_ID, Task_Name, Ordering)
        values (${chatID.value},${name.value}, ${numberOfTask.value})
       """.update

  def delete(chatID: ChatID, numberOfTask: NumberOfTask): Update0 =
    sql"""
         Delete from Task Where Chat_id = ${chatID.value} and Ordering = ${numberOfTask.value}
         """.update

  def update(chatID: ChatID, numberOfTask: NumberOfTask, name: Name): Update0 =
    sql"""
         Update Task set Task_Name = ${name} Where Chat_id = ${chatID.value} and Ordering = ${numberOfTask.value}
         """.update

  def getByChat(chatID: ChatID): Query0[TodoTask] =
    sql"""Select * from Task where CHAT_ID = ${chatID.value}"""
      .query[TodoTask]

  def count(chatID: ChatID): Query0[Int] =
    sql"""Select Count(*) from Task where CHAT_ID = ${chatID.value}"""
      .query[Int]
}
