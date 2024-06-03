package data

sealed trait Event

object Event {
  case class Added(id: Int, num: Int) extends Event

  case class Multiplied(id: Int, by: Int) extends Event

  case class Divided(id: Int, by: Int) extends Event

  case class Subtracted(id: Int, num: Int) extends Event

  case object CalculationCompleted extends Event
}