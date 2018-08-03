# Building Actors
```
case object Pong
case object Ping

class MyActor extends Actor {
  val log = Logging(context.system, this)
  var countdown: Int = 100
  
  def receive = {
    case "test" => log.info("received test")
    case Pong => 
      println(s"${self.path} received pong and countdown is ${countdown}")
      if (countdown > 0) {
        countdown -= 1
        sender() ! Ping
      } else {
        sender() ! PoisonPill
        self ! PoisonPill
      }
    case _ => log.info("received unknonw message")
  }
}
```
## What is `Props` and why are they used
   - A configuration class used to specify actor creation
   - It is immutable
   - It contains associated deployment information
   - Typically the companion object of the class would have a props method which returns a props object
   - Also declare the messages an actor can receive in the companion object of the actor
   ```
   val props1 = Props[Actor]
   val props2 = Props(new ActorWithArgs("args"))
   val props3 = Props(classOf[ActorWithArgs], "args)
   ```
   - The second method `props2` is not recommended to be used within another actor
   - Actors are created by passing a Props instance into the actorOf factory method which is available on ActorSystem and ActorContext

### Recommended Approach to using `Props`, ActorSystems and ActorObjects
```
   import akka.actor.ActorSystem
   
   val system = ActorSystem("mySystem")
   
   object DemoActor {
      def props(magicNumber: Int): Props = Props(new DemoActor(magicNumber))
      case class Greeting(from: String)
      case objecct Goodbye
      }
      
    class DemoActor(magicNumber: Int) extends Actor {
      def receive = {
        case x: Int => sender() ! (x + magicNumber)
        case Greeting(greeter) => log.info(s"Hello i am greeted by $greeter")
        case Goodbye => println("This is goodbye")
      }
    }
    
    //observe that MyActor has not got a Props method
    object MyActor {
      case class Greeting(from: String)
      case object Goodbye
    }
    class MyActor extends Actor with ActorLogging {
      import MyActor._
      def receive = {
        case Greeting(greeter) ⇒ log.info(s"I was greeted by $greeter.")
        case Goodbye           ⇒ log.info("Someone said goodbye to me.")
      }
    }
    
    class SomeOtherActor extends Actor {
      context.actorOf(DemoActor.props(42), "demo")
    }
    
    //In the absence of a Props methid, we use Props[MyActor]
    val myActor = system.actorOf(Props[MyActor], "myActor2")
    
 ```

## 

