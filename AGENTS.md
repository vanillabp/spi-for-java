# Working on spi-for-java

This is the API a business application writes against. Applications see every change here. So by
default a change only adds, and a removal needs a deprecation which names the release it goes out
in.

Read [`README.md`](./README.md) first. The blueprints and the wikis link to its anchors. So rename a
heading only together with everything which points at it.

## What belongs in `UPGRADE.md`

[`UPGRADE.md`](./UPGRADE.md) describes the step from VanillaBP 1 to the 2.0 release, and nothing
else. An entry is needed where a version-1 application behaves differently, or has to change
something. Write it under the version line which brought the change. A change between two
snapshots gets no entry, however much work it was. The entry would ask the reader to follow how the
release was built, instead of doing their own upgrade. And git holds that history anyway.

What does not go there still has a place. The end state of a new part of the API belongs in
[`README.md`](./README.md) and in the wiki, because that is where users read. Reasons which several
places in this repository rely on belong in [`DECISIONS.md`](./DECISIONS.md). What is neither
belongs nowhere. Say it in the commit message.

These entries feed the wiki page for users,
[Migrating from version 1](https://github.com/vanillabp/adapter-platform-integration/wiki/Migrating-from-version-1).
Where the two disagree, the wiki page wins.

## The decision log is binding

[`DECISIONS.md`](./DECISIONS.md) holds the decisions several places in this repository rely on. It
is the ONLY thing the code may cite, in the plain form
`see decision 7 in the repository's DECISIONS.md`, which grep can find. And it may cite only
entries of THIS repository.

Read it before you change behaviour. An entry is not background reading. It is the reason why the
code around it looks the way it does. So a change which contradicts an entry is wrong, until the
entry says otherwise.

**A decision is changed or replaced only after asking.** If your change would make an entry untrue,
stop. Ask the maintainer before you write the change. If the answer is yes, the same commit updates
the log. The old entry STAYS. It is marked as superseded and names the entry which replaced it. The
new decision takes the next free number. Numbers are never reused and never renumbered, because a
citation in an older release still points at them. Never edit an entry until its old text is gone.

Adding an entry follows the same rule. A decision gets a number when several places rely on it, and
copying the explanation to each of them would go stale over time. Anything smaller is a comment
where it belongs. Anything larger is documentation.

## How we write

Most people who read this repository read English as a second language, and so does the
maintainer. Long sentences, rare words and stacked nouns slow them down. Write so that
nobody has to read a sentence twice.

Short main sentences, one thought each. One subordinate clause is enough. Active voice.
The common word instead of the rare one: `use` instead of `leverage`, `about` instead of
`regarding`, `so` instead of `consequently`, `has` instead of `possesses`. A technical term
stays a technical term, but say what it means the first time it turns up, and write an
abbreviation out once. If a sentence trips you up when you read it aloud, rewrite it.

This holds for every English text here: the README files, `DECISIONS.md`, this file, the
Javadoc and comments which explain something, in the main code and in the tests, and the texts
of commits and pull requests. It holds for the [wiki](https://github.com/vanillabp/spi-for-java/wiki)
as well, because the wiki clone has no `AGENTS.md` of its own.

Nothing a program reads is renamed for the sake of language. Class and method names,
configuration keys, artifact coordinates and the headlines of decision log entries stay as
they are, because code, tests and other repositories point at them.

Before and after, from this repository. The first pair is from the README:

> Starting a workflow needs a transaction of yours, a task method must not have one: VanillaBP runs
> a task method in a transaction it owns and commits that transaction when a `TaskException` passes
> it, which an application transaction joining in would turn into a rollback.

becomes

> Starting a workflow needs a transaction of yours. A task method must not have one. VanillaBP runs
> a task method in a transaction of its own. It commits that transaction when a `TaskException`
> passes it. If an application transaction joined in, it would turn this into a rollback.

The second pair is from the Javadoc of `ProcessService#aggregateChanged`:

> Between them the BPMS still holds the state of the last push - which matters when a conditional
> event waits for exactly this change, or when a gateway is evaluated before the next task of yours
> runs.

becomes

> Between these points the BPMS still holds the state of the last push. That matters when a
> conditional event waits for exactly this change, or when a gateway is evaluated before your next
> task runs.

The third pair is from the Javadoc of `@TaskId`:

> This annotation is used to define a parameter which will be used to hand-over the task's id.

becomes

> Marks the parameter which receives the id of the task.

## Before you open a pull request

A number your branch hands out can be taken by the time you open the pull request. Another branch
was open at the same time and got there first. So check your numbers against `origin/main` and
against every open pull request, before the pull request exists.

It went wrong twice on 2026-09-13, in the Business Cockpit repository. Two branches claimed one
number, which had to become 19 and 20. Two more claimed the next one, which had to become 21 and
22. Both times it showed up at the merge, which is the worst moment for it. A merge happens on
GitHub, and a `see decision 21` in a Java file cannot be changed there.

The check:

```bash
bin/check-decision-numbers.sh
```

The script reports and changes nothing. By hand it is:

```bash
git fetch origin
git show origin/main:DECISIONS.md | grep -E '^### [0-9]+\. '  # the numbers already taken
gh pr list --state open
gh pr diff <n> | grep -E '^\+### [0-9]+\. '                   # for each open pull request
```

`gh pr diff` takes no path argument, so the grep does the filtering.

If your number is taken, your entry gets the next free one. Then correct every citation of it in
the code and in the documentation.

Read each citation before you change it. Not every `see decision <n>` in the branch is about your
decision. A branch can cite a number somebody else handed out long ago, and that citation stays as
it is. A search and replace over the branch turns a right reference into a wrong one.

None of this breaks the rule that a number is never renumbered. That rule is about a merged
number, which a citation in a released artifact points at. Until the pull request is merged,
nothing outside the branch has seen the number. So correcting it costs no more than the branch.

Every other running number is checked the same way. The story prompts are such a series. They are
kept outside this repository, so they are checked where they are kept.

After you renumber, run `bin/check-decision-citations.sh`. It says whether every citation of a
decision still points at an entry, also where a citation is wrapped over two lines. The *Checks*
workflow runs it on every pull request.

`bin/check-orphaned-javadoc.sh` finds a javadoc block which stands directly in front of a second
one. Javadoc drops the first block without a word, so its text appears nowhere. Run the script when
you wrote or moved a comment. If it reports a block, hang it back on the element it describes. Do
not delete it.

## What code may point at

Code may point at nothing which a later change can make invalid without anybody noticing: no story
or prompt number, no issue or pull-request number, no chat transcript, no person. Those record a
conversation at one point in time. A decision entry lives next to the code and is updated in the
same commit, and that is why code may cite it.

If a name can carry the reason, the better fix is the name. If it cannot, a comment says why, in its
own words and complete where it stands. Only what several places have to carry becomes an entry in
the log.

Commit messages and pull-request descriptions may cite whatever they like. They are records of a
point in time themselves.
