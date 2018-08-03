import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }


/**
 * Building Actors and their Messages
 * 1. Actors receive messages
 * 2. Messages in a sense are the public facing APIs of Actors
 * 3. Messages should have good semantic names to make them easy to understand
 * 4. To build Messages
 *      i. Messages should be immutable
 *      ii. Messages should be declared in the Actor's companion object
 *      iii. Use a Props method that describes how to create an Actor (Props is immutable making it threadsafe)
 * In the example below, our Greeter actor accepts two parameters
 * 1. A reference to another Actor, printerRef and a message String
 * 2. It implements the receive method which is responsible for receiving messages
 * 3. Receive method accepts messages described in the companion object
 * */


 object Greeter{
   //define the Props method used by the Actor
   def props(message: String, printerActor: ActorRef): Props = Props(new Greeter(message, printerActor))
   //describe the messages accepted by the Actor
   final case class WhoToGreet(who: String)
   case object Greet
 }

 class Greeter(message: String, printerActor: ActorRef) extends Actor with ActorLogging {
   import Greeter._
   import Printer._

   var greeting = ""

   //we can ommit the Receive return type here
   def receive: Receive = {
      case WhoToGreet(who) =>
        greeting = message + " " +who
      case Greet           =>
        printerActor ! Greeting(greeting)
   }
 }

//Printer Actor's companion object
object Printer {
  def props: Props = Props[Printer]
  final case class Greeting(greeting: String)
}

class Printer extends Actor with ActorLogging {
  import Printer._

  def receive = {
    case Greeting(greeting) =>
      log.info("Greeting received (from "+ sender() +"): "+ greeting)
  }
}

// Create the 'helloAkka' actor system
val system: ActorSystem = ActorSystem("helloAkka")

//Actors can now be created using the ActorSystem
val printer: ActorRef = system.actorOf(Printer.props, "PrinterActor")

// Create the 'greeter' actors
val howdyGreeter: ActorRef =
  system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")
val helloGreeter: ActorRef =
  system.actorOf(Greeter.props("Hello", printer), "helloGreeter")
val goodDayGreeter: ActorRef =
  system.actorOf(Greeter.props("Good day", printer), "goodDayGreeter")


/**
 * Following this example, we can now go deeper into the Actor System
 * 1. Actors always belong to a parent
 * 2. A parent actor can create a child actor by calling context.actorOf()
 * 3. All actors have a common parent, the user guardian and it is from this that new actor instances
 * are created using system.actorOf()
 * 4. Actors created using system.actorOf() are called top-level actors
 * See the example below
 * **/

 class ANewActor extends Actor {
   override def receive: Receive = {
     case "printIt" =>
      val secondRef = context.actorOf(Props.empty, "second-actor")
      println(s"Second: $secondRef")
   }
 }

 object ANewActor {
   def props: Props = Props[ANewActor]
 }

 /**
  * In this section, we will consider Actor message patterns
  * Consider the following class
  * Observe that we have a pattern that takes a request and responds with a temperature
  * */
  object Device {
    def props(groupId: String, deviceId: String):Props = Props(new Device(groupId, deviceId))
    final case class ReadTemperature(requestId: Long)
    final case class Respondtemperature(requestId: Long, value: Option[Double])
  }

  class Device (groupId: String, deviceId: String) extends Actor with ActorLogging {
    import Device._
    var lastTemperatureReading: Option[Double] = None

    override def preStart(): Unit = log.info("Device actor {}-{} started", groupId, deviceId)
    override def postStop(): Unit = log.info("Device actor {}-{} stopped", groupId, deviceId)

    override def receive: Receive = {
      case ReadTemperature(id) =>
        sender() ! RespondTemperature(id, lastTemperatureReading)
    }
  }

  /**
   * Testing the Actor
   * We are using the Akka TestKit and ScalaTest
   * ====== How is the test written in scala test ======
   * Objective
   * ---------
   * To test the Device Actor
   * What we really want to test is functionality
   * 1. Describe the scala test i.e "reply with empty reading if no temperature is known" in
   * 2. Describe the implementation
   *    i. Create a test probe i.e val probe = TestProbe()
   *    ii. mock the deviceActor/create a new Device Actor and also create dummy parameters
   *    iii. We want to test that the actor receives or does ReadTemperature
   *          a. So send a message i.e. using Tell, and using the Probe as a reference
   *          b. We basically listen on the probe i.e. deviceActor.tell(Device.ReadTemperature(requestId = 42), probe.ref)
   *          c. The outcome of our tell is to send and expect a response using RespondTemperature
   *          d. we then assert on the response
   * */
  "reply with empty reading if no temperature is known" in {
    val probe = TestProbe() //create the TestProbe
    val deviceActor = system.actorOf(Device.props("group", "device")) //we create our device actor
    //deviceActor ! (Device.ReadTemperature(requestId = 42), probe.ref)
    deviceActor.tell(Device.ReadTemperature(requestId = 42), probe.ref)
    val response = probe.expectMsgType[Device.RespondTemperature]
    response.requestId should ===(42)
    response.value should ===(None)
  }
