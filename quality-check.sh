#!/usr/bin/env bash
# Run the repository's Maven quality gates without changing source files.
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
modules=""
all_modules=false
dry_run=false
checks="spotbugs,spotless,license,enforcer,pmd,checkstyle"

usage() {
    cat <<'HELP'
Usage: ./quality-check.sh [--modules <selector> | --all] [options]

  -m, --modules <selector>  Maven module path or :artifactId; comma-separated.
                           Includes required reactor dependencies (-am).
  --all                    Check the full reactor (default).
  --checks <list>           Comma-separated gates (default: all six):
                           spotbugs,spotless,license,enforcer,pmd,checkstyle
  --dry-run                Print the command without invoking Maven.
  -h, --help               Show this help.

Examples:
  ./quality-check.sh
  ./quality-check.sh -m :loadup-components-scheduler-api
  ./quality-check.sh -m :loadup-components-http --checks spotbugs
  ./quality-check.sh --all --dry-run

Uses mvnd clean verify in one invocation, with optional PMD/Checkstyle goals.
Skips unit and integration tests; does not apply formatting or license fixes.
Uses plugin versions and rules from the root POM. Reports remain in module
target/ directories; console output is saved to an ignored root *.log file.
HELP
}

fail() {
    printf 'Error: %s\n' "$*" >&2
    exit 2
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        -m|--modules)
            [[ $# -ge 2 && -n "$2" && "$2" != -* ]] || fail "$1 requires a module selector"
            [[ -z "$modules" ]] || fail "Specify --modules only once; use commas for multiple modules"
            modules="$2"
            shift 2
            ;;
        --all) all_modules=true; shift ;;
        --checks)
            [[ $# -ge 2 && -n "$2" && "$2" != -* ]] || fail "--checks requires a comma-separated list"
            checks="$2"
            shift 2
            ;;
        --dry-run) dry_run=true; shift ;;
        -h|--help) usage; exit 0 ;;
        *) fail "Unknown argument: $1 (see --help)" ;;
    esac
done

if [[ "$all_modules" == true ]]; then
    [[ -z "$modules" ]] || fail "Use either --all or --modules"
elif [[ -z "$modules" ]]; then
    all_modules=true
fi
[[ "$checks" != ,* && "$checks" != *, && "$checks" != *,,* ]] || fail "Empty check name"
spotbugs=false
spotless=false
license=false
enforcer=false
pmd=false
checkstyle=false
IFS=',' read -r -a selected_checks <<< "$checks"
for check in "${selected_checks[@]}"; do
    case "$check" in
        spotbugs) spotbugs=true ;;
        spotless) spotless=true ;;
        license) license=true ;;
        enforcer) enforcer=true ;;
        pmd) pmd=true ;;
        checkstyle) checkstyle=true ;;
        *) fail "Unknown check: $check" ;;
    esac
done

command=(mvnd -B -ntp -T 1 --fail-at-end)
if [[ "$all_modules" != true ]]; then
    # Include the workspace BOM to avoid an installed stale dependency model.
    command+=(-pl ":loadup-dependencies,$modules" -am)
fi
command+=(clean verify)

if [[ "$pmd" == true || "$checkstyle" == true ]]; then
    command -v python3 >/dev/null 2>&1 || fail "python3 is required to read plugin versions from pom.xml"
    versions="$(python3 - "$project_root/pom.xml" <<'PY'
import sys
import xml.etree.ElementTree as ET
properties = ET.parse(sys.argv[1]).find('{*}properties')
keys = ('maven-pmd-plugin.version', 'maven-checkstyle-plugin.version')
values = [properties.findtext('{*}' + key) for key in keys]
if not all(values):
    sys.exit('Missing quality plugin version in root pom.xml')
print(' '.join(values))
PY
)"
    read -r pmd_version checkstyle_version <<< "$versions"
    if [[ "$pmd" == true ]]; then
        command+=("org.apache.maven.plugins:maven-pmd-plugin:$pmd_version:check")
    fi
    if [[ "$checkstyle" == true ]]; then
        command+=("org.apache.maven.plugins:maven-checkstyle-plugin:$checkstyle_version:check")
    fi
fi

command+=(
    -DskipTests=true -Dskip.unit.tests=true -Dskip.integration.tests=true
    -Dskip.javadoc=true
    "-Dskip.spotbugs=$([[ "$spotbugs" == true ]] && printf false || printf true)"
    "-Dskip.spotless=$([[ "$spotless" == true ]] && printf false || printf true)"
    "-Dlicense.skipCheckLicense=$([[ "$license" == true ]] && printf false || printf true)"
    -Dlicense.skipUpdateLicense=true
    "-Dskip.enforcer=$([[ "$enforcer" == true ]] && printf false || printf true)"
)

printf 'Working directory: %s\nCommand:' "$project_root"
printf ' %q' "${command[@]}"
printf '\n'
if [[ "$dry_run" == true ]]; then
    exit 0
fi
command -v mvnd >/dev/null 2>&1 || fail "mvnd is required; no mvn fallback is used"
cd "$project_root"
log_file="$project_root/quality-check-$(date '+%Y%m%d-%H%M%S')-$$.log"
printf 'Log: %s\n' "$log_file"
set +e
"${command[@]}" 2>&1 | tee "$log_file"
result=("${PIPESTATUS[@]}")
set -e
if [[ "${result[0]}" -ne 0 ]]; then
    printf 'Quality checks failed (Maven exit %s). See %s\n' "${result[0]}" "$log_file" >&2
    exit "${result[0]}"
fi
if [[ "${result[1]}" -ne 0 ]]; then
    printf 'Could not write the quality log: %s\n' "$log_file" >&2
    exit "${result[1]}"
fi
printf 'Quality checks passed. Reports: module target/{spotbugsXml.xml,pmd.xml,checkstyle-result.xml}\n'
