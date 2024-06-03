package read

import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{ EventEnvelope, PersistenceQuery }
import akka.stream.ClosedShape
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.{ Flow, GraphDSL, RunnableGraph, Source }
import data.Event
import data.Event.{ Added, CalculationCompleted, Divided, Multiplied, Subtracted }

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class ReadSide(implicit system: ActorSystem[NotUsed], session: SlickSession) {

  import session.profile.api._

  def apply(): Behavior[NotUsed] = Behaviors.setup { _ =>
    RunnableGraph.fromGraph(graph).run()
    Behaviors.same
  }

  private def getLatestOffsetAndResult: (Int, Double) = Await.result(session.db.run {
    sql"SELECT write_side_offset, calculated_value FROM public.result WHERE id = 1".as[(Int, Double)]
  }, 3.seconds).head

  private var (offset, latestCalculatedResult) = getLatestOffsetAndResult
  private val startOffset: Int = if (offset == 1) 1 else offset + 1

  private val source: Source[EventEnvelope, NotUsed] =
    PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)
                            .eventsByPersistenceId("001", startOffset, Long.MaxValue)

  private val flow = Flow[EventEnvelope].map { envelope =>
    envelope.event.asInstanceOf[Event] match {
      case Added(_, num) =>
        latestCalculatedResult += num
        CalculatorSnapshot(envelope.sequenceNr, latestCalculatedResult)
      case Multiplied(_, by) =>
        latestCalculatedResult *= by
        CalculatorSnapshot(envelope.sequenceNr, latestCalculatedResult)
      case Divided(_, by) =>
        latestCalculatedResult /= by
        CalculatorSnapshot(envelope.sequenceNr, latestCalculatedResult)
      case Subtracted(_, num) =>
        latestCalculatedResult -= num
        CalculatorSnapshot(envelope.sequenceNr, latestCalculatedResult)
      case CalculationCompleted => throw new RuntimeException("Got poison pill")
    }
  }

  private val sink = Slick.sink { (snapshot: CalculatorSnapshot) =>
    sqlu"UPDATE public.result SET calculated_value = ${
      snapshot.value
    }, write_side_offset = ${ snapshot.sequenceNum } WHERE id = 1"
  }

  private val graph = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    source ~> flow ~> sink.async
    ClosedShape
  }
}
