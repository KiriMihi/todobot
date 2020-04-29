import Config.DbConfig
import zio.Task
import org.flywaydb.core.Flyway
object FlyWayMigration {
  def migrate(config: DbConfig): Task[Unit] =
    Task {
      Flyway
        .configure(this.getClass.getClassLoader)
        .dataSource(config.url, config.user, config.password)
        .locations("migrations")
        .connectRetries(Int.MaxValue)
        .load()
        .migrate()
    }
}
