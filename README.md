# picoservice
An execution model for some functional language constructs based on message passing with actors.

Harks back to Carl Hewitt's papers (e.g. [this one](http://worrydream.com/refs/Hewitt-ActorModel.pdf)) but unlike smalltalk puts functions center stage rather than objects. A remote goal is to create
something remotely similar to e.g. Spring integration, just with bearable syntax.

## pico?

Now that there are services, micro- and even nanoservices there wasn't much of a choice.

As of the promotors of [such stuff](https://www.serverlessops.io/blog/rise-of-the-nanoservice) a nanoservice is

>- Deployable
>- Reusable
>- Useful

From that characteristics for a picoservice can be derived.

A picoservice is:

>- Deployable
>- Reusable (sometimes)
>- Useless (except in comination with others)

## the idea 

Computations are nested recursive functions. Functions can be a primitive, this might be on the level of `+`, `-`, `>` or `<=`, but it could be as well a call of a connector to an external data provider, expensive both in terms of time and of money charged.
   
A condition is just another somewhat special primitive function. Each function has a dedicated function to return its result to. 

Every function receives named values as messages and propagates them to subsequent functions if needed. Each function is an actor, so all off them operate concurrently.  

Further, if it is a primitive and got all values to compute a result it does so and sends it to the designated recipient. 

If any function f receives a result value, it passes it on the the designated recipient of f's result.

`if` deserves some words: in one version it does just the same as every primitive function: it waits for the necessary parameters (the calculated value of the condition `Vc`, true or false, and the value of both branches, and returns one of them depending on`Vc`). This doesn't limit the evaluation to one of the branches, which is an issue if you want to use it to terminate recursion. So `if` also comes in a second flavor, that only propagates values to a branch, if `Vc` indicates so. 
 So it has kind of this signature: `if(condition, value_true_branch, value_false_branch)` 

Here's an example, a function `C` that takes 2 values (`a`, `b`) and subtracts the smaller one from the bigger one. It consists of 3 primitive functions (`>` and 2 times `-`, say `-t`, `-f`) and one `if`.

![if](./if.png)

That's a possible sequence of events, le's say `X`->`a`->`Y` means `X` sends `a` to `Y` and let's assume a caller `Γ`.

```
1  Γ -> a -> C
2  C -> a -> if 
3  Γ -> b -> C
4  C -> b -> if
5  if -> a -> >
6  if -> b -> >
7  > -> condition -> if
8  if -> a -> -t
9  if -> a -> -f
10 if -> b -> -t
11 if -> b -> -f
12 -t -> value_true_branch -> if
13 -t -> value_false_branch -> if  
14 if -> result -> C
15 C -> result -> Γ 
```
Note, that it could as well happen that way.

```
1  Γ -> b -> C
2  Γ -> a -> C
3  C -> a -> if 
4  if -> a -> >
5  if -> a -> -t
6  if -> a -> -f
7  C -> b -> if
8  if -> b -> >
9  if -> b -> -t
10 > -> condition -> if
11 -t -> value_true_branch -> if
12 if -> b -> -f
13 if -> result -> C
14 C -> result -> Γ 
15 -t -> value_false_branch -> if  
```

## implications, maybe

Messages between functions can be logged and used to restore computation states, if the internal state changes of the actors are event sourced too.
All computations are inherently parallel and transparent regarding their location. 

## experience

It was possible to model

- Recursive functions and function calls
- Partially applied functions
- Functions as parameters
- Conditionals

and to express a [functional version of quicksort](http://learnyouahaskell.com/recursion). Multiple quicksorts could be executed in parallel. 

The approach chosen prevented event sourcing eventually.

The main issue was the decision that a function is one actor, a function call are messages to this actor, several calls go to the same actor.
So there must be some logic per actor to maintain those messages from different souces, correlate them with results from subsequent actors and send the results to the proper recipients. 

An implementation without event sourcing proved possible, but already too complicated IMHO. Actors became quite complex, making them bad candidates for event sourcing.

It seems more promising to represent the computation as a data structure that grows as the computation progresses, each element representing one execution of a function, as can be seen in [this branch](https://github.com/curiosag/picoservice/tree/MoreMicro).
Only the recursive sum sample is implemented, but it was way less headache and comes with event sourcing and recovery of the execution state from the event stream. It is single threaded at the moment, but with a few consisderatinos it should scale to arbitrary numbers of threads or one per function, bringing it again closer to the original actor approach. 


### thanks & credits
[Jörg W Mittag](https://stackoverflow.com/users/2988/j%c3%b6rg-w-mittag) for straightening out [my view on conditional expressions](https://stackoverflow.com/questions/58316588/how-to-model-if-expressions-with-actor-systems) and pointing out the historical relationship between the actor model and Smalltalk.

All the zillion articles about ever more micro microservices which caused my mind to generate this approach one morning in the shower while I picked up some old stockings. It has earned me a few insights and cost me lots of time ever since. 

    java.lang.IllegalArgumentException: invalid ActorSystem name [picosörvis], must contain only word characters (i.e. [a-zA-Z0-9] plus non-leading '-' or '_')

	at akka.actor.ActorSystemImpl.<init>(ActorSystem.scala:698)
	at akka.actor.ActorSystem$.apply(ActorSystem.scala:258)
	at akka.actor.ActorSystem$.apply(ActorSystem.scala:302)
	at akka.actor.ActorSystem$.apply(ActorSystem.scala:246)
	at akka.actor.ActorSystem$.create(ActorSystem.scala:176)
	at akka.actor.ActorSystem.create(ActorSystem.scala)
	at miso.ingredients.Actresses.<init>(Actresses.java:21)
	at miso.ingredients.Actresses.instance(Actresses.java:30)
	at miso.ingredients.Actresses.resolve(Actresses.java:41)
	at miso.ingredients.Actress.resolveTracer(Actress.java:34)
	at miso.ingredients.Actress.<init>(Actress.java:20)
	at miso.ingredients.Function.<init>(Function.java:15)
	at miso.ingredients.BinOp.<init>(BinOp.java:32)
	at miso.ingredients.nativeImpl.BinOps.eq(BinOps.java:21)
	at miso.ServiceTest.getModEqZero(ServiceTest.java:509)
	at miso.ServiceTest.testgetModEqZero(ServiceTest.java:523)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:27)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
