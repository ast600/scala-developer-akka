package data

final case class State(value: Int) {
  def add(num: Int): State = copy(value = value + num)

  def multiply(by: Int): State = copy(value = value * by)

  def divide(by: Int): State = copy(value = value / by)

  def subtract(num: Int): State = copy(value = value - num)
}

object State {
  val zero: State = State(0)
}
