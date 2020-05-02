package todo

import java.util.UUID

import todo.Repository.Name
import Repository.Name
import TodoTask.TaskID

final case class TodoTask(
    id: TaskID,
    chatID: ChatID,
    taskName: Name,
    ordering: NumberOfTask
)

object TodoTask {
  final case class TaskID(value: Long)
}
