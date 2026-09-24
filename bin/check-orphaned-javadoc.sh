#!/usr/bin/env bash
#
# Finds a javadoc block which stands directly in front of a second one. Javadoc keeps the
# last block before an element and drops every earlier one, without a word, not even with
# failOnWarnings on. Somebody wrote the text and kept it up to date, and it appears nowhere.
#
# Never delete such a block. It describes something, usually the element next door, so hang
# it back where it belongs. Where that is not clear, ask the author rather than guess.
#
# Give it files or directories to look at. Without an argument it reads the whole
# repository. It prints one line per find and ends with 1 when it found something.

set -uo pipefail

if [ "$#" -eq 0 ]; then
    cd "$(git rev-parse --show-toplevel)" || exit 1
    set -- .
fi

mapfile -t files < <(find "$@" -name '*.java' -type f -not -path '*/target/*' | sort)

if [ "${#files[@]}" -eq 0 ]; then
    echo "No Java source below: $*"
    exit 0
fi

# The state machine reads whole lines. Every source here is formatted, so a javadoc block
# opens a line with /** and closes one with */, and a one-line /** ... */ does both. That
# is enough: a block which opens in the middle of a line of code is not documentation of
# anything, and it is not what this looks for.
awk '
    FNR == 1 { in_comment = 0; doc_ended = 0 }

    {
        line = $0
        sub(/^[ \t]*/, "", line)
        sub(/[ \t]*$/, "", line)
    }

    # inside a block comment: only its end is of interest
    in_comment {
        if (line ~ /\*\//) {
            in_comment = 0
            if (is_doc) { doc_ended = 1; doc_line = start_line }
        }
        next
    }

    # a blank line between the two blocks changes nothing, javadoc drops the first either way
    line == "" { next }

    line ~ /^\/\*/ {
        is_doc = (line ~ /^\/\*\*/)
        start_line = FNR
        if (doc_ended && is_doc) {
            printf "%s:%d: a javadoc block ends here and the next one starts in line %d, so this one is dropped\n", FILENAME, doc_line, FNR
            found = 1
        }
        doc_ended = 0
        # a one-line comment opens and closes in the same line
        if (line ~ /\*\/$/) {
            if (is_doc) { doc_ended = 1; doc_line = FNR }
        } else {
            in_comment = 1
        }
        next
    }

    # anything else is code, and code is what a javadoc block is allowed to stand in front of
    { doc_ended = 0 }

    END { exit (found ? 1 : 0) }
' "${files[@]}"
status=$?

if [ "$status" -eq 0 ]; then
    echo "No javadoc block stands in front of another one."
fi

exit $status
