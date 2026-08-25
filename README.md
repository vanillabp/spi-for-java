![](./readme/vanillabp-headline.png)

*Vanilla BP* is **an aspect orientated service provider interface (SPI) for workflow systems as a Java developer would expect it to be**.

Every example on this page is about approving a loan: a customer asks for an amount, a credit rating is retrieved, partners are asked for an offer and the request is decided. That is the same business case the [VanillaBP blueprints](https://github.com/vanillabp-blueprints) implement, so a link followed from here lands in a model you have already read about. The blueprints name their beans after the role they play (`Workflow`, `WorkflowTaskHandler`, `Service`), because each of them holds exactly one workflow; on this page the classes are named after the business case (`LoanApproval`, `LoanApprovalService`), because the examples stand next to each other without that context. The process id, the message names, the task names and the attributes of the workflow-aggregate are literally the same in both places.

*Heads up:* If you want to learn about the things we had in mind creating this SPI then don't miss to also read the [About the SPI](#about-the-spi) section afterwards :wink:. It also includes links to [available adapters](#available-adapters) to use this SPI with an existing workflow system.

*This README documents the SPI itself.* How you set it up in an application, meaning workflow modules, aggregate persistence, configuration, and the platform you run on (Spring Boot, Quarkus), is documented in the [VanillaBP wiki](https://github.com/vanillabp/adapter-platform-integration/wiki), which also links to each BPMS adapter's own wiki.

## Content

1. [How it looks like](#how-it-looks-like)
2. [Usage](#usage)
   1. [Process-specific workflow-aggregate](#process-specific-workflow-aggregate)
   2. [Start a workflow](#start-a-workflow)
   3. [Wire up a process](#wire-up-a-process)
   4. [Wire up a task](#wire-up-a-task)
   5. [Wire up an expression](#wire-up-an-expression)
3. [Advanced topics](#advanced-topics)
   1. [Process variables](#process-variables)
   2. [What the BPMS gets to see](#what-the-bpms-gets-to-see)
   3. [Natural ids](#natural-ids)
   4. [Correlate an incoming message](#correlate-an-incoming-message)
   5. [Broadcast a signal](#broadcast-a-signal)
   6. [Tell the BPMS that the aggregate changed](#tell-the-bpms-that-the-aggregate-changed)
   7. [Learn that a workflow ended](#learn-that-a-workflow-ended)
   8. [Versioning of BPMN business-processes](#versioning-of-bpmn-business-processes)
   9. [Call-activities](#call-activities)
   10. [Multi-instance](#multi-instance)
   11. [User tasks and asynchronous tasks](#user-tasks-and-asynchronous-tasks)
   12. [The workflow module a service belongs to](#the-workflow-module-a-service-belongs-to)
   13. [What the API throws](#what-the-api-throws)
4. [Viewing BPMN and execution history of workflows](#viewing-bpmn-and-execution-history-of-workflows)
   1. [Showing the BPMN of a simple workflow](#showing-the-bpmn-of-a-simple-workflow)
   2. [Showing the BPMN of a complex workflow](#showing-the-bpmn-of-a-complex-workflow)
5. [About the SPI](#about-the-spi)
   1. [Prerequisites](#prerequisites)
   2. [Motivation](#motivation)
   3. [Goals](#goals)
   4. [Available Adapters](#available-adapters)
   5. [Concept](#concept)
6. [Decision log](#decision-log)
7. [Noteworthy & Contributors](#noteworthy--contributors)
8. [License](#license)

## How it looks like

This is a section of a loan approval workflow and should give you an idea of how the Vanilla BP SPI is used in your business code:

> **Screenshot to be added:** `readme/example.png` - the section of the process `loan_approval` cut
> from the blueprint [`bpmn-multi-instance-task`](https://github.com/vanillabp-blueprints/bpmn-multi-instance-task-springboot),
> showing the start event "Loan requested", the service task "Retrieve credit rating" and the
> multi-instance service task "Request partner offer" with its parallel marker.

*Screenshot of [Camunda Modeler](https://camunda.com/en/download/modeler/)*

```java
@Component
@WorkflowService(
        workflowAggregateClass = LoanApproval.class,
        bpmnProcess = @BpmnProcess(bpmnProcessId = "loan_approval"))
public class LoanApprovalService {

    @Autowired
    private ProcessService<LoanApproval> processService;

    @Autowired
    private CreditRatingClient creditRatings;

    @Autowired
    private PartnerClient partners;

    @Transactional
    public String loanRequested(
            final String loanRequestId,
            final int amount) {

        final var loanApproval = new LoanApproval(loanRequestId, amount);
        ...
        return processService
                .startWorkflow(loanApproval)
                .getLoanRequestId();
    }

    @WorkflowTask
    public void retrieveCreditRating(
            final LoanApproval loanApproval) {

        if (loanApproval.getCreditRating() != null) {
            return; // a BPMS may deliver a task twice
        }

        loanApproval.setCreditRating(
                creditRatings.rate(
                        loanApproval.getLoanRequestId(),
                        loanApproval.getAmount()));
    }

    @WorkflowTask
    public void requestPartnerOffer(
            final LoanApproval loanApproval,
            @MultiInstanceElement("ServiceTask_RequestPartnerOffer")
            final String partnerId) {

        partners.requestOffer(
                partnerId,
                loanApproval.getLoanRequestId(),
                loanApproval.getAmount());
    }
    ...
```

The `@Transactional` sits on the method calling `ProcessService`, and on no other method of the class. Starting a workflow needs a transaction of yours, a task method must not have one: VanillaBP runs a task method in a transaction it owns and commits that transaction when a [`TaskException`](#wire-up-a-task) passes it, which an application transaction joining in would turn into a rollback. The application does not boot if a transaction annotation covers a `@WorkflowTask` method, and the message names the method.

The first thing `retrieveCreditRating` does is to check whether there is a rating already. A remote BPMS may deliver the same task more than once, so a task method is written to be repeatable, keyed on the state of the aggregate.

## Usage

### Process-specific workflow-aggregate

Typically, you have data needed to fulfill the purpose of the workflow. This might be values like customer ID, order ID or in case of the loan approval the requested amount and the credit rating retrieved for it:

```java
@Entity
@Table(name = "LOAN_APPROVAL")
@Getter
@Setter
public class LoanApproval {
  @Id
  private String loanRequestId; // see section "Natural ids"
  private Integer amount;
  private Integer creditRating;
  private String ratingBand;
  private String outcome;
}
```

This data has a 1:1 relationship to a particular workflow (a running instance of a BPMN process).
The Vanilla BP SPI uses a dedicated entity per workflow for storing those values. In terms of DDD this entire tree is called *an aggregate*.

When using JPA for aggregate persistence, this entity might be split up into a couple of sub-entities
(many-to-many, one-to-many, many-to-one relations and embedded objects) but the root of that entity-tree is the record connected to the workflow.

It is also possible to use another persistence technology than JPA like NoSQL databases.
Please read the platform integration documentation for details.

### Start a workflow

There is a ready-to-use service bean available called `ProcessService`. It is a generic bean using the workflow aggregate's class as a generic parameter and can be injected in any Spring component:

```java
@Autowired
private ProcessService<LoanApproval> processService;
```

To start a workflow we can use it as part of a typical bean method which may be called due to a business event (e.g. user hits a button):

```java
@Transactional
public void loanRequested(LoanRequest request) {
     // use the request to initialize the aggregate
     var loanApproval = new LoanApproval(request);
     // start the process
     processService.startWorkflow(loanApproval);
}
```

The aggregate is persisted and the workflow is started within the transaction of the caller, so a workflow without its aggregate cannot happen. What comes back is the persisted aggregate, attached where the persistence layer works that way (e.g. JPA).

If the process starts with a message start event instead of a plain start event, use `startWorkflowByMessage(aggregate, messageName)` instead:

```java
processService.startWorkflowByMessage(loanApproval, "LoanRequested");
```

Only the *name* of the message reaches the BPMS. Whatever the message carried belongs on the aggregate before the call, which is the same rule as for [correlating a message](#correlate-an-incoming-message).

#### Workflows the BPMS starts

Some processes start without anybody asking: a timer start event fires, a signal start event receives a broadcast, or a conditional start event's condition becomes true. Nobody hands VanillaBP an aggregate then, so VanillaBP builds one: it instantiates the aggregate class (which needs a constructor without arguments), assigns an ID and copies the process variables the model set into attributes of the same name.

Annotate a method of your workflow service if you want a say - to build the aggregate yourself:

```java
@WorkflowStartedByBpms
public NightlyReview buildAggregate(BpmsStartTrigger trigger) {
     return new NightlyReview(trigger.time());
}
```

or to enrich the one VanillaBP built:

```java
@WorkflowStartedByBpms(id = "StartEvent_ScheduledReview")
public void enrich(NightlyReview review, @TaskParam("region") String region) {
     review.setRegion(region);
}
```

The method may take the workflow aggregate, a `BpmsStartTrigger` and process variables via `@TaskParam`, in any order. The trigger says which kind of start event fired (`TIMER`, `SIGNAL` or `CONDITIONAL`), when it fired, the name of the signal where it was one, and the BPMN id of the start event. A message start event is not among the kinds, because that one is triggered by the application through `startWorkflowByMessage`, which carries the aggregate. Whether your BPMS can serve such a start at all is documented in the [adapter platform's wiki](https://github.com/vanillabp/adapter-platform-integration/wiki/Starting-workflows). The blueprint [`bpmn-bpms-initiated-start`](https://github.com/vanillabp-blueprints/bpmn-bpms-initiated-start-springboot) runs it.

### Wire up a process

Starting a workflow or correlating a message (explained in the [Advanced topics](#correlate-an-incoming-message) section) are actions originated in our custom business code typically triggered by some kind of business event (e.g. user hits a button). Wiring a process, a task or an expression is about connecting BPMN elements to our software components. In these situations the action to run our business code is initiated by the workflow system. So, we have to introduce markers to let the engine know where to find the right code to run.

We introduce a name based approach for the binding in an aspect-oriented style. As a basis for this binding the BPMN process-id is used:

> **Screenshot to be added:** `readme/process_propertiespanel.png` - the property panel of the
> Camunda Modeler for the process `loan_approval`, showing the field "Process id" holding
> `loan_approval`.

*Screenshot of [Camunda Modeler](https://camunda.com/en/download/modeler/)*

#### Software-first approach

Developers might want to use a BPMN and a BPMN-engine to improve readability or maintainability of their software since this takes a lot of coding away. In this situation the service bean might be created upfront.

Use the service's class-name as the process BPMN's process-id to wire up the component to the process by simply adding the `@WorkflowService` annotation:

```java
@Component
@WorkflowService(workflowAggregateClass = LoanApproval.class)
public class LoanApprovalService {
  ...
}
```

The BPMN then carries `LoanApprovalService` as its process-id, and nothing else has to be said. The mandatory annotation attribute `workflowAggregateClass` references the class used as workflow-aggregate of this workflow.

#### BPMN-first approach

In case of a given BPMN file the component needs to be mapped by setting the `bpmnProcess` attribute if it is not the same string as the class-name:

```java
@Component
@WorkflowService(
        workflowAggregateClass = LoanApproval.class,
        bpmnProcess = @BpmnProcess(bpmnProcessId = "loan_approval")
    )
public class LoanApprovalService {
  ...
}
```

If the service-bean becomes huge due to the number of tasks of the workflow then multiple service-beans can be annotated with the same `@WorkflowService` annotation. The only precondition for this is to avoid duplicate task wiring. However, if there is a method wired twice this will be detected on startup and reported by throwing an exception.

### Wire up a task

Similar to [wiring a process](#wire-up-a-process) an aspect-oriented approach is used for the task binding. This applies to service tasks, send tasks, business rule tasks and user tasks.

> **Screenshot to be added:** `readme/task_propertiespanel.png` - the property panel of the Camunda
> Modeler for the service task "Retrieve credit rating" of the process `loan_approval`, showing the
> task definition `retrieveCreditRating`.

*Screenshot of [Camunda Modeler](https://camunda.com/en/download/modeler/)*

The `@WorkflowTask` annotation is used to mark a method responsible for certain BPMN task:

```java
@WorkflowTask
public void retrieveCreditRating(LoanApproval loanApproval) {
  ...
}
```

#### Software-first approach

In this situation for the name of the BPMN task definition the name of the method has to be used (e.g. `retrieveCreditRating`) as shown in the screenshot above:

```java
@WorkflowTask
public void retrieveCreditRating(LoanApproval loanApproval) {
  ...
}
```

#### BPMN-first approach

If the BPMN is given, then the name used in the BPMN can be mapped by the annotation attribute `taskDefinition` if it does not match the method's name:

```java
@WorkflowTask(taskDefinition = "RETRIEVE_CREDIT_RATING")
public void retrieveCreditRating(LoanApproval loanApproval) {
  ...
}
```

as an alternative the task can be wired by the task's BPMN id:

```java
@WorkflowTask(id = "ServiceTask_RetrieveCreditRating")
public void retrieveCreditRating(LoanApproval loanApproval) {
  ...
}
```

One method may serve several BPMN elements: the annotation is repeatable, and the container `@WorkflowTasks` is what the compiler builds out of it.

```java
@WorkflowTask(taskDefinition = "retrieveCreditRating")
@WorkflowTask(taskDefinition = "refreshCreditRating")
public void retrieveCreditRating(LoanApproval loanApproval) {
  ...
}
```

#### Workflow-aggregate argument

```java
@WorkflowTask
public void retrieveCreditRating(LoanApproval loanApproval) {
  ...
}
```

As mentioned in section [Process-specific workflow-aggregate](#process-specific-workflow-aggregate) for each workflow an entity-record is used as a workflow-aggregate. So, whenever a service-method is called there is one parameter accepted: The workflow-aggregate providing values of the current workflow.

*Hint:* These workflow task methods do not return any value because they operate on the given data from the workflow-aggregate and also store new data in the workflow-aggregate if necessary. So, just change the field values of the aggregate as the [BPMS-specific adapter](#available-adapters) used will take care of persisting these changed values.

#### Values the model maps into the task

A BPMN task may carry an input mapping, which is how a model hands a task something the aggregate does not hold. Such a value is picked up by a parameter annotated with `@TaskParam`:

```java
@WorkflowTask
public void retrieveCreditRating(
        LoanApproval loanApproval,
        @TaskParam("ratingProvider") String provider) {
  ...
}
```

This is the one direction in which a value travels *into* your code without passing the aggregate. It is a value of the model, so the model may point the task at another rating provider without the code being touched.

#### The transaction is VanillaBP's

VanillaBP loads the aggregate, calls the method and saves the aggregate, all in one transaction it owns. Do not declare a transaction of your own on a workflow service class or on a `@WorkflowTask` method: it would join VanillaBP's transaction and mark it rollback-only as soon as a `TaskException` passes it, which discards everything the method wrote although the workflow takes the BPMN error path. VanillaBP does not let that happen unnoticed and refuses to boot, naming the method. Methods calling `ProcessService` do need their own transaction, so annotate those instead of the whole class.

#### What a task method may throw

A task method has two ways to end badly, and the BPMN reacts to each of them differently.

```java
@WorkflowTask
public void retrieveCreditRating(LoanApproval loanApproval) {

    // the rating provider is down: a technical failure
    final var rating = creditRatings.rate(loanApproval.getLoanRequestId());

    loanApproval.setCreditRating(rating);

    if (rating < MINIMUM_RATING) {
        loanApproval.setRejectionReason("rating " + rating + " is too low");
        // a business outcome the model has an error boundary event for
        throw new TaskException("loan-rejected");
    }
}
```

A `TaskException` is not a failure. It names a BPMN error code, the workflow leaves the task through the matching error boundary event, and everything the method wrote onto the aggregate **is committed** on the way out, including the rejection reason above. Where a model tells the error name and the error code apart, the second constructor takes both (`new TaskException("Loan rejected", "loan-rejected")`), and the two are readable as `getErrorName()` and `getErrorCode()`. Every other exception is a technical failure: nothing is committed and the BPMS applies its retry semantics. Which of the two happened is a decision the model and the method make together, which is why the transaction has to stay VanillaBP's.

The blueprint [`bpmn-service-task`](https://github.com/vanillabp-blueprints/bpmn-service-task-springboot) plays all three outcomes through, and [`bpmn-error-escalation`](https://github.com/vanillabp-blueprints/bpmn-error-escalation-springboot) shows what a model does with the error afterwards.

### Wire up an expression

There are two major situations in which expressions are used:

1. A path decision has to be taken (exclusive gateway, inclusive gateway, conditional flows)

   > **Screenshot to be added:** `readme/expression_propertiespanel.png` - the property panel of the
   > Camunda Modeler for the sequence flow "good enough" leaving the exclusive gateway "How does the
   >
   >> rating look?" in the blueprint [`bpmn-gateways`](https://github.com/vanillabp-blueprints/bpmn-gateways-springboot),
   >> showing the condition expression `${ratedAcceptable}`.

   *Screenshot of [Camunda Modeler](https://camunda.com/en/download/modeler/)*

2. A value needs to be calculated (e.g. x business-days as a timer-event definition)

   > **Screenshot to be added:** `readme/timer_propertiespanel.png` - the property panel of the
   > Camunda Modeler for the timer intermediate catch event "Cool-off period" of the blueprint
   > [`bpmn-timer`](https://github.com/vanillabp-blueprints/bpmn-timer-springboot), showing a timer
   > definition of type duration holding the expression `${coolOffPeriod}`.

   *Screenshot of [Camunda Modeler](https://camunda.com/en/download/modeler/)*

The expression specified in the BPMN will be used to retrieve the value from the workflow-aggregate by using a getter or, if there is no getter, by accessing the named field. In case of the getter the result can also be computed on-the-fly:

```java
@Entity
@Table(name = "LOAN_APPROVAL")
public class LoanApproval {
    ...
    private String ratingBand;
    ...
    public boolean isRatedAcceptable() {
       return "acceptable".equals(ratingBand);
    }
    ...
    public String getCoolOffPeriod() {
        var holidays = HolidayManager.getInstance(HolidayCalendar.AUSTRIA);
        var nextBusinessDay = Instant.now();
        while (holidays.isHoliday(nextBusinessDay)) {
            nextBusinessDay = nextBusinessDay.plus(1, ChronoUnit.DAYS);
        }
        return Duration.between(Instant.now(), nextBusinessDay).toString();
    }
}
```

Letting the model ask a question (`ratedAcceptable`) instead of comparing numbers itself is worth the getter. The data behind the answer stays free to change, so `ratingBand` can become an enum or three columns later without the BPMN or a running workflow noticing.

*Hint:* Each [BPMS-specific adapter](#available-adapters) implements this *magic* to redirect attribute references in BPMN expressions to the proper getter of your workflow-aggregate. Which values reach the BPMS at all is the subject of [What the BPMS gets to see](#what-the-bpms-gets-to-see).

## Advanced topics

### Process variables

If you are familiar with any workflow system then you might know about process-variables you can use to store information the workflow needs to fulfill decisions like at sequence-flow conditions. As shown in upper sections the Vanilla BP SPI has no process-variables in its API and makes the workflow system [use the workflow-aggregate instead](#wire-up-an-expression):

> **Screenshot to be added:** `readme/expression_propertiespanel.png` - the same property panel as
> above: the sequence flow "good enough" of the blueprint `bpmn-gateways` with the condition
> expression `${ratedAcceptable}`, which is an attribute of the workflow-aggregate and not a process
> variable anybody assigned.

*Screenshot of [Camunda Modeler](https://camunda.com/en/download/modeler/)*

Reasons for not using process-variables:

1. BPMN
   1. Process variables have no schema and therefore they cannot be documented and tested easily
   2. Using process variables, the "contract" between your BPMN model and your code can become quite intransparent
   3. No type-safety in regard to the information needed by the process
   4. Tight-coupling of the code and the business process definition
2. Operation of workflows
   1. Historic process-variables need to be cleaned up in order not to exhaust your database (even for cleaning-up itself!)
   2. Process-variables tend to pollute the execution context because typically they are not cleaned up by developers. The longer the process is running the more unused variables are stored.
   3. For call-activities, *all* process-variables are copied as a default, even including the temporary and unused variables mentioned above.
   4. Schema evolution: Process variables may have complex types and evolve over time. Migrating such values is a hard job.

Under the hood a BPMS still needs the values its models read, and VanillaBP writes them as process variables whenever it talks to the BPMS on behalf of the workflow. Which values those are is [declared on the aggregate](#what-the-bpms-gets-to-see), and no application code ever names one.

### What the BPMS gets to see

An aggregate holds what the business case needs. A BPMS needs the part of it the model reads, and `@SyncWithBPMS` and `@NoSyncWithBPMS` are how you say which part that is. Both work on the aggregate class, on an attribute and on a getter, and the more specific declaration wins:

```java
@Entity
@Table(name = "LOAN_APPROVAL")
@NoSyncWithBPMS                    // nothing is shared by default ...
public class LoanApproval {

    @Id
    private String loanRequestId;  // ... except the id, which VanillaBP always shares

    private String customerName;

    private Integer creditRating;

    @SyncWithBPMS                  // ... and the question the model asks
    public boolean isRatedAcceptable() {
        return creditRating != null && creditRating >= MINIMUM_RATING;
    }
}
```

An attribute annotated neither way inherits the behavior of its owner, which is the aggregate class or the attribute it belongs to, nested objects and collection elements included. The outermost default belongs to the BPMS adapter, and every VanillaBP adapter shares everything: an aggregate carrying no annotation at all reaches its model completely, on an embedded engine as well as on a remote one. A model may therefore read the same attributes wherever it runs.

Declaring it anyway is what keeps a model portable: an engine running embedded could read the aggregate live instead, and an application relying on that takes the wrong branch on every remote BPMS, silently. It also keeps the payload honest, because the customer's name has no reason to leave the application if no condition reads it.

The blueprint [`bpmn-aggregate-decoupling`](https://github.com/vanillabp-blueprints/bpmn-aggregate-decoupling-springboot) is built around this pair.

### Natural ids

The aggregate uses a natural id as a primary key, so for one specific natural key a particular process started twice is identified as a duplicate and rejected.

A natural id is a primary key which uniquely identifies a single business-case. That might be an order-id, a trouble-ticket-id or a checksum calculated based on the use-case's attributes.

This natural id has to be chosen wisely, because it is used to identify duplicate requests, which might occur in a distributed, fault-tolerant system. It is also fine to use an auto-increment/UUID but in this case de-duplication will not work.

The id is also what addresses the workflow in every later call: there is no technical workflow id in this API. That is why the value has to exist before the workflow starts and has to stay stable afterwards. A key the persistence hands out on flush is fine, a value the business edits later is not, because the BPMS remembers the one it was given.

### Correlate an incoming message

Some BPMN elements are meant to wait for external messages like receive tasks and message catch events. The content of the message is typically incorporated into the domain aggregate to be used by upcoming tasks.

However, the event of the incoming message is also used to make the workflow wake up and process whatever comes after the "sleeping" receive task. This mechanism is called message correlation and is based on the message's name registered for the receive task.

*Important:* The content of the message is *never* transmitted to the BPMS - the workflow aggregate is the single source of truth. BPMN logic (expressions, conditions) reads data from the workflow aggregate, not from message payloads. So incorporate any data of the incoming message into the workflow aggregate before correlating the message. The same applies to starting workflows by message (`startWorkflowByMessage`).

One can use the `ProcessService` to perform that message correlation:

```java
    @Autowired
    private LoanApprovalRepository loanApprovals;

    @Autowired
    private ProcessService<LoanApproval> processService;

    @Transactional
    public void contractSigned(ContractSigned message) {
         var loanApproval = loanApprovals.findById(message.getLoanRequestId()).orElseThrow();
         loanApproval.setSignedAt(message.getSignedAt());
         processService.correlateMessage(loanApproval, "ContractSigned");
    }
```

VanillaBP finds the workflow by the aggregate's id, so no correlation key is modelled and none is passed. If no configured BPMS knows the workflow, a `WorkflowNotFoundException` says so rather than losing the message silently.

*Hint:* To start a new workflow by a message start event use `startWorkflowByMessage` instead.
In this situation the aggregate must not be persisted before.

Additionally, if there are several receive tasks "waiting" for the same message then you need to define a correlation-id as a third parameter of `correlateMessage`.

The blueprints [`bpmn-message-correlation`](https://github.com/vanillabp-blueprints/bpmn-message-correlation-springboot) and [`bpmn-message-start`](https://github.com/vanillabp-blueprints/bpmn-message-start-springboot) show both halves.

### Broadcast a signal

A BPMN signal is a broadcast: every element waiting for it reacts, and processes having a signal start event are started by it. That is why `sendSignal` takes no workflow aggregate - unlike a message, a signal is not addressed to one workflow:

```java
processService.sendSignal("InterestRatePublished");
```

Pass the signal name as modelled; VanillaBP applies the name scoping of the workflow module. No payload travels with the signal, for the same reason a message carries none: the workflow aggregate is the single source of truth. Whatever the receiving workflows need has to be readable from the application's own data by the time they wake up.

The broadcast is scoped to the **workflow module** of the service you called: across the processes of that module, not across modules, and addressed with the tenant and client of each adapter it is deployed to. Where the module prefixes its identifiers, the signal name is prefixed too. A signal meant for several workflow modules is sent through the `ProcessService` of each of them - which modules are meant is a business decision.

Within the module the signal reaches every BPMS it is deployed to, which keeps a broadcast complete while workflows are being migrated from one BPMS to another. Call it within a transaction: an embedded BPMS broadcasts inside it, and for a remote BPMS the outbox entry carrying the broadcast travels with it - so a rollback takes the broadcast with it either way. There is nothing to deduplicate a signal by, so a redelivered entry may broadcast twice; do not build exactly-once expectations on it.

A signal is not buffered either. It reaches whoever waits for it at that very moment, and a workflow arriving at its catch event a moment later gets nothing. Where a delivery has to wait for its recipient, correlate a message to that workflow instead. The blueprint [`bpmn-signals`](https://github.com/vanillabp-blueprints/bpmn-signals-springboot) runs a broadcast against several waiting workflows.

### Tell the BPMS that the aggregate changed

VanillaBP hands the aggregate's shared state to the BPMS at the moments it talks to it anyway: starting a workflow, finishing a `@WorkflowTask` method, completing or canceling a task, correlating a message. Sometimes a change has to arrive between those moments - a conditional event waits for it, or a gateway is evaluated before your next task runs:

```java
loanApproval.setCollateralConfirmed(true);
processService.aggregateChanged(loanApproval);
```

WHAT is pushed is not decided here: it stays the part of the aggregate shared with the BPMS ([`@SyncWithBPMS`](#what-the-bpms-gets-to-see)). The call says "look again", nothing more - and the aggregate remains the single source of truth.

The second overload picks the scope:

```java
processService.aggregateChanged(loanApproval, taskId);
```

With a task id the values land in the scope that task RUNS in: the process, an embedded subprocess, or the one iteration of a multi-instance embedded subprocess. That is what multi-instance work needs, where every iteration has a scope of its own and a workflow-wide write would be a lost update between them, and it is the scope an event subprocess with a conditional start event listens on. The task's own context is deliberately skipped - values there would serve a boundary event of that task and disappear with it.

It does **not** additionally write the workflow's global scope, so a gateway after the multi-instance evaluates the older state unless you also push globally. Pass the task id your `@TaskId` parameter was given.

Call it within a transaction, like every operation reaching a BPMS. A workflow which already ended makes the push a warned no-op, a workflow no BPMS knows raises a `WorkflowNotFoundException` - and the aggregate is saved in both cases. Repeating a push is harmless: the values are read when it happens, not when it was scheduled.

What a BPMS does with the new values is its own business - Camunda 7 re-evaluates the conditions of waiting conditional events, others simply hold them until something reads them.

### Learn that a workflow ended

Annotate a method to be told when a workflow finished, instead of modelling a service task in front of every end event:

```java
@WorkflowEnded
public void loanApprovalEnded(LoanApproval loanApproval, WorkflowEnd end) {
     loanApproval.setClosedAt(end.time());
}
```

VanillaBP loads the workflow aggregate, calls the method and saves the aggregate. The annotation is optional and a model without it pays nothing: adapters attach their listener only where a method exists. The method may take the aggregate and a `WorkflowEnd` in any order, and `WorkflowEnd` says when the workflow ended, which end event it reached where the BPMS reports one, and whether it `COMPLETED` or was `TERMINATED` without reaching an end event. An adapter whose BPMS cannot tell the two apart reports `COMPLETED` and says so in its documentation, because a faked distinction would be worse than none.

Two properties worth knowing. The notification is **at-least-once**, so write the method idempotently. And whether it runs in the transaction which ended the workflow depends on the BPMS: an embedded engine ends the workflow and calls the method in one transaction, a remote BPMS delivers the notification afterwards. What a BPMS can report about the KIND of end also differs - see the [adapter platform's wiki](https://github.com/vanillabp/adapter-platform-integration/wiki/Starting-workflows#when-a-workflow-ends) and the blueprint [`bpmn-workflow-ended`](https://github.com/vanillabp-blueprints/bpmn-workflow-ended-springboot).

### Versioning of BPMN business-processes

Once a BPMN model changes in a way older workflows cannot follow, you can tell VanillaBP which versions of the
process a method serves:

```java
@WorkflowTask(taskDefinition = "assessRisk", version = "1")
public void assessRiskManually(
        final LoanApproval loanApproval) { ... }

@WorkflowTask(taskDefinition = "assessRisk", version = ">1")
public void assessRiskAutomatically(
        final LoanApproval loanApproval) { ... }
```

The version meant is the version of the deployed process **definition** as the BPMS counts it (Camunda 7 and
Camunda 8 count integers upwards per BPMN process id), not a version your application invents. Instead of that
number a boundary may name a **version tag** given in the model (`camunda:versionTag` in Camunda 7,
`zeebe:versionTag` in Camunda 8), which is what teams usually put their release name into.

Valid formats:
* missing attribute or `'*'`: every version
* `'3'`: only version "3"
* `'release-2024'`: every version tagged that way
* `'1-3'` or `'v1.0..v2.0'`: a range, both boundaries included
* `'<3'` / `'<=3'`, `'>3'` / `'>=3'`: open ended, the boundary excluded respectively included

Ranges accept `..` as well as `-` as their separator; use `..` whenever a boundary is a version tag containing a `-`.
"Greater" and "less" mean the deployment order, which for a BPMS counting versions upwards is the numeric order.

The same attribute exists on `@WorkflowStartedByBpms` and `@WorkflowEnded` and means the same there.

Two things are worth knowing. Several methods may serve one BPMN element as long as their versions do not overlap;
overlapping specifications are a mistake VanillaBP reports when the application starts, naming both methods. And a
BPMS which does not report the version of a process serves only methods without a version specification. See the
[adapter platform's wiki](https://github.com/vanillabp/adapter-platform-integration/wiki/Workflow-tasks#versions-of-a-process)
for what each BPMS can tell, and the blueprint [`bpmn-versioning`](https://github.com/vanillabp-blueprints/bpmn-versioning-springboot)
for a model changed under running workflows.

### Call-activities

In general BPMN models can be split up by using the "call-activity" task which is not meant to be executed by custom business code but instead is interpreted by the workflow system to spawn another process instance based on another BPMN.

There are two different situations in which you might want to split up the BPMN model into several smaller models:

#### 1. Decomposition - a call-activity is used to hide complexity

> **Screenshot to be added:** `readme/callactivity_propertiespanel.png` - the property panel of the
> Camunda Modeler for the call activity "Assess risk" of the process `loan_approval` in the blueprint
> [`bpmn-call-activity-decomposition`](https://github.com/vanillabp-blueprints/bpmn-call-activity-decomposition-springboot),
> showing the called element `risk_assessment`.

*Screenshot of [Camunda Modeler](https://camunda.com/en/download/modeler/)*

In this situation the workflow-aggregate entity created for the root workflow is also used for the workflows spawned by call-activities. The reason for this is, that one still could put the content of the call-activities BPMN into to parent BPMN (e.g. as an embedded subprocess).

Both processes belong to the same workflow-aggregate, and the aggregate has exactly one `ProcessService` (that is what `ProcessService<LoanApproval>` injects). The called process is therefore declared as a *secondary* process of the service bean which declares the process to be started:

```java
@Component
@WorkflowService(
        workflowAggregateClass = LoanApproval.class,
        bpmnProcess = @BpmnProcess(bpmnProcessId = "loan_approval"),
        secondaryBpmnProcesses = @BpmnProcess(bpmnProcessId = "risk_assessment"))
public class LoanApprovalService {
  ...
}
```

The handlers of the called process may live in a service bean of their own, as long as that bean declares the same `bpmnProcess`:

```java
@Component
@WorkflowService(
        workflowAggregateClass = LoanApproval.class,
        bpmnProcess = @BpmnProcess(bpmnProcessId = "loan_approval"))
public class RiskAssessmentService {
  ...
}
```

What does **not** work is two service beans of one workflow-aggregate declaring different processes as their `bpmnProcess`: `startWorkflow` starts one process, and which one that is must not depend on the order classes are found in. VanillaBP reports that while the application starts (Quarkus: while it is built), naming both classes and both processes.

#### 2. Reuse - a call-activity is used to reuse a section of a process by other processes, too

In this situation the call-activity's process is used in different contexts of different parent-processes. Therefore, also a separate workflow-aggregate has to be defined and used.

In order to support this notion the target process is not modeled as a call-activity but as a collapsed pool. Instead of a call-activity a service task is used in the BPMN.

```java
@Component
@WorkflowService(workflowAggregateClass = Payment.class)
public class ChargeAccount {
  ...
}
```

> **Screenshot to be added:** `readme/call-activity.png` - a section of a process in the Camunda
> Modeler showing the reuse variant: a service task "Charge account" next to a collapsed pool
> "Charge account", with a message flow between them, so the reused process is a participant of its
> own rather than a call activity.

*Screenshot of [Camunda Modeler](https://camunda.com/en/download/modeler/)*

### Multi-instance

> **Screenshot to be added:** `readme/multi-instance.png` - the multi-instance service task "Request
>
>> partner offer" of the process `loan_approval` in the blueprint
>> [`bpmn-multi-instance-task`](https://github.com/vanillabp-blueprints/bpmn-multi-instance-task-springboot),
>> its property panel showing the parallel multi-instance loop characteristics with the collection
>> `${partnerIds}` and the element variable `partnerId`.

*Screenshot of [Camunda Modeler](https://camunda.com/en/download/modeler/)*

For multi-instance executions typically a lot of process variables are created automatically:

1. The current element of the collection
2. The number of index of the current element
3. The total number of elements

To keep the values a BPMS has to serialize small:

1. Iterate over a collection of ids the aggregate shares, or use `loop-cardinality` to define the number of iterations.
2. Don't hand the BPMS collections of complex objects.
3. In case of dynamically changing collections use attribute `completionCondition`.
4. Fetch what an iteration works on from the aggregate, based on the element or the index handed to the method.

Especially the last item is important: If you ask the process engine to handle the collection to retrieve the current element it might be that this is not done in the most efficient way, since the process engine does not know about the details of the underlying data (typically the workflow-aggregate). Therefore, it is better to fetch the element as part of the method the iteration is used for.

#### Tasks

In case of multi-instance the current element (for collection-based tasks), the current iteration's number and the total number of elements can be passed to any method called.

To announce which values you are interested in, add further method-parameters annotated by one of these annotations:
* `@MultiInstanceElement` to pass the current element
* `@MultiInstanceIndex` to pass the current iterations index, counted from zero
* `@MultiInstanceTotal` to pass the total number of iterations

Each of them names the BPMN id of the multi-instance element it asks about, because a method may sit inside more than one iteration at once:

```java
private static final String REQUEST_PARTNER_OFFER = "ServiceTask_RequestPartnerOffer";

@WorkflowTask
public void requestPartnerOffer(LoanApproval loanApproval,
    @MultiInstanceElement(REQUEST_PARTNER_OFFER) String partnerId,
    @MultiInstanceIndex(REQUEST_PARTNER_OFFER) int index,
    @MultiInstanceTotal(REQUEST_PARTNER_OFFER) int total) {
  ...
}
```

A cardinality-based task has no element to hand over, so it asks for the index and fetches what it needs itself:

```java
@WorkflowTask
public void requestPartnerOffer(LoanApproval loanApproval,
    @MultiInstanceIndex(REQUEST_PARTNER_OFFER) int index) {
  final var partnerId = loanApproval.getPartnerIds().get(index);
  ...
}
```

#### Embedded subprocesses

For multi-instance embedded-subprocesses the iteration is used at tasks within that embedded-subprocess. A task inside such a subprocess asks the subprocess about its iteration by naming the subprocess' BPMN id, and it does so whether it is multi-instance itself or not:

```java
@WorkflowTask
public void summariseRegion(LoanApproval loanApproval,
    @MultiInstanceElement("SubProcess_AssessRegion") String regionId,
    @MultiInstanceIndex("SubProcess_AssessRegion") int index) {
  ...
}
```

*Nested multi-instance activities*:

One can design processes having more than one multi-instance context active:
* Multi-instance task within a multi-instance embedded sub-process
* Multi-instance task within a multi-instance call-activity
* Task within a multi-instance call-activity within a multi-instance embedded-subprocess
* etc.

To handle these complex situations a `MultiInstanceElementResolver` bean can be specified as part of the annotation `@MultiInstanceElement`:

```java
@WorkflowTask
public void requestPartnerOffer(LoanApproval loanApproval,
    @MultiInstanceElement(resolverBean = IterationResolver.class) Iteration iteration) {
  ...
}
```

A Spring-bean implementing the resolver class is used to convert the current multi-instance execution-context into an object used by the business-method. Using this technique hides the complexity and makes it reusable for different activities within the same BPMN-context. A resolver has to implement the interface `MultiInstanceElementResolver`, naming the elements it wants to be handed and building one value out of them.

Example:

```java
@Component
public class IterationResolver
        implements MultiInstanceElementResolver<LoanApproval, Iteration> {

    static final String ASSESS_REGION = "SubProcess_AssessRegion";
    static final String REQUEST_PARTNER_OFFER = "ServiceTask_RequestPartnerOffer";

    public Collection<String> getNames() {
        return List.of(ASSESS_REGION, REQUEST_PARTNER_OFFER);
    }

    public Iteration resolve(LoanApproval loanApproval,
            Map<String, MultiInstance<Object>> multiInstances) {

        var region = multiInstances.get(ASSESS_REGION);
        var partner = multiInstances.get(REQUEST_PARTNER_OFFER);

        return new Iteration(
                (String) region.getElement(), region.getIndex(),
                (String) partner.getElement(), partner.getIndex());
    }
}
```

The map is keyed by the BPMN id of the multi-instance element and sorted from the outermost iteration to the innermost one, and each entry tells the element, the index and the total of its level. In this example the region of the multi-instance embedded subprocess and the partner of the multi-instance task inside it are combined into one value which says what the pair means. The blueprints [`bpmn-multi-instance-task`](https://github.com/vanillabp-blueprints/bpmn-multi-instance-task-springboot) and [`bpmn-multi-instance-subprocess`](https://github.com/vanillabp-blueprints/bpmn-multi-instance-subprocess-springboot) run both variants.

### User tasks and asynchronous tasks

#### Task id

A user task is a task which is fulfilled by a human. In terms of a BPMN-engine the engine stops and waits for human input (e.g. collected via a graphical user interface) to be reported via API. This procedure is the same as processing asynchronous service tasks (e.g. calling an external service which confirms after a while by calling a REST-endpoint).

In both situations one needs a reference id to be used to complete the task once the workload is done (either by a human or a service). This reference id, also called `task-id`, is handed over by using the `@TaskId` annotation:

```java
@WorkflowTask
public void requestPartnerApproval(
        final LoanApproval loanApproval,
        final @TaskId String taskId) {
    loanApproval.setPartnerApprovalTaskId(taskId);
}
```

That single parameter is what makes the task asynchronous: the method returning does **not** complete it, and the workflow stays there until the application says otherwise. The very same method without the parameter would complete the task by returning.

*Heads up:* If the task-id is used in a workflow-task's method which is not able to be processed asynchronously then `null` is passed as a value (e.g. not a user-task or the BPMN-engine does not support asynchronous tasks).

#### Task event

According to the life-cycle of asynchronous tasks there are two situations in which one might to get informed by the engine:

> **Screenshot to be added:** `readme/user-task.png` - the life cycle of an asynchronous task as a
> diagram: the BPMS creates the task and calls the workflow-method with the event `CREATED`, the
> application answers later with `completeTask`/`completeUserTask` or `cancelTask`/`cancelUserTask`,
> and a task taken away by an interrupting boundary event, by the end of the workflow or by the
> application itself calls the workflow-method again with the event `CANCELED`.

A `@TaskEvent` annotated parameter can be used to mark a method to be called in one or in both situations. Depending on whether a `@TaskEvent` annotated parameter is given and [it's value](./src/main/java/io/vanillabp/spi/service/TaskEvent.java), the workflow-method is called on each event:

* `@TaskEvent(TaskEvent.Event.CREATED)`: The workflow-method is called only when the task is created.
* `@TaskEvent(TaskEvent.Event.CANCELED)`: The workflow-method is called only when the task is canceled (e.g. due to interrupting boundary events). It can be used to unset a task-id previously stored (e.g. `loanApproval.setPartnerApprovalTaskId(null)`).
* `@TaskEvent(TaskEvent.Event.ALL)`: The workflow-method is called two times each for `CREATED` and `CANCELED`. The default behavior for the `@TaskEvent` annotation with no value given.
* No `@TaskEvent` annotation: The workflow-method is called only when the task is created.

A method interested in both writes the distinction out:

```java
@WorkflowTask
public void requestPartnerApproval(
        final LoanApproval loanApproval,
        final @TaskId String taskId,
        final @TaskEvent TaskEvent.Event event) {
    switch (event) {
        case CREATED -> partnerApprovals.request(loanApproval, taskId);
        case CANCELED -> loanApproval.setPartnerApprovalTaskId(null);
        default -> throw new IllegalStateException("Unexpected task event " + event);
    }
}
```

#### Completing asynchronous tasks

On the `CREATED` event in case of a user task for example a workflow-method could send a notification to the user. In case of an asynchronous task the external service has to be called. The given task-id has to be stored to be used once the asynchronous task completes to make the BPMN-engine know the which task is done.

Four methods of `ProcessService` end such a task, one pair per kind of task:

|       Kind of task        |            Regular answer             |            Answer taking the error path            |
|---------------------------|---------------------------------------|----------------------------------------------------|
| user task                 | `completeUserTask(aggregate, taskId)` | `cancelUserTask(aggregate, taskId, bpmnErrorCode)` |
| asynchronous service task | `completeTask(aggregate, taskId)`     | `cancelTask(aggregate, taskId, bpmnErrorCode)`     |

The two "cancel" methods do not throw the task away: they end it by raising the BPMN error code given, so the workflow leaves the task through the matching error boundary event instead of the regular sequence flow. Both pairs need a transaction of the application, because the aggregate is saved along with the answer and a remote BPMS is told only after that transaction committed. A rollback therefore leaves the task open rather than answering for work which was undone.

VanillaBP finds the BPMS holding the task itself, by asking the configured adapters in their order of priority. If none of them knows the id, a `TaskNotFoundException` explains why: the id is wrong or outdated, the task was completed long ago, or the workflow was terminated. A task which is merely completed already is a logged no-op rather than an error, so an answer arriving twice is harmless.

In this example the task is completed by using the previously received task-id:

```java
@RestController
public class PartnerApprovalController {
    @Autowired
    private ProcessService<LoanApproval> processService;
    @Autowired
    private LoanApprovalRepository loanApprovals;
    @Transactional
    @PostMapping("/api/loan-approval/{loanRequestId}/partner-approval")
    public void partnerApproved(
            @PathVariable("loanRequestId") String loanRequestId,
            @RequestBody PartnerApproval approval) {
        final var loanApproval = loanApprovals.findById(loanRequestId).orElseThrow();
        loanApproval.setPartnerApproved(approval.isApproved());
        processService.completeTask(
                loanApproval,
                loanApproval.getPartnerApprovalTaskId());
    }
}
```

The blueprints [`bpmn-user-task`](https://github.com/vanillabp-blueprints/bpmn-user-task-springboot) and [`bpmn-async-task`](https://github.com/vanillabp-blueprints/bpmn-async-task-springboot) show both pairs, including the cancel case.

### The workflow module a service belongs to

```java
String workflowModuleId = processService.getWorkflowModuleId();
```

A `ProcessService` knows which [workflow module](https://github.com/vanillabp/adapter-platform-integration/wiki/Workflow-modules) it was built for, and says so. Applications ask for it where code is shared by several modules and has to name the one it is currently working for, in a log line or as the key of something stored per module. It is also the answer to "how far does a signal reach", because that is exactly the scope of `sendSignal`.

### What the API throws

The API throws three exceptions, all of them unchecked.

* `WorkflowNotFoundException`, raised by `correlateMessage`, `aggregateChanged`,
  `getProcessDefinitions` and `getWorkflowHistory`: no configured BPMS knows a workflow for this
  aggregate. It never started, it ended long ago, or the aggregate's id is not the one the workflow
  was started with.
* `TaskNotFoundException`, raised by `completeUserTask`, `cancelUserTask`, `completeTask` and
  `cancelTask`: no configured BPMS knows the given task. The id is wrong or outdated, the task was
  removed without completion, or the workflow was terminated. A task which is merely completed
  already is a warned no-op instead.
* `ProcessDefinitionNotFoundException`, raised by `getBpmnXml`: no configured BPMS can resolve this
  process definition id. The ids are opaque and namespaced per adapter, so pass back what
  `getProcessDefinitions` reported, unchanged.

`TaskException` is not in this list on purpose. It is not raised by the API but thrown by your own task method, and it is a [modelled business outcome](#wire-up-a-task) rather than an error.

Each message names the adapters which were asked, in the order they were asked, so a failing call says which BPMS was involved without a debugger.

## Viewing BPMN and execution history of workflows

Some business applications might require viewing the BPMN and execution history of workflows.
There are open source tools available to do so (e.g. https://bpmn.io/). These tools are fed with the BPMN-XML of
a workflow and the workflow's execution history.

*VanillaBP* provides all data necessary to feed a BPMN viewer:

1. **The process definitions used by a certain workflow:**<br>

   ```java
   List<ProcessDefinition> getProcessDefinitions(
      A workflowAggregate,
      String historyContext) throws WorkflowNotFoundException;
   ```
2. **The BPMN-XML of a process definition:**<br>

   ```java
   InputStream getBpmnXml(String processDefinitionId) throws ProcessDefinitionNotFoundException;
   ```
3. **The execution history of a certain workflow:**<br>

   ```java
   WorkflowHistory getWorkflowHistory(
       A workflowAggregate,
       String historyContext) throws WorkflowNotFoundException;
   ```

These parts play together to provide a complete BPMN-XML and execution history of a certain workflow.

### Showing the BPMN of a simple workflow

Simple workflows might be defined by a single process definition. Calling the method `getProcessDefinitions`
with a workflow-aggregate and a null history-context returns a list containing exactly one process definition.
The returned [ProcessDefinition](./src/main/java/io/vanillabp/spi/process/ProcessDefinition.java)
contains meta-data about the process (the bpmn-process-id and the version of the BPMN
used to run the workflow) next to a process definition ID. The process definition ID can be used to retrieve
the BPMN-XML using the method `getBpmnXml`:

```java
@Controller
public class LoanApprovalDiagramController {
    @Autowired
    private ProcessService<LoanApproval> processService;

    @Autowired
    private LoanApprovalRepository loanApprovals;

    @GetMapping("/{loanRequestId}/bpmn")
    public ResponseEntity<InputStreamResource> getBpmnXml(
            @PathVariable final String loanRequestId) {
        var loanApproval = loanApprovals.findById(loanRequestId).orElseThrow();
        var processDefinitions = processService.getProcessDefinitions(loanApproval, null);
        var xml = processService.getBpmnXml(processDefinitions.getFirst().id());
        return ResponseEntity.ok(new InputStreamResource(xml));
    }
}
```

The process definition ID is an opaque string: VanillaBP namespaces the BPMS' own id with the adapter which can resolve it, because a workflow may run on any configured BPMS and `getBpmnXml` has no aggregate to derive one from. Pass it back unchanged.

Typically, users want to see the BPMN colored according to the current state of the workflow.
The data necessary for this is provided by the method `getWorkflowHistory`:

```java
@GetMapping("/{loanRequestId}/workflow-history")
public ResponseEntity<WorkflowHistory> getWorkflowHistory(
        @PathVariable final String loanRequestId) {
    var loanApproval = loanApprovals.findById(loanRequestId).orElseThrow();
    var workflowHistory = processService.getWorkflowHistory(loanApproval, null);
    return ResponseEntity.ok(workflowHistory);
}
```

The [WorkflowHistory](./src/main/java/io/vanillabp/spi/process/WorkflowHistory.java) primarily consists of the time
when the workflow is started, the time when the workflow ended (if) and the execution history for each element
of the workflow (see `elementsHistory`).

Items in `elementsHistory` are sorted by their execution. Each
[WorkflowElementHistory](./src/main/java/io/vanillabp/spi/process/WorkflowElementHistory.java)
contains when the element was executed (start- and end-time if already ended),
whether there is an error or the element was canceled). The element id given
can be used to find the right element in the BPMN viewer for proper coloring.
The attribute `elementType` names what kind of element it was, as a `WorkflowElementType`
(`SERVICE_TASK`, `USER_TASK`, `EXCLUSIVE_GATEWAY` and so on), and it is `UNKNOWN` for a BPMS
which does not report types in its history.

*Hint:* It depends on the BPMS used or its configuration whether the
element history is available. If not available, the list is null.
Additionally, which items are tracked is also specific to the BPMS. Some
record only activities (like service tasks) and others also include
intermediate elements (like flow nodes and gateways).

### Showing the BPMN of a complex workflow

Sometimes a BPMN process model becomes too complex to be displayed in a single process definition.
This can be done by splitting up into multiple process definitions
(each stored in a separate BPMN file) and using them in the main process by [call-activities](#call-activities).
This builds a tree structure of process definitions.

Viewing those kinds of workflows requires a bit more work. The viewer may allow digging into the tree structure
by clicking call-activities in the BPMN to show the selected sub-process BPMN model. Here one has to distinguish
two situations:

1. The call-activity was not executed so far. The user expects to see the version of BPMN of the sub-process
   which will be executed next.
2. The call-activity was already executed. The user expects to see the version of BPMN of the sub-process
   which was executed.

When executing long-running workflows, intermediate updates of the software and the BPMN model might cause
updates of sub-processes. So, a recently executed sub-process might be different from the one which will be executed
next.

The method `getProcessDefinitions` returns a list of process definitions include all sub-processes in the
version of future executions. The result's attribute `usedByElements` is null for the main process definition and
contains the ids of all elements that use this definition for call-activities.

To view a call-activity's model including coloring of elements already executed, the
[WorkflowElementHistory](./src/main/java/io/vanillabp/spi/process/WorkflowElementHistory.java)
returned by the method `getWorkflowHistory` includes the attribute `secondaryWorkflowHistoryContext`
for call-activity elements already executed. This value can be used to dig down in the tree structure of
process definitions and retrieve the next level using the method `getProcessDefinitions` passing the history-context
of the call-activity's execution.

A viewer might show the path of steps already digged down, each linked as a navigation to go back to upper processes
or the main process. The blueprint [`bpmn-history-and-diagram`](https://github.com/vanillabp-blueprints/bpmn-history-and-diagram-springboot)
serves all three methods over HTTP.

## About the SPI

VanillaBP was developed as part of the [Taxi Ride Blueprint](https://github.com/phactum/taxiride-blueprint), a demo of how to implement Java based business processing software using state-of-the-art techniques. The SPI is what stayed when the demo had made its point; the examples on this page moved on to the loan approval the [blueprints](https://github.com/vanillabp-blueprints) model.

### Prerequisites

You should know about [BPMN](https://en.wikipedia.org/wiki/Business_Process_Model_and_Notation) and you should be able to create meaningful models using a [modeler tool](https://camunda.com/en/download/modeler/).

*If you are not familiar with BPMN:* BPMN is a graphical representation for specifying business processes in XML also including semantic information. A BPMN engine runs those processes and acts as a state engine. This helps to dramatically reduce the amount of code since only "tasks" need to be implemented and the flow is handled by the engine. Hang on to see this in action.

Additionally, it helps to know about the basics of aspect orientated development. You don't have to be able to implement your own aspects but you should be aware of that annotations are used to place provided aspects in your code. Your code will be scanned for those annotations on starting the application and afterwards enriched behind the scenes to fulfill the meaning of the annotation.

### Motivation

Each BPMN engine, also called Business Processing Management System (BPMS) or workflow system, has its own APIs. Using a workflow system requires a developer to know the API and also to understand its paradigms. Typically, the API is not completely decoupled from the runtime environment which means that things like transactional behavior, synchronization of concurrent executions and similar has to be controlled by the business code but also affects the behavior of the workflow system.

So, the API *bleeds* into the business code and the business code effects the API and leads to:

- The BPMS' API is interwoven with the business code.
- All developers need to know about the APIs and their rules.
- The business code is less readable and therefore harder to maintain.
- Moving to other technology stacks/APIs (like from Camunda 7 to Camunda 8) requires to reimplement at least parts the business logic.

### Goals

To deal with the problems mentioned in section [Motivation](#motivation) we decided to introduce *VanillaBP*. This SPI incorporates best-practices collected as part of developing business-processing services since 2014 using various of those system. The provided implementation of the SPI is called adapter and hides all the details of the workflow system API. This lets the developer focus on the business aspects rather than technical details.

The SPI incorporates various state-of-the-art techniques and concepts to simplify business-process applications. We use Domain-Driven design aspects, loose-coupling, aspect-orientated programming and "convention over configuration" not only as buzz-words - as you will see in the upcoming sections - as they help to minimize the weaving between the BPMN and the business software.

### Available Adapters

An implementation of the SPI is called adapter and hides all the details of a particular workflow system's API. This lets the developer focus on the business aspects rather than technical details.

Available adapters:
* [Camunda 7 adapter](https://github.com/vanillabp/camunda7-adapter)
* [Camunda 8 adapter](https://github.com/vanillabp/camunda8-adapter)
* [Process-Engine-API adapter](https://github.com/vanillabp/process-engine-api-adapter)

Which adapter serves which BPMS, what to add as a dependency and what each BPMS can and cannot do is listed in the wiki page [BPMS adapters](https://github.com/vanillabp/adapter-platform-integration/wiki/BPMS-adapters). Several adapters may run side by side, which is what [migrating workflows from one BPMS to another](https://github.com/vanillabp/adapter-platform-integration/wiki/BPMS-migration) is built on. It is a matter of configuration and changes nothing about the code on this page, apart from the few places noted above where an API call reaches more than one BPMS.

### Concept

Imagine you have to implement the business processing software of a loan approval.

*How will the code be structured?*

The base idea is to implement a small group of service beans which are responsible to implement all requirements of a particular business process.

If external services or components are required then those service beans act as a frontend for all of them to not bind third-party dependencies close to the BPMN. This will ensure that changes in those external dependencies will be highlighted by the compiler and not only at runtime.

*How many services do you need to implement?*

In the best case only one service bean is sufficient. One workflow implemented by one service bean!

In case of more complex processes typically sections of fulfillment can be identified (retrieve a rating, collect partner offers, handle payout, etc.) which can be used as semantical buckets mapped to separate service beans.

*How are those service beans wired to the BPMN?*

In terms of BPMN there are tasks (e.g. service-task, send-task, etc.) which are wired to methods of that service by name and there are expressions (e.g. conditional-flows) which are evaluated against a process-specific [workflow-aggregate](https://martinfowler.com/bliki/DDD_Aggregate.html) (see *How is data handled?*).

All those names used to wire tasks or expressions should be in a natural language camel-case style and therefore defined by the BPMN designer (BPMN-first approach) or upfront by the developer (software-first approach). This should on one hand force the BPMN designer to name the expected data/behavior and on the other hand help developers to understand what they have to implement.

The sum of those names forms the contract between the BPMN and the underlying implementation. As an example these are typical names used as part of a loan approval workflow:

- LoanRequested (message name)
- retrieveCreditRating (service task)
- requestPartnerApproval (send task)
- ratedAcceptable (aggregate attribute read by a gateway)
- customerInformed (aggregate attribute)

*How is data handled?*

A domain aggregate is used as a persistent entity to store data either required by the process to execute (e.g. as part of expressions) or by the underlying implementation to fulfill tasks. This aggregate does not keep all the data ever needed by the workflow but stores at least references to retrieve required values on demand.

If particular attributes are required often (e.g. nearly every task) then this aggregate can be used as a *cache* of the original source of data. Depending on the use-case one might has to implement a proper update strategy.

## Decision log

Decisions several places in this repository rely on live in [`DECISIONS.md`](./DECISIONS.md), the
one thing the code is allowed to cite. A citation reads `see decision 2 in the repository's
DECISIONS.md`, numbers are never reused, and an overturned entry stays and names its successor, so
a citation written today still resolves in a year. The entries are the promises this API makes to
an application: where the state of a workflow lives, what addresses it, what the BPMS gets to see,
and what an exception out of a handler means.

## Noteworthy & Contributors

VanillaBP was developed by [Phactum](https://www.phactum.at) with the intention of giving back to the community as it has benefited the community in the past.

![Phactum](./readme/phactum.png)

Special thanks go to Martin Schimak, [plexiti GmbH](https://plexiti.com/about/), who inspired us to apply DDD to workflow.

## License

Copyright 2026 Phactum Softwareentwicklung GmbH

Licensed under the Apache License, Version 2.0
