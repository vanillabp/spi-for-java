#!/usr/bin/env bash
#
# A copy of the script of the same name in adapter-platform-integration, which every
# repository with a DECISIONS.md carries. A change to one copy belongs in all of them.
#
# Reads every citation of a decision of this repository and says whether it still points at
# something. Three things go wrong, and all three are silent:
#
#   1. A decision which has no number yet is cited by its number instead of by its file.
#      Then a search for the one spelling misses the citation, and it survives the
#      numbering. Two of them did.
#   2. A pending decision got its number, so its file under DECISIONS.pending is gone, and a
#      citation of that file points at nothing.
#   3. A citation names a number which no entry of DECISIONS.md carries.
#
# The one spelling of a decision which is still waiting for its number is its file, and in
# javadoc that path stands in a {@code} tag. A number in angle brackets is not it: javadoc
# needs the brackets escaped, the escaped form is long, the formatter wraps it over two
# lines, and that is how both of those citations walked past a search.
#
# So this reads the text and not the lines. A comment is wrapped wherever the formatter
# wants, so every line is joined with the ones after it, and a continued line loses its
# leading '*', '//' or '#', before anything is looked for.
#
# It reads the phrase the repository writes, "decision <n> in the repository's DECISIONS.md",
# and nothing else. A citation worded another way is not read at all, which is one more
# reason to keep the phrase. So it ends by saying how many citations it read: a regular
# expression which matches nothing looks exactly like a repository with nothing wrong in it.
#
# Give it files or directories to look at. Without an argument it reads the whole
# repository. It prints one line per find and ends with 1 when it found something.
#
# --self-test builds a small repository of its own, puts one citation of each kind in it and
# checks that all three are found. Two of them are wrapped over two lines, which is the
# promise worth holding: a search which reads lines finds neither.

set -uo pipefail

# CDPATH is cleared because cd prints the directory it found through CDPATH, and that
# second line would end up inside the path.
self=$(CDPATH= cd -- "$(dirname "$0")" && pwd)/$(basename "$0")

# Three citations, each wrapped the way the formatter wraps one, and one which is fine.
self_test() {
    playground=$(mktemp -d) || exit 1
    trap 'rm -rf "$playground"' EXIT
    cd "$playground" || exit 1
    git init --quiet . || exit 1

    printf '### 7. A decision which exists\n' > DECISIONS.md
    mkdir DECISIONS.pending
    printf 'A decision which is still waiting.\n' > DECISIONS.pending/42.md

    cat > Wrapped.java <<'JAVA'
/**
 * A number instead of the file, and the escaped brackets pushed over the
 * line: see decision &lt;pending:
 * 42&gt; in the repository's DECISIONS.md.
 */
class Wrapped {}
JAVA

    cat > Gone.java <<'JAVA'
/**
 * The right spelling of a decision which has its number by now, so the file is
 * gone: see {@code DECISIONS.pending/99.md} in the repository.
 */
class Gone {}
JAVA

    cat > Unknown.java <<'JAVA'
/**
 * A number no entry carries, and the phrase wrapped: see decision 8 in the
 * repository's DECISIONS.md.
 */
class Unknown {}
JAVA

    cat > Fine.java <<'JAVA'
/**
 * Both citations of this one are right: decision 7 in the repository's
 * DECISIONS.md, and {@code DECISIONS.pending/42.md} for the one still
 * waiting.
 */
class Fine {}
JAVA

    output=$("$self" . 2>&1)
    failures=0
    expect() {
        if printf '%s\n' "$output" | grep -q "$1"; then
            echo "ok: $2"
        else
            echo "FAILED: $2"
            failures=$((failures + 1))
        fi
    }
    expect '^Wrapped\.java:3:' 'the wrong spelling is found although it is wrapped'
    expect '^Gone\.java:3:' 'a citation whose pending file is gone is found'
    expect '^Unknown\.java:2:' 'a number no entry carries is found although the phrase is wrapped'
    if printf '%s\n' "$output" | grep -q '^Fine\.java:'; then
        echo "FAILED: a file whose citations are right is reported"
        failures=$((failures + 1))
    else
        echo "ok: a file whose citations are right is not reported"
    fi

    rm Wrapped.java Gone.java Unknown.java
    if "$self" . > /dev/null 2>&1; then
        echo "ok: a repository whose citations are all right passes"
    else
        echo "FAILED: a repository whose citations are all right does not pass"
        failures=$((failures + 1))
    fi

    if [ "$failures" -gt 0 ]; then
        echo
        echo "The self test found $failures thing(s) this script no longer does."
        exit 1
    fi
    echo
    echo "Self test passed."
    exit 0
}

if [ "${1:-}" = "--self-test" ]; then
    self_test
fi

cd "$(git rev-parse --show-toplevel)" || exit 1

if [ "$#" -eq 0 ]; then
    set -- .
fi

log="DECISIONS.md"

if [ ! -f "$log" ]; then
    echo "No $log in this repository, nothing to check."
    exit 0
fi

