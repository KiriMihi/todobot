package todo
sealed trait TodoError extends Throwable {
  def message: String
  override def getMessage: String = message
}

object TodoError {
  final case class ConfigurationError(text: String) extends TodoError {
    override def message: String = text
  }
  final case object MissingBotToken extends TodoError {
    def message: String = "Bot token is missed"
  }

  final case class NotFound(url: String) extends TodoError {
    override def message: String = s"$url not found"
  }

  final case class UnexpectedError(text: String) extends TodoError {
    def message: String = text
  }
}
