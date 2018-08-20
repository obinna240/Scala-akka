## Note on Streams
### Quickstart
- Let's start with a simple source
```
val source: Source[Int, NotUsed] = Source(1 to 100)
```

### Some Code
See - https://blog.scalac.io/2017/04/18/akka-streams-introduction.html#introduction-to-the-concept-of-streams
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
    /**
     * create the actor materializer, which is used to run the stream
     * Materializer is needed to materialize the flow into a Processor
     * which represents a processing stage.
     * */
    implicit val materializer = ActorMaterializer()
    
    val numbers = 1 to 1000
    
    /**
     * Source consists of only one output
     * it takes two types of parameters.
     * First the type of data it emits: In this example an Int
     * Second, is the type of auxiliary value it can produce when materialized or ran
     * If we don't produce any type we simply used NotUsed. 
     * Nowlet's create a source that will iterate over the number sequence
     * */
    val numberSource:Source[Int, NotUsed] = Source.fromIterator(() => numbers.iterator)
    
    /**
     * The flow represents a Stream (a set of processing steps) with one open input
     * and one open output. It is an ordered chain of transformations to its input
     * It takes three types of parameters, the input datatype, outputand last the auxiliary type
     * 
     * Let's now build the flow: Only let pass even numbers through the flow
     * We can create as many flows as we want
     * Or chain two flows together
     **/
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
