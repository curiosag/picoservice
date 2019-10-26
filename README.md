# picoservice
An execution model for some functional language constructs based on message passing with actors.

Harks back to Hewitt's papers but unlike smalltalk puts functions center stage rather than objects. The idea is to create
something remotely similar to e.g. Spring integration, just with bearable syntax.

## pico?

Now that there are services, micro- and even nanoservices there wasn't much of a choice.

As of the promotors of [such stuff](https://www.serverlessops.io/blog/rise-of-the-nanoservice) a nanoservice is

>- Deployable
>  
>- Reusable
>
>- Useful

From that characteristics for a picoservice can be derived.

A picoservice is:

>- Deployable
> 
>- Reusable (sometimes)
>
>- Useless (except in comination with others)

### thanks & credits
[Jörg W Mittag](https://stackoverflow.com/users/2988/j%c3%b6rg-w-mittag) for straightening out [my view on conditional expressions](https://stackoverflow.com/questions/58316588/how-to-model-if-expressions-with-actor-systems) and pointing out the historical relationship between the actor model and Smalltalk.

TODO: &&, ||, split/join

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
