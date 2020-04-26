package telegram

import canoe.api.Scenario
import zio.{Has, Task, URLayer, ZLayer}
import canoe.api.{TelegramClient => Client, _}
import chat.ChatStorage
import chat.ChatStorage.ChatStorage
import log.Logger
import todo.TodoLogic
import todo.TodoLogic.TodoLogic
import telegram.Live

object CanoeScenarios {
  type CanoeScenarious = Has[Service]

  trait Service {
    def start: Scenario[Task, Unit]
    def help: Scenario[Task, Unit]
    def add: Scenario[Task, Unit]
    def del: Scenario[Task, Unit]
    def list: Scenario[Task, Unit]
  }

  type LiveDeps = Has[Client[Task]] with TodoLogic
  def live: URLayer[LiveDeps, Has[Service]] =
    ZLayer.fromServices[Client[Task], TodoLogic.Service, Service] {
      (client, todoLogic) => Live(todoLogic, client)
    }

}
