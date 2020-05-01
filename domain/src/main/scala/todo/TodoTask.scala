package todo

import java.util.UUID

import todo.Repository.Name
import Repository.Name
import TodoTask.TaskID

final case class TodoTask(id: TaskID, chatID: ChatID, taskName: Name)

object TodoTask {
  final case class TaskID(value: String)

  def make(chatID: ChatID, name: Name): TodoTask =
    TodoTask(
      TaskID(UUID.randomUUID().toString),
      chatID,
      name
    )
}
