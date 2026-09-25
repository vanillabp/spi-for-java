# Contributing

This repository holds the API a business application writes against: annotations, interfaces and
nothing else. There is no runtime here. What brings them to life is the
[platform integration](https://github.com/vanillabp/adapter-platform-integration) together with a
BPMS adapter, and what an application does with them is described in the
[wiki](https://github.com/vanillabp/adapter-platform-integration/wiki).

Where the rules are: [`README.md`](./README.md) documents the API itself and is read by users, so
its headings are linked from the blueprints and from the wikis, and one is renamed only together
with everything pointing at it. [`AGENTS.md`](./AGENTS.md) says how work is done here, in the form an agent reads.
[`DECISIONS.md`](./DECISIONS.md) holds the decisions several places rely on, and it is the only
thing the code is allowed to cite.

## Building and testing

A JDK 21 or newer, and Maven, without a wrapper. The workflows build with the JDK named in
`.github/workflows`, currently 25, so build with that one if you want to see what the pipeline sees.
The class files stay at Java 21 either way, because that is what the property `version.java` in the
root `pom.xml` compiles against:

```bash
mvn spotless:apply
mvn install
```

`install` and not `install verify`: `install` already runs every phase `verify` has, so naming both
walks two lifecycles and reports every compiler warning twice. Nothing here needs Docker.

Annotations and interfaces have no behaviour of their own, so no test in this repository can fail
when a sentence of the README stops being true. The tests which hold those promises live in the
platform integration, which is what
[Where the promises on this page are held](./README.md#where-the-promises-on-this-page-are-held)
says. A change of behaviour is therefore proven over there, in the same change.

## A POM comment says what this POM does

A comment in a POM says what this POM sets and why it differs from what it would get for free. It
does not repeat what the parent sets. `io.vanillabp:release-parent` is released on its own schedule,
so a copy of its settings in a child here goes stale without anything noticing. The javadoc
comments of three POMs in this organisation did exactly that: they said the parent switched the
javadoc check off, after a release of it had switched the check on.

Where a module really does rely on something its parent does or does not do, say so as an
assumption and name the version it was checked against. Then the next reader can check it again in
one command, `mvn help:effective-pom`, instead of believing a sentence.

## How we write

Most people who read this repository read English as a second language, and so does the maintainer.
Long sentences, rare words and stacked nouns slow them down. Write so that nobody has to read a
sentence twice.

Short main sentences, one thought each. One subordinate clause is enough. Active voice. The common
word instead of the rare one: `use` instead of `leverage`, `about` instead of `regarding`, `so`
instead of `consequently`. A technical term stays a technical term, but say what it means the first
time it turns up, and write an abbreviation out once. If a sentence trips you up when you read it
aloud, rewrite it.

This holds for every English text here, the javadoc, the commit message and the pull request
included. Nothing a program reads is renamed for the sake of language: type and method names,
configuration keys and artifact coordinates stay as they are, because code in other repositories
points at them.

## What is asked before the code is written

A change here is a change to a published contract. Applications written against version 1 keep
compiling, so additive is the default: a new annotation, or a new method with a default body. A
signature in `io.vanillabp.spi.*` is never changed in place, a removal needs a deprecation naming
the release it goes out in, and all four BPMS adapters implement whatever is added. Where your
change cannot be additive, ask before you write it.

The second question is a decision. Where a change would make an entry of
[`DECISIONS.md`](./DECISIONS.md) untrue, ask as well, and wait for the answer. An entry is never
edited away: it stays, marked as superseded and naming its successor, and the new decision takes
the next free number.

## Opening a pull request

Work on a branch of your own and keep one subject per pull request. The description says what moved
and why it had to. It may cite an issue or a conversation, because it is a record of a moment
itself, which the code is not.

Check the numbers your branch hands out before you open it. Another branch may have taken the
decision number you used while you were writing, and once a pull request is merged a
`see decision 2` in a Java file can no longer be corrected on GitHub:

```bash
bin/check-decision-numbers.sh
```

Two tools read the javadoc, and each one sees a part the other misses. The compiler checks every
class for a broken reference or broken HTML, the package private ones included. The javadoc plugin
checks what the published documentation shows, so it starts at protected and stops there. One thing
below protected is shown as well: the fields a serializable class carries into its serialized form,
which is why a private field of an exception is asked for a comment too.

A comment which is missing breaks the build. Everything this repository publishes has one now, and
the plugin fails on a warning so that it stays that way. Write the sentence rather than switching
the check off, and write the one a reader needs: this is the API an application is built against,
and `@return the value` is the same gap in a longer form.

Two javadoc blocks in a row are the gap neither tool sees. Javadoc keeps the last block before an
element and drops the earlier ones without a word, so a comment somebody wrote and kept up to date
appears nowhere. `bin/check-orphaned-javadoc.sh` finds that shape. A block it reports describes
something, usually the element next door, so hang it back there rather than delete it.

The *Publish to GitHub Packages* workflow builds and tests every pull request and publishes nothing
from a branch. A red check is a finding about your change. Read the log and fix it rather than
pushing again to see whether it goes away.

## License

VanillaBP is published under the [Apache License, Version 2.0](./LICENSE), and by contributing you
agree that your contribution is licensed the same way. [`NOTICE`](./NOTICE) names who holds the
copyright.
