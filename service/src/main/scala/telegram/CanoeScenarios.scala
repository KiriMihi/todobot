package telegram

import canoe.api.Scenario
import zio.{Has, Task, ULayer, URLayer, ZLayer}
import canoe.api.{TelegramClient => Client, _}
import todo.TodoLogic
import todo.TodoLogic.TodoLogic

object CanoeScenarios {
  type CanoeScenarious = Has[Service]

  trait Service {
    def start: Scenario[Task, Unit]
    def help: Scenario[Task, Unit]
    def add: Scenario[Task, Unit]
    def del: Scenario[Task, Unit]
    def list: Scenario[Task, Unit]
    def update: Scenario[Task, Unit]
    def alltasks: Scenario[Task, Unit]
  }

  type LiveDeps = Has[Client[Task]] with TodoLogic
  def live: URLayer[LiveDeps, Has[Service]] =
    ZLayer.fromServices[Client[Task], TodoLogic.Service, Service] {
      (client, todoLogic) => Live(todoLogic, client)
    }
}
