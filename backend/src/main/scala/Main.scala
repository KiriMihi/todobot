import Config.DbConfig
import PageError.{ConfigurationError, MissingBotToken}
import canoe.api.{TelegramClient => CanoeClient}
import cats.effect.{Blocker, Resource}
import doobie.hikari.HikariTransactor
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
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
      //config <-
      http4sClient <- makeHttpClient
      canoeClient <- makeCanoeClient(token)
      // transactor <- makeTransactor(config )

    } yield ()

    program.foldM(
      err => putStrLn(s"Execution failed with: ${err}") *> ZIO.succeed(1),
      _ => ZIO.succeed(0)
    )
  }
//
//    private def readConfig: IO[ConfigurationError, Config] =
//      ZIO.fromEither(ConfigSource.default)default

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
        transactEC <- ZIO.access[Blocking](_.get.blockingExecutor.asEC).toManaged_
        transactor <- transactor(rt.platform.executor.asEC, transactEC).toManaged
      } yield transactor
    }
  }

  private def makeCanoeClient(token: String): UIO[TaskManaged[CanoeClient[Task]]] =
    ZIO.runtime[Any].map { implicit rts => CanoeClient.global(token).toManaged }

  private def telegramBotToken: RIO[system.System, String] =
    for {
      token <- system.env("")
      token <- ZIO.fromOption(token).mapError(_ => MissingBotToken)
    } yield token

}
