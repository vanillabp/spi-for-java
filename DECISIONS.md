# Decision log

Decisions this repository's code points at. A number is handed out once and never reused or
renumbered, so a citation stays resolvable; a decision which gets overturned keeps its entry,
marked as superseded and naming the entry which replaced it.

A citation in code reads `see decision 2 in the repository's DECISIONS.md`, and it names an entry
of THIS repository only. What the platform integration or a BPMS adapter makes of a decision has
its own entry in that repository, written from that side; a pointer into another repository is the
fragile kind this log exists to avoid.

This is the API a business application writes against, so every entry here is a promise to that
application rather than an implementation note. The detail belongs in
[`README.md`](./README.md) and in the
[wiki](https://github.com/vanillabp/spi-for-java/wiki), which an entry may link.

### 1. The workflow aggregate is the state of a workflow, and the only state there is

A workflow does not keep data in the BPMS. It keeps it in an entity of the application, the
workflow aggregate, which the application owns, persists and queries with its own tools, and
VanillaBP hands to every handler of that workflow. Process variables are not part of this API.

That is what makes a workflow debuggable with the application's own means, and it is what makes a
model portable: an expression which needs a value reads it from what the aggregate SHARES rather
than from something a particular engine happens to hold. It also decides the failure mode. Because
the aggregate is written in the same transaction as everything else the handler did, a rollback
undoes the business change and the workflow step together.

### 2. A workflow is addressed by the identifier the business already has

There is no technical workflow id in this API. The id attribute of the aggregate is what addresses
the workflow, from `startWorkflow` to every later operation, so an application which already knows
a loan request by its number keeps using that number.

The consequence a caller feels is that an id has to exist before a workflow starts, and that it
has to be stable. A generated key which the persistence hands out on flush is fine; a value the
business changes later is not, because the BPMS remembers the one it was given.
See [Natural ids](./README.md#natural-ids).

### 3. What the BPMS gets to see is declared, not guessed

`@SyncWithBPMS` and `@NoSyncWithBPMS` say which values of an aggregate travel to the BPMS, on the
class, on an attribute or on a nested type, with the more specific one winning. The default is the
adapter's, which is why an aggregate without any annotation behaves the way its BPMS needs.

Declaring it is what keeps a model portable. An engine which runs embedded could read the
aggregate live and an application which relies on that works on that engine and takes the wrong
branch on every remote one, silently. Declaring it is also what keeps the payload honest: a value
which nothing in the model reads has no reason to leave the application.

### 4. A `TaskException` is a business outcome, not a failure

A handler which throws `TaskException` has not failed. It reports a result the model has an error
boundary for, so the workflow takes that path and the aggregate is COMMITTED on the way out.
Every other exception is a technical failure: nothing is committed and the BPMS retries.

Which of the two happened is therefore a decision the model and the handler make together, and
this is why a workflow service must not carry a transaction of its own. VanillaBP holds the
transaction and needs a `TaskException` not to roll it back; an application-side `@Transactional`
which joins would mark it rollback-only and turn a modelled outcome into an incident. The
startup check reports that before the first workflow runs.

### 5. A method serves a version range of a deployed process, not a business version

`version` on `@WorkflowTask`, `@WorkflowStartedByBpms` and `@WorkflowEnded` names versions of the
DEPLOYED process definition as the BPMS counts them, or a version tag of the model. It exists
because a BPMS keeps every version it was ever given and workflows keep running on the old ones
while the application only brings the newest model with it.

Two rules follow, and both are checked while the application boots. Ranges of one task must not
overlap, or a delivery would be served by an arbitrary one of two candidates. And a delivery whose
version the BPMS does not report is served only by a method WITHOUT a range, so a method which
names versions never runs on a delivery nobody could match.
See [Versioning of BPMN business-processes](./README.md#versioning-of-bpmn-business-processes).

### 6. A signal is a broadcast within one workflow module

`sendSignal` has no instance-addressed variant, on purpose: a signal in BPMN is a broadcast, and
an application which wants to reach ONE workflow has `correlateMessage`, which is addressed by the
aggregate. What the broadcast reaches is the workflow module of the `ProcessService` it was sent
through, across its processes and not across module boundaries.

Which modules are meant is a business question, so an application which needs a signal in several
of them sends it through the `ProcessService` of each. A signal carries no payload either, for the
same reason a correlated message carries none: what the workflow needs is in its aggregate.
See [Broadcast a signal](./README.md#broadcast-a-signal).

### 7. An asynchronous task is a task which keeps its id

A handler taking a `@TaskId` tells VanillaBP that the workflow stays in this task after the method
returns, and that the application will complete or cancel it later by that id. A handler without
one is done when it returns.

That single parameter is what an adapter needs to know at wiring time, which is why a
contradiction between it and the model is reported while the application boots rather than as an
incident on a live workflow. The same id is what a user task is completed by, so both kinds of
waiting look the same to the application.
See [User tasks and asynchronous tasks](./README.md#user-tasks-and-asynchronous-tasks).
