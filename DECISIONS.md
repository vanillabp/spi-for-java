# Decision log

These are the decisions the code of this repository points at. A number is handed out once. It is
never reused and never renumbered, so a citation can always be found. A decision which is overturned
keeps its entry. The entry is marked as superseded and names the entry which replaced it.

A citation in code reads `see decision 2 in the repository's DECISIONS.md`. It names an entry of
THIS repository only. The platform integration and each BPMS adapter write down what they make of a
decision in their own repository, as an entry of their own. A pointer into another repository is
the fragile kind of reference this log exists to avoid.

This is the API a business application writes against. So every entry here is a promise to that
application, not a note about the implementation. The details belong in
[`README.md`](./README.md) and in the
[wiki](https://github.com/vanillabp/spi-for-java/wiki), and an entry may link to them.

### 1. The workflow aggregate is the state of a workflow, and the only state there is

A workflow does not keep data in the BPMS. It keeps it in an entity of the application, the
workflow aggregate. The application owns it, persists it and queries it with its own tools.
VanillaBP hands it to every handler of that workflow. Process variables are not part of this API.

That is what makes a workflow debuggable with the application's own means. It also makes a model
portable: an expression which needs a value reads it from what the aggregate SHARES, not from
something a particular engine happens to hold. And it decides what happens on a failure. The
aggregate is written in the same transaction as everything else the handler did. So a rollback
undoes the business change and the workflow step together.

### 2. A workflow is addressed by the identifier the business already has

There is no technical workflow id in this API. The id attribute of the aggregate addresses the
workflow, from `startWorkflow` to every later operation. So an application which already knows a
loan request by its number keeps using that number.

For the caller this means: an id has to exist before a workflow starts, and it has to be stable. A
generated key which the persistence hands out on flush is fine. A value which the business changes
later is not, because the BPMS remembers the one it was given.
See [Natural ids](./README.md#natural-ids).

### 3. What the BPMS gets to see is declared, not guessed

`@SyncWithBPMS` and `@NoSyncWithBPMS` say which values of an aggregate travel to the BPMS. They
work on the class, on an attribute or on a nested type, and the more specific one wins. Without any
annotation, every adapter shares everything. So the default is the same on every BPMS and is not
left to the adapter. An aggregate without any annotation is started only where the workflow allows
it with `allow-full-sync-with-bpms` (see `SyncWithBPMS`).

Declaring it keeps a model portable. An engine which runs embedded could read the aggregate live.
An application which relies on that works on that engine, and takes the wrong branch on every
remote one, without anybody noticing. Declaring it also keeps the payload honest: a value which
nothing in the model reads has no reason to leave the application.

### 4. A `TaskException` is a business outcome, not a failure

A handler which throws `TaskException` has not failed. It reports a result the model has an error
boundary for. So the workflow takes that path, and the aggregate is COMMITTED on the way out.
Every other exception is a technical failure: nothing is committed, and the BPMS retries.

So the model and the handler decide together which of the two happened. This is why a workflow
service must not have a transaction of its own. VanillaBP holds the transaction, and a
`TaskException` must not roll it back. An `@Transactional` on the application side would join the
transaction, mark it rollback-only, and turn a modelled outcome into an incident. The check at
startup reports that before the first workflow runs.

### 5. A method serves a version range of a deployed process, not a business version

*Superseded by decision 8. Decision 8 adds `@BpmnProcess` as the fourth annotation which carries a
version, and it says what a method promises which inherits a range.*

`version` on `@WorkflowTask`, `@WorkflowStartedByBpms` and `@WorkflowEnded` names versions of the
DEPLOYED process definition as the BPMS counts them, or a version tag of the model. It exists
because a BPMS keeps every version it was ever given. Workflows keep running on the old versions,
while the application brings only the newest model with it.

Two rules follow, and both are checked while the application starts. The ranges of one task must
not overlap, or a delivery would be served by a random one of two candidates. And a delivery whose
version the BPMS does not report is served only by a method WITHOUT a range. So a method which
names versions never runs on a delivery nobody could match.
See [Versioning of BPMN business-processes](./README.md#versioning-of-bpmn-business-processes).

### 6. A signal is a broadcast within one workflow module

`sendSignal` has no variant addressed to one instance, on purpose. A signal in BPMN is a broadcast.
An application which wants to reach ONE workflow has `correlateMessage`, which is addressed by the
aggregate. The broadcast reaches the workflow module of the `ProcessService` it was sent through:
all its processes, but no other module.

Which modules are meant is a business question. So an application which needs a signal in several
modules sends it through the `ProcessService` of each. A signal carries no payload either, for the
same reason a correlated message carries none: what the workflow needs is in its aggregate.
See [Broadcast a signal](./README.md#broadcast-a-signal).

### 7. An asynchronous task is a task which keeps its id

A handler which takes a `@TaskId` tells VanillaBP two things. The workflow stays in this task after
the method returns. And the application will complete or cancel it later by that id. A handler
without a `@TaskId` is done when it returns.

An adapter needs to know about this one parameter when it wires the handlers. So a contradiction
between the parameter and the model is reported while the application starts, not as an incident
on a live workflow. A user task is completed by the same id, so both kinds of waiting look the same
to the application.
See [User tasks and asynchronous tasks](./README.md#user-tasks-and-asynchronous-tasks).

### 8. A version range is declared per method or per workflow service class

`@BpmnProcess(version = ...)` is the fallback of `@WorkflowTask`, `@WorkflowStartedByBpms` and
`@WorkflowEnded`. A method which names no version serves the range of the process it was declared
with. A method which names one keeps it exactly as written. The two ranges are never intersected.
So a class with the handlers of one generation of a model says it once. Under decision 5 it said
the same thing once per method, and a method added later without the attribute silently served
every version.

The attribute has been on `@BpmnProcess` since version 1, and nothing read it. This meaning makes it
possible to document it. It is also a promise to an application which wrote it back then and
believed it worked: the `UPGRADE.md` of the platform says what such an application has to check.

Two rules of decision 5 still hold for the range a method ends up with, whichever annotation it came
from. First, the ranges of one BPMN element must not overlap. This now also holds for two classes
which declare the same process. That is the shape this exists for, and it is allowed as long as
their ranges do not overlap. Second, a delivery whose version the BPMS does not report is served
only by a method WITHOUT a range. A method which inherits a range is restricted. So to serve such a
delivery, you need a method whose class names no version either. Every message about a range names
the declaration it came from, because the method it complains about may have no attribute at all.

The process a delivery came from decides which declaration applies, not the class. A class declares
one `bpmnProcess` plus any number of `secondaryBpmnProcesses`, each with a version of its own. And
one method may serve elements of both.
See [Versioning of BPMN business-processes](./README.md#versioning-of-bpmn-business-processes).

### 9. The application names the workflow, and the trigger carries no time

The id of a workflow is the id of its workflow aggregate. The application assigns it, in the
`@WorkflowStartedByBpms` method, and nobody else does. The BPMS holds that id afterwards: Camunda
7 as the business key, Camunda 8 and the Process-Engine-API as a process variable named after the
id attribute of the aggregate. The platform integration writes down in its own decision log what
this means for the platform. This entry says what it changes in this API.

`BpmsStartTrigger` lost its time. The trigger used to carry the moment the start event fired. It
existed for one reason: the core turned that moment into the id of the workflow aggregate. Now the
application names the workflow, so that reason is gone. What is left is a value no BPMS can give
honestly. Camunda 7 does not tell a listener the time it planned the timer for. Camunda 8 does not
either. Both adapters reported `Instant.now()`. That is the moment the notification was processed,
not the moment the event fired. A field which every adapter has to fake is worse than no field. An
application which needs that time models a process variable, fills it by an expression in the BPMN
model and reads it as a `@TaskParam`. The model is the place which knows what "the time" is
supposed to mean.

The kinds now cover every start event. `NONE` and `MESSAGE` joined `TIMER`, `SIGNAL` and
`CONDITIONAL`. The adapters hang their listener on every start event of a process. What a start
means is read from the state of the workflow, not from the kind of its event. So a foreign start
can arrive through a plain start event, and the application has to be able to see that.

Two other ways were turned down. One was a `@NaturalIdentity` annotation. The other was a generic
`BpmsStartTrigger<I>`, which would have carried the name the BPMS holds in the id type of the
application. Both would make every application name a type, for a case almost none of them has. If
the case ever turns up, an annotation of its own is the smaller change.
See [Workflows nobody started through VanillaBP](./README.md#workflows-nobody-started-through-vanillabp).

### 10. An answer to a task of a suspended workflow is refused with a type of its own

Proposed by story 2070. The name `WorkflowSuspendedException` is the default of the story and still
waits for the maintainer.

**Why.** Camunda 7 and Camunda 8 from 8.10 on can suspend a workflow. While it is suspended, both
refuse every answer to one of its tasks. Camunda 7 throws its own `SuspendedEntityInteractionException`,
Camunda 8 answers `409 INVALID_STATE`, the same code it gives for several other states. Before this
type existed, an application got the raw exception of the BPMS, or nothing at all: a user task on
Camunda 8 committed, and its answer failed after the commit until the outbox gave up. An application
which wants to tell its user "this case is stopped, try again later" could not do that without
knowing the BPMS.

**What holds.**

- `io.vanillabp.spi.process.WorkflowSuspendedException`, unchecked, next to `TaskNotFoundException`.
  `completeUserTask`, `cancelUserTask`, `completeTask` and `cancelTask` throw it.
- It is thrown before the transaction of the caller commits. So the transaction rolls back and the
  task stays open. The message says that the same call can be made again after the workflow is
  resumed.
- It carries a message and, where the adapter has one, the exception of the BPMS as its cause. It
  carries no task id or workflow id. The caller named the task when it made the call, so it knows
  them already.
- A suspension which comes after the commit is not reported to the caller. The answer waits in the
  outbox for as long as the workflow is suspended, and the wait is not counted as a failed attempt.
  How an adapter does that is decided in the adapter's own log.
- It is not a `TaskNotFoundException`. The task is still there, and it can be answered later. A
  caller which treats "gone" as final must not do that with a suspended task.

**What it is not.** It is no event. A workflow is suspended and resumed by somebody outside the
application, and Camunda 8 tells no client about it. An extension which needs to know, such as the
Business Cockpit, sees the exception at its own call, or finds out from the BPMS by itself.

### 11. A snapshot reaches Maven Central again, without a sources jar, and a refused request is repeated

Proposed by stories 2162 and 2160, 2026-11-13. Story 2158 brought the profile `publish-snapshots`,
which sends a snapshot to GitHub Packages and to the snapshot repository of Maven Central in one
build. It wrote no decision here. `adapter-platform-integration` made the same change, see decision
139 in adapter-platform-integration for the URL and decision 140 in adapter-platform-integration for
the sources jar and the repeats.

**The snapshot reaches Maven Central again.** The profile `publish-snapshots` sets the snapshot
repository of the POM to GitHub Packages, because that is where `maven-deploy-plugin` uploads.
`central-publishing-maven-plugin` 0.12.0 reads the same setting: where its configuration names no
`centralSnapshotsUrl`, it uploads a snapshot to the snapshot repository of the POM. So since story
2158 every snapshot of this repository went to GitHub Packages twice and never to Maven Central, and
the build stayed green. The configuration of the plugin in the profile now names
`https://central.sonatype.com/repository/maven-snapshots/` as `centralSnapshotsUrl`. The modules
which are not published keep `skip.central.release`, so the list of published modules is the same as
before.

**A snapshot carries no sources jar.** Maven Central answers the GitHub runners with 429 Too Many
Requests, and every file a publish uploads is one more request. The root POM no longer binds
`maven-source-plugin`, so a snapshot carries the jar, the POM and their checksums, and nothing else.
GitHub Packages gets no sources jar either, because both repositories get their files from one
build. A release still carries a sources jar and a javadoc jar: the release workflow runs with the
profile `release` of `io.vanillabp:release-parent`, which binds both. A developer who opens a class
of a snapshot in the IDE sees decompiled code again, as before the root POM bound the plugin.

**A refused request is repeated.** `.mvn/maven.config` tells Maven to repeat a request which a
server answers with 429 or 503. It repeats a request at most twice and waits as long as the
`Retry-After` header asks, up to 35 minutes. Central blocks a runner for about half an hour, and
every request during the block makes it longer, so waiting once is better than asking again. Maven
also uploads one file after the other instead of five at once. The file applies to every build of
this repository, on a laptop and in a pull request as well.

**Where it is referred to:** the comments at `centralSnapshotsUrl` and `skipPublishing` in the
profile `publish-snapshots` of `pom.xml`, the comment in `pom.xml` where `maven-source-plugin` used
to be bound, and the comments in `.mvn/maven.config`. None of them cites a number.
