import Config.TodoConfig

final case class Config(relaseConfig: TodoConfig)

object Config {
  final case class DbConfig(
      url: String,
      driver: String,
      user: String,
      password: String
  )
  final case class TodoConfig(dbConfig: DbConfig)
}
