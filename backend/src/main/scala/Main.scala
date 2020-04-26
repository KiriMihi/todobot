import Config.DbConfig
import canoe.api.{TelegramClient => CanoeClient}
import cats.effect.{Blocker, Resource}
import cats.syntax.either._
import chat.ChatStorage
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import log.Logger
import org.http4s.blaze.http.HttpClient
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import pureconfig._
import pureconfig.generic.auto._
import telegram.{CanoeScenarios, TelegramClient}
import todo.TodoError.{ConfigurationError, MissingBotToken}
import todo.TodoLogic
import zio._
import zio.blocking.Blocking
import zio.console.putStrLn
import zio.interop.catz._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits

object Main extends zio.App {
  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val program = for {
      token <- telegramBotToken orElse UIO.succeed("")
      config <- readConfig
      //http4sClient <- makeHttpClient
      canoeClient <- makeCanoeClient(token)
      transactor <- makeTransactor(config.relaseConfig.dbConfig)

    } yield ()

    program.foldM(
      err => putStrLn(s"Execution failed with: ${err}") *> ZIO.succeed(1),
      _ => ZIO.succeed(0)
    )
  }

  private def makeProgram(
      http4sClient: TaskManaged[Client[Task]],
      canoeClient: TaskManaged[CanoeClient[Task]],
      transactor: RManaged[Blocking, Transactor[Task]]
  ): RIO[ZEnv, Int] = {
    val loggerLayer = Logger.console
    val transactorLayer = transactor.toLayer.orDie
    val chatStorageLayer = transactorLayer >>> ChatStorage.doobie

    val storageLayer = chatStorageLayer
    val todoLogicLayer = (loggerLayer ++ storageLayer) >>> TodoLogic.live
    //val http4sClientLayer = http4sClient.toLayer.orDie
    //val httpClientLayer = http4sClientLayer >>> HttpClient.http
    val canoeClientLayer = canoeClient.toLayer.orDie
    val canoeScenarioLayer =
      (canoeClientLayer ++ todoLogicLayer) >>> CanoeScenarios.live
    val telegramClientlayer =
      (loggerLayer ++ canoeClientLayer ++ canoeScenarioLayer) >>> TelegramClient.canoe
    val startTelegramClientLayer = TelegramClient.start
    val programLayer = telegramClientlayer
    val program = startTelegramClientLayer.fork
    program.provideSomeLayer[ZEnv](programLayer)
  }

  private def readConfig =
    ZIO.fromEither {
      ConfigSource.default
        .load[Config]
        .leftMap(errors => ConfigurationError(errors.prettyPrint()))
    }

  private def makeHttpClient: UIO[TaskManaged[Client[Task]]] =
    ZIO.runtime[Any].map { implicit rts =>
      BlazeClientBuilder.apply(Implicits.global).resource.toManaged
    }

  private def makeTransactor(
      config: DbConfig
  ): RIO[Blocking, RManaged[Blocking, HikariTransactor[Task]]] = {
    def transactor(
        connectEC: ExecutionContext,
        transactEC: ExecutionContext
    ): Resource[Task, HikariTransactor[Task]] =
      HikariTransactor.newHikariTransactor[Task](
        config.driver,
        config.url,
        config.user,
        config.password,
        connectEC,
        Blocker.liftExecutionContext(transactEC)
      )

    ZIO.runtime[Blocking].map { implicit rt =>
      for {
        transactEC <-
          ZIO.access[Blocking](_.get.blockingExecutor.asEC).toManaged_
        transactor <-
          transactor(rt.platform.executor.asEC, transactEC).toManaged
      } yield transactor
    }
  }

  private def makeCanoeClient(
      token: String
  ): UIO[TaskManaged[CanoeClient[Task]]] =
    ZIO.runtime[Any].map { implicit rts => CanoeClient.global(token).toManaged }

  private def telegramBotToken: RIO[system.System, String] =
    for {
      token <- system.env("")
      token <- ZIO.fromOption(token).mapError(_ => MissingBotToken)
    } yield token

}
