# Finite State Machines

## Overview
If we are in a state S and the event E occurs, we should perform the actions A and make a transition to state S'

## Basics
   - FSM is a trait
   - There are four parts to a FSM
     - The actor needs to be in a state
     - The actor receives an event
     - The actor performs an action
     - On performing the action, the actor goes into a new state
   - An actor must be in one of the defined states at all times
   - States must be in the typed system
   - We define a state as a case object which extends a common trait
   ```
   trait ObjectState
   case object On extends ObjectState
   case object Off extends ObjectState
   case object Sleeping extends ObjectState
   case object Hibernating extends ObjectState
   
   ```
   - We also need to be able to attach a data object to each state.
     - Typically, 
     
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
