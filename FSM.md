# Finite State Machines

## Overview
If we are in a state S and the event E occurs, we should perform the actions A and make a transition to state S'
To construct the FSM, we must describe:
   - the messages received or produced by the Actor (Typically case objects and case classes).
   - the states that the actor could be in. These states will typically extend a `sealed trait State`
   - Finally, the internal state data of the actor which is made up of the target actor reference and other messages. This basically represents the data held or moved around by the FSM actor.

## Basics
   - An actor can be in a state `become` or `unbecome` 
   - However, if it can exist in more than two states, then it is recommended that we use FSMs
   - FSM is a trait
   - With any FSM two things are key: The state of the machine and the data, as such for our FSM, we declare it by indicating its state and data i.e. ```class FSM1 extends FSM[FSMState, FSMData]```. This also means that all our possible states extends `FSMState` and all of our data extends `FSMData`.
   - We can now declare all our states and data in a companion object
    ```
    object FSM1 {
      sealed trait FSMState
      case object State1 extends FSMState
      case object State2 extends FSMState
      case object State3 extends FSMState
      
      case class FSMData(age: Int, status: String)
    }
    ```

## Implementation
   - There are four parts to a FSM
     - The actor needs to be in a state
     - The actor receives an event
     - The actor performs an action
     - On performing the action, the actor goes into a new state
   - An actor must be in one of the defined states at all times
   - States must be in the typed system
   - The FSM trait takes two type parameters:
     - the supertype of all state names, usually a sealed trait with case objects extending it
     - the type of the state data which are tracked by the FSM module itself.
     
     ```
     //received events (The events the FSM listens for)
     final case class SetTarget(ref: ActorRef)
     final case class Queue(obj: Any)
     case object Flush
     
     //sent events
     final case class Batch(obj: immutable.Seq[Any])
     
     //SetTarget is needed for starting it up, setting the destination for the Batches to be passed on; Queue will add to the internal queue while Flush will mark the end of a burst.
     ```
   - Additionally, actors need to be in a state
     - Actors can be `Idle` i.e no message queued
     - Actors can be `Active` i.e some queued message
     - Actors will stay in the `Active` state as long as messages keep arriving and no flush is requested
 
 ## FSM Example
 ```
import akka.pattern._
import akka.util._
import akka.actor.FSM
import akka.actor.ActorRef
import scala.concurrent.duration._

//declare the actor, mixing the FSM trait and 
//specifying the possible states and data values as type parameters.
//A DSL is used within the body for declaring the state machine
class Buncher extends FSM[State, Data] {
  
  //defines the initial state and initial data
  startWith(Idle, Uninitialized)
  
  //actors can be in two states active and idle
  //here we handle the idle state
  when(Idle) {
    case Event(setTarget(ref), Uninitialized) => 
      //stay prepares to end this event's processing for not leaving the current state
      //using modifier makes the FSM replace
      stay using Todo(ref, Vector.empty)
  }
  
  onTransition {
    case Active -> Idle =>
      stateData match {
        case Todo(ref, queue) => ref ! Batch(queue)
        case _                => //nothing to do
      }
  }
  
  when(Active, stateTimeout =  1 second) {
    case Event(Flush | StateTimeout, t:Todo) => 
      goto(Idle) using t.copy(queue = Vector.empty)
  }
  
  //when in a particular state and an event is unhandled then
  whenUnhandled {
    //common code for both states
    case Event(Queue(obj), t @ Todo(_, v)) => 
      goto(Active) using t.copy(queue = v :+ obj)
    case Event(e,s) => 
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }
  
  //we start it up using initialize
  initialize()
}
 ```

## FSM Creation Steps
- We define the states - a state as a case object which extends a common trait
   
   ```
   trait ObjectState
   case object On extends ObjectState
   case object Off extends ObjectState
   case object Sleeping extends ObjectState
   case object Hibernating extends ObjectState
   
   ```
- Define a data object to each state (In other words in a particular state, there has to be an object that can be referred to it)
  - The data object could be a case class or a case object
  - Typically, we define a common trait and have case objects or classes which extend it
  - We could have different types for each state
  - For example, in a game, we could define our data type as a `Map[Int, Option[Player]]`. The keys of this map will be numbers representing the squares (numbered 0-8, left to right), while the values indicate which player is occupying the square. The value will be None if there is no player in the square, or Some(X) or Some(O)f when one of the players has occupied the square.
- Once types are defined we can mix in FSM to the actor.
- The FSM must be parameterized with the types we'll be using for state and data.
```
trait Player
class Game extends Actor with FSM[ObjectState, Map[Int, Option[Player]]] {
   startWith(PlayerXTurn, Map.empty ++ (0 to 8 map(i => i -> None)))
}
```
- We now need to define what state the FSM should start at and the data at that state. This is defined with a call to the `startWith` method in the actor body

```
import akka.actor.{ ActorRef, FSM }
import scala.concurrent.duration._
import scala.collection._

/**
 * The FSM trait takes two type parameters:

the supertype of all state names, usually a sealed trait with case objects extending it
the type of the state data which are tracked by the FSM module itself.
**/

//messages
//received events
final case class SetTarget(ref: ActorRef)
final case class Queue(obj: Any)
case object Flush

//sent events
final case class Batch(obj: immutable.Seq[Any])

//state
sealed trait State
case object Idle extends State
case object Active extends State
case object Asleep extends State

//data
sealed trait Data
case object Uninitialized extends Data
final case class Todo(target: ActorRef, queue: immutable.Seq[Any]) extends Data

//declare the actor
//we can attach some data to each state
//Note that we could have different data types appended to states
//By using the sealed trait Data, we can have different types of Data types
class Buncher extends FSM[State, Data]{
  
  //startWith defines the initial state and initial data
  //Each FSM needs a starting point declared using
  //startWith(state, data[, timeout])
  startWith(Idle, Uninitialized)
  
  //the result of any state function must be a definition of the next state unless
  //terminating the FSM
  when(Idle) {
    case Event(SetTarget(ref), Uninitialized) =>
      stay using Todo(ref, Vector.empty) //stay in this state, replace the 
      //unuinitialized object with a Todo object
  }
  
  when(Asleep) {
    //This takes the form
    /**
     * 
    case Event(msg, data) => {
      
    }
    see below for an example
    **/
    // case Event(SetTarget(ref), Uninitialized) => {
    //   stay using Todo(ref, Vector.empty)
    // }
    FSM.NullFunction
  }
  
  
  
  
   onTransition {
    case Active -> Idle =>
      stateData match {
        case Todo(ref, queue) => ref ! Batch(queue)
        case _                => // nothing to do
      }
  }
  
  //Active state has a state timeout declared which means that if no message
  //is received for 1 second, a FSM.StateTimeout message will be generated
  when(Active, stateTimeout = 1 second) {
    case Event(Flush | StateTimeout, t: Todo) =>
      goto(Idle) using t.copy(queue = Vector.empty)
  }
  
  whenUnhandled {
    // common code for both states
    case Event(Queue(obj), t @ Todo(_, v)) =>
      goto(Active) using t.copy(queue = v :+ obj)

    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }
  
  //This starts up the FSM and performs the transition into the initial state and sets up timers
  initialize() 
 }
 
//implicit classes
implicit class IntTimes(x: Int) {
  def times [A](f: => A): Unit = {
         def loop(current: Int): Unit =
         
         if(current > 0){
            f
            loop(current - 1)
         }
         loop(x)
      }
}
```
