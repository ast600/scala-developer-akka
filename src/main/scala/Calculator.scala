import akka.NotUsed
import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.stream.alpakka.slick.javadsl.SlickSession
import data.Command
import read.ReadSide
import write.WriteSide

import scala.language.postfixOps


object Calculator {

  private def launchWriteSideAndFeedMessages: Behavior[NotUsed] = Behaviors.setup { ctx =>
    val writeActorRef = ctx.spawn(new WriteSide(PersistenceId.ofUniqueId("001")).apply(), "write_side", Props.empty)
    writeActorRef ! Command.Add(10)
    writeActorRef ! Command.Divide(2)
    writeActorRef ! Command.Multiply(3)
    Behaviors.same
  }

  def main(args: Array[String]): Unit = {
    implicit val slick: SlickSession = SlickSession.forConfig("slick-postgres")
    implicit val system: ActorSystem[NotUsed] = ActorSystem(launchWriteSideAndFeedMessages, "calculator")

    new ReadSide().apply()
  }


}
