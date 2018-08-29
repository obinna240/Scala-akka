```
import akka.actor.{ ActorSystem, Actor, ActorRef, Props, PoisonPill, Terminated, Identify }
import language.postfixOps
import scala.concurrent.duration._
import akka.event._

/**
 * Our actor takes a number and builds a sequence of integers from it
 * */
class Builder(number: Int) extends Actor {
  import Builder._
  override def receive = {
    case MultiplyNumber => {
      
    }
    case ConvertNumberToString => {
      
    }
  }
}

/**
 * This actor takes a string and does some stuff with it
 * */
class StringBuilder extends Actor {
  import StringBuilder._
  override def receive = {
    case RebuildString => {
      
    }
    case AppendString => {
      
    }
  }
}

//we also need a companion object for this class responsible for 
//defining andd building the actor
object Builder {
  
  def props(number: Int): Props = {
    Props(new Builder(number))
  }
  
  //we can also include the messages that this actor can receive
  case object MultiplyNumber
  case class ConvertNumberToString(string: String)
}

object StringBuilder {
  
  def props: Props = Props(new StringBuilder())
  
  //we can also include the messages that the actor can receive
  case class RebuildString(string: String)
  case class AppendString(string: String)
}


//we now want to use the actors
//we can create an actor by passing a Props instance into the ACTORof factory method
//available on ActorSystem and ActorContext
class MainActor(int: Int, str: String) extends Actor {
  val strBuilderRef: ActorRef = context.actorOf(StringBuilder.props, name = "stringBuilder")
  val builderRef: ActorRef = context.actorOf(Builder.props(int), name="builder")
  
  //we watch the actors here
  context.watch(strBuilderRef)
  context.watch(builderRef)
  
  //This actor can also register itself 
  context.watch(self) 
  //or can we do below
  val child = context.actorOf(Props.empty, "child")
  //now this actor wants to be notified when a child actor 
  //system has died or terminates and this service is provided by the DeathWatch
  //component of the actor system
  //The logic here is that, we want to watch these child actors as listed above
  
  //then we access the deadletter queue of the actor's context
  var lastSender = context.system.deadLetters
  
  override def receive = {
    case _ => println("This is the first actor")
    case "kill" => context.stop(self); lastSender = sender()
    case Terminated(`child`) => lastSender ! "finished"
    case ActorIdentity(`identityId`, Some(ref)) => 
      context.watch(ref)
      context.become(active, ref)
    case ActorIdentity(`identityId`, None => context.stop(self))
  }
  
  //Actor Selection
  //An actor can look up another actor using actor selection
  //will look up this absolute path
  context.actorSelection("/user/serviceA/aggregator")
  
  //will look up sibling beneath same supervisor
  context.actorSelection("../joe") ! Identify(identifyId)
}

/**
 * 
 * sending messages
 * */
 case class User (name: String)
 case class Register(user: user)
 val user = User("mike")
 val message = Register(user)
 
 







```
