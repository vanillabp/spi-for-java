# Upgrade notes

Contributor-facing list of breaking changes per version line. These entries feed
the V1→V2 migration guide for VanillaBP users.

## 1.2.0

### Removed: message-object overloads of `ProcessService`

The overloads taking a message `Object` whose class simple name was used as the
message name were removed — data lives exclusively in the workflow aggregate, so
an object whose only role is carrying its class simple name earns no API surface:

- `ProcessService.correlateMessage(A workflowAggregate, Object message)`
- `ProcessService.correlateMessage(A workflowAggregate, Object message, String correlationId)`
- `ProcessService.startWorkflowByMessage(A workflowAggregate, Object message)`

Mechanical replacement: use the `String messageName` variant and pass the message
name explicitly (formerly derived as `message.getClass().getSimpleName()`):

```java
// before
processService.correlateMessage(ride, rideConfirmation);
// after
processService.correlateMessage(ride, "RideConfirmation");
```

Message content is never transmitted to the BPMS — the workflow aggregate is the
single source of truth. Incorporate the message's data into the aggregate before
correlating (this was already the case; the removed overloads only obscured it).

### Viewer/history and message-start methods are now `default`

`getProcessDefinitions(...)`, `getBpmnXml(...)`, `getWorkflowHistory(...)` and
`startWorkflowByMessage(A, String)` are `default` methods throwing
`UnsupportedOperationException` (a VanillaBP adapter implements them). Hand-written
test doubles of `ProcessService` implementing only the pre-1.1.0 surface plus
`getWorkflowModuleId()` keep compiling when query methods are added.
`getWorkflowModuleId()` stays abstract.

## 1.1.0

Inherited breaks of the 1.1.0 line, relevant for V1 users:

- **`@BpmnProcess.primary()` removed** (stays removed by decision). Whether a
  process is the primary one is derived from the `bpmnProcess` vs.
  `secondaryBpmnProcesses` attributes of `@WorkflowService`.
- **Java 21 is the minimum runtime** (previously Java 17).

