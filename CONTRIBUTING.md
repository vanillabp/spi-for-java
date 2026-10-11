# Contributing

This repository holds the API a business application writes against: annotations and interfaces,
nothing else. There is no runtime here. The
[platform integration](https://github.com/vanillabp/adapter-platform-integration), together with a
BPMS adapter, brings them to life. The
[wiki](https://github.com/vanillabp/adapter-platform-integration/wiki) describes what an application
does with them.

Where the rules are: [`README.md`](./README.md) documents the API itself, and users read it. The
blueprints and the wikis link to its headings, so rename a heading only together with everything
which points at it. [`AGENTS.md`](./AGENTS.md) says how work is done here, in the form an agent
reads. [`DECISIONS.md`](./DECISIONS.md) holds the decisions several places rely on, and it is the
only thing the code may cite.

## Getting started

Start from the [development workspace](https://github.com/vanillabp/development-workspace). Clone it
with `git clone --recurse-submodules`. This repository comes first in the build order and builds on
no other VanillaBP repository, so you can also clone it alone. The [README of the
workspace](https://github.com/vanillabp/development-workspace#readme) says how to build the
repositories in order, and how to open a pull request, with or without the right to push here. It
also shows how a coding agent can take a bug off your hands. The rest of this file is about this
repository.

## Building and testing

You need a JDK 21 or newer, and Maven, without a wrapper. The workflows build with the JDK named in
`.github/workflows`, currently 25. Build with that one if you want to see what the pipeline sees.
The class files stay at Java 21 either way, because the property `version.java` in the root
`pom.xml` compiles against Java 21:

```bash
mvn spotless:apply
mvn install
```

Run `install`, not `install verify`. `install` already runs every phase `verify` has. Naming both
walks through two lifecycles and reports every compiler warning twice. Nothing here needs Docker.

Annotations and interfaces have no behaviour of their own. So no test in this repository can fail
when a sentence of the README stops being true. The tests which hold those promises live in the
platform integration, as
[Where the promises on this page are held](./README.md#where-the-promises-on-this-page-are-held)
says. So a change of behaviour is proven over there, in the same change.

## Snapshots on Maven Central

This repository needs no snapshot. Its parent and all its dependencies are releases from Maven
Central. Every push to `main` publishes a snapshot of this repository to the snapshot repository of
Maven Central, `https://central.sonatype.com/repository/maven-snapshots/`. The platform and the
adapters read it from there. The parent `io.vanillabp:release-parent` names that repository, and
reading it needs no login and no token.

## A POM comment says what this POM does

A comment in a POM says what this POM sets, and why it differs from what it would get anyway. It
does not repeat what the parent sets. `io.vanillabp:release-parent` is released on its own
schedule. So if a child here copies its settings, the copy goes stale without anybody noticing. The
javadoc comments of three POMs in this organisation did exactly that. They said that the parent
switched the javadoc check off, after a release of the parent had switched the check on.

Sometimes a module really relies on something its parent does or does not do. Then say so as an
assumption, and name the version you checked it against. The next reader can check it again with
one command, `mvn help:effective-pom`, and does not have to believe a sentence.

## How we write

Most people who read this repository read English as a second language, and so does the
maintainer. Long sentences, rare words and stacked nouns slow them down. Write so that nobody has
to read a sentence twice.

Short main sentences, one thought each. One subordinate clause is enough. Active voice. The common
word instead of the rare one: `use` instead of `leverage`, `about` instead of `regarding`, `so`
instead of `consequently`, `has` instead of `possesses`. A technical term stays a technical term,
but say what it means the first time it turns up, and write an abbreviation out once. If a sentence
trips you up when you read it aloud, rewrite it.

This holds for every English text here, the javadoc and comments, in the main code and in the
tests, the commit message and the pull request included. Nothing a program reads is renamed for
the sake of language: type and method names, configuration keys and artifact coordinates stay as
they are, because code in other repositories points at them. The section `How we write` in
[`AGENTS.md`](./AGENTS.md) has the same rule, with examples from this repository.

## What is asked before the code is written

A change here is a change to a published contract. Applications written against version 1 keep
compiling. So by default a change only adds: a new annotation, or a new method with a default
body. A signature in `io.vanillabp.spi.*` is never changed in place. A removal needs a deprecation
which names the release it goes out in. All four BPMS adapters implement whatever is added. If your
change cannot just add, ask before you write it.

The second question is about decisions. If a change would make an entry of
[`DECISIONS.md`](./DECISIONS.md) untrue, ask as well, and wait for the answer. An entry is never
edited away. It stays, marked as superseded and naming the entry which replaced it, and the new
decision takes the next free number.

## Opening a pull request

Work on a branch of your own, and keep one subject per pull request. The description says what
changed and why it had to. It may cite an issue or a conversation, because it is a record of one
moment itself, and the code is not.

Check the numbers your branch hands out before you open it. Another branch may have taken your
decision number while you were writing. Once a pull request is merged, a `see decision 2` in a Java
file can no longer be corrected on GitHub:

```bash
bin/check-decision-numbers.sh
```

After you corrected a number, `bin/check-decision-citations.sh` says whether every citation of a
decision still points at an entry of `DECISIONS.md`. The *Checks* workflow runs it on every pull
request.

Two tools read the javadoc, and each one sees a part the other misses. The compiler checks every
class for a broken reference or broken HTML, including the package private ones. The javadoc
plugin checks what the published documentation shows. So it starts at protected and stops there.
It shows one thing below protected as well: the fields a serializable class carries into its
serialized form. That is why a private field of an exception needs a comment too.

A missing comment breaks the build. Everything this repository publishes has one now, and the
plugin fails on a warning, so that it stays that way. Write the sentence instead of switching the
check off. And write the sentence a reader needs. This is the API an application is built against,
and `@return the value` is the same gap in a longer form.

Two javadoc blocks in a row are the gap which neither tool sees. Javadoc keeps the last block in
front of an element and drops the earlier ones without a word. So a comment which somebody wrote
and kept up to date appears nowhere. `bin/check-orphaned-javadoc.sh` finds that shape. A block it
reports describes something, usually the element next to it. So hang it back there, and do not
delete it.

The *Build and publish snapshots* workflow builds and tests every pull request. It publishes
nothing from a branch. A red check is a finding about your change. Read the log and fix it. Do not
push again just to see whether it goes away.

A pull request from a fork builds and tests here like any other. It cannot publish anything,
because GitHub gives it no secrets. If this is somebody's first pull request in the repository, a
maintainer approves the first run of the workflows.

`main` has a ruleset. It requires two green checks before a merge: `publish`, which is the build of
that workflow, and `orphaned-javadoc-check`, which is the *Checks* workflow running the script
named above. While one of them is red, GitHub does not offer the merge.

## License

VanillaBP is published under the [Apache License, Version 2.0](./LICENSE). By contributing, you
agree that your contribution is licensed the same way. [`NOTICE`](./NOTICE) names who holds the
copyright.
