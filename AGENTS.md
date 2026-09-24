# Working on spi-for-java

The API a business application writes against. Every change here is visible to applications, so
additive is the default and a removal needs a deprecation which names the release it goes out in.

Read [`README.md`](./README.md) first. Its anchors are linked from the blueprints and from the
wikis, so a heading is renamed only together with everything pointing at it.

## What belongs in `UPGRADE.md`

[`UPGRADE.md`](./UPGRADE.md) describes the step from VanillaBP 1 to the 2.0 release and nothing
else. An entry is owed where a version-1 application behaves differently or has to change
something, and it is written under the version line which brought it. A change between two
snapshots earns no entry, however much work it was: it would ask the reader to follow how the
release was built instead of carrying out their own upgrade, and git holds that history anyway.

What does not go there still has a place. The end state of a new part of the API belongs in
[`README.md`](./README.md) and in the wiki, which is where users read. A reasoning several places
in this repository rely on belongs in [`DECISIONS.md`](./DECISIONS.md). What is neither belongs
nowhere, and the commit message is where it is said.

These entries feed the user-facing wiki page
[Migrating from version 1](https://github.com/vanillabp/adapter-platform-integration/wiki/Migrating-from-version-1),
which wins where the two disagree.

## The decision log is binding

[`DECISIONS.md`](./DECISIONS.md) holds the decisions several places in this repository rely on. It
is the ONLY thing the code is allowed to cite, in the plain greppable form
`see decision 7 in the repository's DECISIONS.md`, and only entries of THIS repository.

Read it before you change behaviour. An entry is not background reading, it is the reason the code
around it looks the way it does, so a change which contradicts one is wrong until the entry says
otherwise.

**A decision is changed or replaced only after asking.** Where your change would make an entry
untrue, stop and put the question to the maintainer before you write the change. If the answer is
yes, the same commit updates the log: the old entry STAYS, marked as superseded and naming the
entry which replaced it, and the new decision takes the next free number. Numbers are never reused
and never renumbered, because a citation in an older release still points at them. Editing an
entry until its old text is gone is never the way.

Adding an entry has the same rule. A decision earns a number when several places rely on it and
copying the explanation to each of them would rot; anything smaller is a comment where it belongs,
and anything larger is documentation.

## Before you open a pull request

A number your branch hands out can be taken by the time you open the pull request. Another branch
was open at the same time and got there first. So check your numbers against `origin/main` and
against every open pull request, before the pull request exists.

It went wrong twice on 2026-09-13, in the Business Cockpit repository: two branches claimed one
number, which had to become 19 and 20, and two more claimed the next, which had to become 21 and
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

If your number is taken, your entry gets the next free one, and you correct every citation of it
in the code and in the documentation.

Read each citation before you change it. Not every `see decision <n>` in the branch is about your
decision. A branch can cite a number somebody else handed out long ago, and that citation stays
as it is. A search and replace over the branch turns a right reference into a wrong one.

None of this breaks the rule that a number is never renumbered. That rule is about a merged
number, which a citation in a released artifact points at. Until the pull request is merged,
nothing outside the branch has seen the number, so correcting it costs no more than the branch.

Every other running number is checked the same way. The story prompts are such a series. They are
kept outside this repository, so they are checked where they are kept.

The other script of `bin/` is `bin/check-orphaned-javadoc.sh`. It finds a javadoc block standing
directly in front of a second one, which javadoc drops without a word, so the text appears nowhere.
Run it when you wrote or moved a comment. Hang a block it reports back on the element it
describes rather than delete it.

## What code may point at

Nothing which a later change can invalidate without anything noticing: no story or prompt number,
no issue or pull-request number, no chat transcript, no person. Those record a conversation at a
point in time. A decision entry lives next to the code and is overhauled in the same commit, which
is what makes it citable.

Where a name can carry the reason, the name is the better fix. Where it cannot, a comment says why
in its own words, complete where it stands. Only what several places have to carry becomes an
entry in the log.

Commit messages and pull-request descriptions may cite whatever they like. They are records of a
point in time themselves.
