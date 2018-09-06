## Testing Actors using the AKKA TestKit

In this section, we want to test the following, that:
- If we send something to an actor, we would like to know if it received the message
- After sending something to an actor, it in turn does some processing and on success sends something to another actor and we would like to know if the other actor receives the message
- We would like to know if a particular message is received by an actor amongst a number of messages
- The actor does not send across particular messages to others
- What an actor sends back as a response based on certain messages

To accomplish this, we create 4 actors as seen below

```
import akka.actor.ReceiveTimeout
import scala.concurrent.duration._
import akka.actor.{ ActorSystem, Actor, ActorRef, Props, PoisonPill, Terminated, Identify }
import language.postfixOps
import scala.concurrent.duration._
import akka.event._
import akka.actor.Timers
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{WordSpec, BeforeAndAfterAll}
import akka.actor.Actor._
import akka.util.duration._
import akka.util.TestKit
import java.util.concurrent.TimeUnit
import util.Random

/**
 * Echo Actor is an actor that just echoes messages
 * */
 class EchoActor extends Actor {
   override def receive = {
     case msg => {
       //self ! msg
       println(msg)
     }
   }
 }
 
 
 /**
  * Forwarding Actor is an actor that forwards a message to another actor
  * */
  class ForwardingActor(next: ActorRef) extends Actor {
    def receive = {
      case msg => {
        next ! msg
      }
    }
  }
  
  /**
   * Filtering Actor is an actor that forwards only certain messages
   * 
   * */
   class FilteringActor(next: ActorRef) extends Actor {
     def receive = {
       case msg: String => {
         next ! msg
       }
       case _ => None
     }
   }
   
   /**
    * Sequencing Actor is an actor that forwards some random amount of uninteresting messages
    * an interesting message and some random amount of uninteresting messages
    * 
    * */
    class SequencingActor(next: ActorRef, head: List[String], tail: List[String]) extends Actor {
      def receive = {
        case msg => {
          head map (next ! _)
          next ! msg
          tail map (next ! _)
        }
      }
    }
    
```
