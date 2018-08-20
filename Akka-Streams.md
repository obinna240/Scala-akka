## Note on Streams
```
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl._
import scala.concurrent.Future

object SimpleStreamExample {
  def main(args: Array[String]): Unit = {
    //create the actor system
    implicit val system = ActorSystem("Sys")
    //create the actor materializer, which is used to run the stream
    implicit val materializer = ActorMaterializer()
    
    val numbers = 1 to 1000
    
    //let's create a source that will iterate over the number sequence
    val numberSource:Source[Int, NotUsed] = Source.fromIterator(() => numbers.iterator)
    
    //build the flow: Only let pass even numbers through the flow
    val isEvenFlow: Flow[Int, Int, NotUsed] = Flow[Int].filter((num) => num % 2 == 0)
    
    //create a source of even random numbers by combining the random number source 
    //with the even number filter flow
    val evenNumberSource: Source[Int, NotUsed] = numberSource.via(isEvenFlow)
    
    //Create a sink that will write its input to console
    val consoleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
    
    //connect source with sink and run using materializer
    evenNumberSource.runWith(consoleSink)
  }
}
```
