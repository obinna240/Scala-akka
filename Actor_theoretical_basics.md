Basics of  an Actor system
===========================

1. Actors are a container for State, Behaviour, Mailbox, Child Actors and Supervisor Strategy
2. All this functionality is encapsulated in an Actor reference
3. Actors must be explicitly terminated 
4. Actors are passed around using their reference

## Actor state
1. Actor objects will typically contain some variables which reflect possible states the actor may be in.
2. This can be an explicit state machine (e.g. using the FSM module), or it could be a counter, set of listeners, pending requests, etc.
3. An actor's state can be persisted so that it can be recovered from the persistent layer by replaying all the received Messages

## Behaviour
1. Behavior means a function which defines the actions to be taken in reaction to the message at that point in time, say forward a request if the client is authorized, deny it otherwise. 

## Mailbox
1. Each actor has exactly one Mailbox
2. Messages sent to the actor are enqueued in a Mailbox
3. Enqueuing happens in the time-order of send operations
   - Messages sent from different actors may not have a defined order at runtime due to the randomness of distributing actors across threads
   - Sending multiple messages from the same actor will enqueue the messages in the same order
4. Mailbox's could have different implementations
   - FIFO (typically the default)
   - For priority meesages, a priority mailbox will enqueue messages based on priority

## Child Actors
1. An actor can create a child actor
2. The actor thus becomes the supervisor of the child Actor
3. The children of the actor are maintained within the child actor's context
4. child actors are created using context.actorOf(...) and stopped by saying context.stop(child)

## Actor Supervision
1. Refers to dependency relationships between Actors
2. Supervisors delegate tasks to subordinates
3. Actions taken on supervisors will impact the subordinates, for example resuming an actor will resume all its subordinate actors

## Actor References, Paths and Addresses
1. ActorRef 
   - This is the reference to an Actor
   - Used to send messages to the Actor it represents
   - Each actor has access to its reference through the `self` field
   - The actor also has access to the a reference representing the sender of the message using `sender()`
   - An actor path consists of an anchor, which identifies the actor system, followed by the concatenation of the path elements, from root guardian to the designated actor; the path elements are the names of the traversed actors and are separated by slashes.
   - Actors may be looked up by using the `ActorSystem.actorSelection` method
     - In addition to `ActorSystem.actorSelection` there is also `ActorContext.actorSelection`, which is available inside any actor as `context.actorSelection`. This yields an actor selection much like its twin on ActorSystem, but instead of looking up the path starting from the root of the actor tree it starts out on the current actor. Path elements consisting of two dots `("..")` may be used to access the parent actor. 
     You can for example send a message to a specific sibling: `context.actorSelection("../*") ! msg`
     
## `actorOf` vs. `actorSelection`
   - `actorOf` only ever creates a new actor, and it creates it as a direct child of the context on which this method 
   is invoked (which may be any actor or actor system).
   - `actorSelection` only ever looks up existing actors when messages are delivered, i.e. does not create actors, or 
   verify existence of actors when the selection is created.
   
## Configuration of Actors
   - You can start using Akka without defining any configuration, since sensible default values are provided.
   - All configuration for Akka is held within instances of ActorSystem, or put differently, as viewed from the outside, ActorSystem is the only consumer of configuration information. 
   
## Actor API
Actors extend the `Actor trait` which provides
   - The `receive` method
   - The `self` reference to the ActorRef of the actor
   - The `sender` reference sender actor of the last received message, typically used to reply the sender
   - `supervisorStrategy` user overridable definition the strategy for supervising child actors
   
