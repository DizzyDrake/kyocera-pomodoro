#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
out="$(mktemp -d)"
trap 'rm -rf "$out"' EXIT
javac --release 8 -Xlint:all,-options -d "$out" \
  "$root/app/src/main/java/org/roarin/durapomodoro/SessionPlan.java" \
  "$root/app/src/main/java/org/roarin/durapomodoro/SessionTimeline.java" \
  "$root/app/src/main/java/org/roarin/durapomodoro/TimerEditPolicy.java" \
  "$root/tests/SessionPlanTest.java" \
  "$root/tests/SessionTimelineTest.java" \
  "$root/tests/TimerEditPolicyTest.java"
java -cp "$out" SessionPlanTest
java -cp "$out" SessionTimelineTest
java -cp "$out" TimerEditPolicyTest
