package write

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior, RetentionCriteria }
import data.Command.{ Add, CompleteCalculations, Divide, Multiply, Subtract }
import data.Event.{ Added, Divided, Multiplied, Subtracted }
import data.{ Command, Event, State }

class WriteSide(persistenceIdentifier: PersistenceId) {

  def apply(): Behavior[Command] = Behaviors.setup { ctx =>
    EventSourcedBehavior[Command, Event, State](
      persistenceId = persistenceIdentifier,
      State.zero,
      (state, command) => handleCommand("001", state, command, ctx),
      (state, event) => handleEvent(state, event, ctx)
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2)
                                       .withDeleteEventsOnSnapshot)
  }

  private def handleCommand(
                             persistenceId: String,
                             state: State,
                             command: Command,
                             ctx: ActorContext[Command]
                           ): Effect[Event, State] =
    command match {
      case Add(amount) =>
        ctx.log.info(s"Received addition command: adding $amount to ${ state.value }")
        Effect.persist(Added(persistenceId.toInt, amount))
      case Multiply(factor) =>
        ctx.log.info(s"Received multiplication command:  multiplying ${ state.value } by $factor")
        Effect.persist(Multiplied(persistenceId.toInt, factor))
      case Divide(divisor) =>
        if (divisor == 0) {
          ctx.log.error("Attempting division by zero, ignoring command")
          Effect.none
        } else {
          ctx.log.info(s"Received division command: dividing ${ state.value } by $divisor")
          Effect.persist(Divided(persistenceId.toInt, divisor))
        }
      case Subtract(num) =>
        ctx.log.info(s"Received subtraction command: subtracting $num from ${ state.value }")
        Effect.persist(Subtracted(persistenceId.toInt, num))
      case CompleteCalculations =>
        ctx.log.info(s"Received poison pill, stopping")
        Effect.stop
    }

  private def handleEvent(state: State, event: Event, ctx: ActorContext[Command]): State =
    event match {
      case Added(_, amount) =>
        ctx.log.info(s"Handing event amount is $amount and state is ${ state.value }")
        state.add(amount)
      case Multiplied(_, amount) =>
        ctx.log.info(s"Handing event amount is $amount and state is ${ state.value }")
        state.multiply(amount)
      case Divided(_, amount) =>
        ctx.log.info(s"Handing event amount is $amount and state is ${ state.value }")
        state.divide(amount)
    }
}