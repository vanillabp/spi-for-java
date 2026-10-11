# Upgrade notes

A list of breaking changes per version line, for contributors. These entries feed the migration
guide from version 1 to version 2 for VanillaBP users.

## 2.0

### Removed: message-object overloads of `ProcessService`

The overloads which took a message `Object`, and used the simple name of its class as the message
name, were removed. Data lives only in the workflow aggregate. An object whose only job is to carry
the simple name of its class is not worth a place in the API:

- `ProcessService.correlateMessage(A workflowAggregate, Object message)`
- `ProcessService.correlateMessage(A workflowAggregate, Object message, String correlationId)`
- `ProcessService.startWorkflowByMessage(A workflowAggregate, Object message)`

The replacement is mechanical. Use the `String messageName` variant and pass the message name
yourself. It used to be derived as `message.getClass().getSimpleName()`:

```java
// before
processService.correlateMessage(ride, rideConfirmation);
// after
processService.correlateMessage(ride, "RideConfirmation");
```

The content of a message is never sent to the BPMS. The workflow aggregate is the single source of
truth. Copy the data of the message into the aggregate before you correlate. This was already the
case before. The removed overloads only hid it.

### Viewer/history and message-start methods are now `default`

`getProcessDefinitions(...)`, `getBpmnXml(...)`, `getWorkflowHistory(...)` and
`startWorkflowByMessage(A, String)` are `default` methods which throw
`UnsupportedOperationException`. A VanillaBP adapter implements them. Some test doubles of
`ProcessService` are written by hand and implement only the methods from before 1.1.0 plus
`getWorkflowModuleId()`. They keep compiling when query methods are added.
`getWorkflowModuleId()` stays abstract.

## 1.1.0

Breaking changes inherited from the 1.1.0 line, which matter for version 1 users:

- **`@BpmnProcess.primary()` removed** (and it stays removed, by decision). Whether a process is
  the primary one follows from the attributes `bpmnProcess` and `secondaryBpmnProcesses` of
  `@WorkflowService`.
- **Java 21 is the minimum runtime** (before it was Java 17).

