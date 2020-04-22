sealed trait PageError extends Throwable {
  def message: String
  override def getMessage: String = message
}

object PageError {
  final case class ConfigurationError(text: String) extends PageError {
    override def message: String = text
  }
  final case object MissingBotToken extends PageError {
    def message: String = "Bot token is missed"
  }

  final case class NotFound(url: String) extends PageError {
    override def message: String = s"$url not found"
  }

  final case class UnexpectedError(text: String) extends PageError {
    def message: String = text
  }
}
