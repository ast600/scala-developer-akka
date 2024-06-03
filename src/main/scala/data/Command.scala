package data

sealed trait Command

object Command {
  case class Add(num: Int) extends Command

  case class Multiply(by: Int) extends Command

  case class Divide(by: Int) extends Command

  case class Subtract(num: Int) extends Command

  case object CompleteCalculations extends Command
}