# The numbers DECISIONS.md carries. A heading is "## 7. ..." or "### 7. ...", depending on
# the repository.
numbers=$(grep -E '^#{2,3} [0-9]+\. ' "$log" | sed -E 's/^#+ ([0-9]+)\..*/\1/' | sort -n -u)

# The stories whose decision is still waiting for a number.
waiting=""
if [ -d DECISIONS.pending ]; then
    waiting=$(find DECISIONS.pending -maxdepth 1 -name '*.md' -type f \
        | sed -E 's|.*/([^/]+)\.md$|\1|' | sort -u)
fi

# A citation lives in a source, in a README, in a decision entry or in a workflow, and all
# of those are text. A picture holds none, and neither does anything below target.
#
# This script itself is left out. It carries the shapes it looks for, as the fixtures of its
# self test and in the text of its messages, so it would report itself.
files=()
while IFS= read -r candidate; do
    [ "$(readlink -f "$candidate")" = "$self" ] && continue
    grep -qI '' "$candidate" 2>/dev/null && files+=("$candidate")
done < <(find "$@" -type f \
    -not -path '*/.git/*' -not -path '*/target/*' -not -path '*/node_modules/*' \
    | sort)

if [ "${#files[@]}" -eq 0 ]; then
    echo "No text file below: $*"
    exit 0
fi

awk -v numbers="$numbers" -v waiting="$waiting" '
    BEGIN {
        split(numbers, entries, "\n")
        for (i in entries) if (entries[i] != "") known[entries[i]] = 1
        split(waiting, stories, "\n")
        for (i in stories) if (stories[i] != "") pending[stories[i]] = 1

        # A decision which has no number yet, cited by its number. The one spelling names
        # the file, so every appearance of the word next to a number is a find.
        by_number_while_pending = "pending:? ?[0-9]+"
        # The one spelling.
        by_file = "DECISIONS[.]pending/[0-9]+[.]md"
        # A citation of a numbered entry of THIS repository. Another repository keeps a
        # DECISIONS.md of its own, and its numbers are none of our business, so a citation
        # which names that repository is left alone.
        by_number = "decisions? [0-9]+( and [0-9]+)* in (the|this) repository.?s DECISIONS[.]md"
    }

    function strip(line) {
        sub(/^[ \t]*/, "", line)
        sub(/^([*]|\/\/|#|--)+[ \t]*/, "", line)
        sub(/[ \t]*$/, "", line)
        return line
    }

    function line_of(position,   k) {
        for (k = count; k >= 1; k--) if (start[k] <= position) return k
        return 1
    }

    function first_number(hit,   digits) {
        gsub(/[^0-9]+/, " ", hit)
        sub(/^ +/, "", hit)
        sub(/ .*/, "", hit)
        return hit
    }

    # Walks every match of one pattern through the joined text of the file.
    function each(pattern, kind,   rest, base, position, hit, number) {
        rest = joined
        base = 0
        while (match(rest, pattern)) {
            hit = substr(rest, RSTART, RLENGTH)
            position = base + RSTART
            base = base + RSTART + RLENGTH - 1
            rest = substr(rest, RSTART + RLENGTH)
            number = first_number(hit)

            seen_kind[kind]++
            if (kind == "spelling") {
                printf "%s:%d: a decision waiting for its number is cited as \"%s\";" \
                    " the one spelling is the file, DECISIONS.pending/%s.md\n", \
                    name, line_of(position), hit, number
                found = 1
            } else if (kind == "file" && !(number in pending)) {
                printf "%s:%d: \"%s\" points at no file, so this decision has a number by" \
                    " now and the citation has to name it\n", \
                    name, line_of(position), hit
                found = 1
            } else if (kind == "number" && !(number in known)) {
                printf "%s:%d: \"%s\", and DECISIONS.md carries no entry %s\n", \
                    name, line_of(position), hit, number
                found = 1
            }
        }
    }

    function scan() {
        if (count == 0) return
        joined = ""
        for (k = 1; k <= count; k++) {
            start[k] = length(joined) + 1
            joined = joined text[k] " "
        }
        each(by_number_while_pending, "spelling")
        each(by_file, "file")
        each(by_number, "number")
        count = 0
        delete text
        delete start
    }

    FNR == 1 { scan(); name = FILENAME; sub(/^[.]\//, "", name); read_files++ }

    { count = FNR; text[FNR] = strip($0) }

    END {
        scan()
        # on stderr, so that whatever reads the finds reads finds only
        printf "Read %d citation(s) of a numbered entry and %d of one waiting" \
            " for its number, in %d file(s).\n", \
            seen_kind["number"], seen_kind["file"], read_files > "/dev/stderr"
        exit (found ? 1 : 0)
    }
' "${files[@]}"
status=$?

if [ "$status" -eq 0 ]; then
    echo "Every citation of a decision points at something which exists."
    exit 0
fi

cat <<'EOF'

A citation of a decision which is still waiting for its number names its file, and nothing
else: DECISIONS.pending/<story>.md, in javadoc inside a {@code} tag. When the decision gets
its number, one search over the repository finds every citation of it.
EOF

exit 1
