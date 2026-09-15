#!/usr/bin/env bash
#
# Says whether a decision number this branch hands out is already taken, either on
# origin/main or in an open pull request. Run it before you open a pull request.
#
# It only reports. Renumbering is a hand job, because the citations of a number are
# spread over code and documentation, and not every "see decision <n>" in a branch is
# about the branch's own decision.
#
# Needs git and the GitHub CLI (gh), both authenticated for this repository.

set -uo pipefail

cd "$(git rev-parse --show-toplevel)" || exit 1

log="DECISIONS.md"

# A heading of an entry is "## 7. ..." or "### 7. ...", depending on the repository.
headings() {
    grep -E '^#{2,3} [0-9]+\. ' || true
}

number_of() {
    # "### 21. An end overwrites ..." -> "21"
    sed -E 's/^#+ ([0-9]+)\..*/\1/'
}

text_of() {
    # "### 21. An end overwrites ..." -> "An end overwrites ..."
    sed -E 's/^#+ [0-9]+\. *//'
}

heading_with_number() {
    # $1 = number, stdin = headings
    grep -E "^#{2,3} $1\. " | head -1
}

# One heading is the edited form of the other when the shorter one starts the longer
# one. An entry which was superseded gets a note appended to its heading, and that is
# an edit, not a second decision under the same number.
same_decision() {
    case "$2" in "$1"*) return 0 ;; esac
    case "$1" in "$2"*) return 0 ;; esac
    return 1
}

if [ ! -f "$log" ]; then
    echo "No $log in this repository, nothing to check."
    exit 0
fi

echo "Fetching origin ..."
git fetch origin --quiet || { echo "git fetch origin failed."; exit 1; }

mine=$(headings < "$log")
main=$(git show origin/main:"$log" | headings)

collisions=0
new_numbers=""

while read -r line; do
    [ -n "$line" ] || continue
    number=$(printf '%s' "$line" | number_of)
    text=$(printf '%s' "$line" | text_of)
    on_main=$(printf '%s\n' "$main" | heading_with_number "$number")
    if [ -z "$on_main" ]; then
        new_numbers="$new_numbers $number"
        continue
    fi
    main_text=$(printf '%s' "$on_main" | text_of)
    if ! same_decision "$text" "$main_text"; then
        echo "COLLISION: decision $number is taken on origin/main."
        echo "  origin/main: $main_text"
        echo "  this branch: $text"
        collisions=$((collisions + 1))
    fi
done <<< "$mine"

if [ -z "$new_numbers" ]; then
    echo "This branch hands out no new decision number."
else
    echo "This branch hands out decision number(s):$new_numbers"
fi

# The open pull requests are the other half. A number free on origin/main can be
# waiting in a pull request which merges first.
if [ -n "$new_numbers" ]; then
    branch=$(git rev-parse --abbrev-ref HEAD)
    prs=$(gh pr list --state open --limit 100 --json number,headRefName \
        --jq ".[] | select(.headRefName != \"$branch\") | .number") || {
        echo "gh pr list failed, so the open pull requests were NOT checked."
        exit 1
    }
    for pr in $prs; do
        added=$(gh pr diff "$pr" 2>/dev/null \
            | grep -E "^\+#{2,3} [0-9]+\. " | sed -E 's/^\+//' ) || true
        [ -n "$added" ] || continue
        for number in $new_numbers; do
            hit=$(printf '%s\n' "$added" | heading_with_number "$number")
            [ -n "$hit" ] || continue
            their_text=$(printf '%s' "$hit" | text_of)
            my_text=$(printf '%s\n' "$mine" | heading_with_number "$number" | text_of)
            if same_decision "$my_text" "$their_text"; then
                # The same entry under the same number, which is what a branch built on
                # that pull request's branch looks like. Worth a look, not a collision.
                echo "NOTE: pull request #$pr carries decision $number with the same heading."
                echo "  Fine if this branch is built on #$pr. Otherwise it is a collision."
                continue
            fi
            echo "COLLISION: decision $number is also handed out by pull request #$pr."
            echo "  #$pr:        $their_text"
            echo "  this branch: $my_text"
            collisions=$((collisions + 1))
        done
    done
fi

highest=$(printf '%s\n' "$main" | number_of | sort -n | tail -1)
echo "Highest number on origin/main: ${highest:-none}"

if [ "$collisions" -gt 0 ]; then
    cat <<'EOF'

Give your own decision the next free number, counting origin/main and every open pull
request. Then fix the citations, and read each one before you change it: a "see decision
<n>" in this branch can point at somebody else's decision, and that one stays as it is.
EOF
    exit 1
fi

echo "No collision."
