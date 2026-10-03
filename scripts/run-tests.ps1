# Runs the offline Maven test suite using the project-local repository.
#
# Usage:  powershell -ExecutionPolicy Bypass -File scripts/run-tests.ps1
$ErrorActionPreference = 'Stop'
$repo = Resolve-Path (Join-Path $PSScriptRoot '..')
Push-Location $repo
try {
  if (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn -o -Dmaven.repo.local=.m2 test
    exit $LASTEXITCODE
  }

  # Fall back to a Maven distribution downloaded by the Maven wrapper, if present.
  $wrapper = Get-ChildItem -Path (Join-Path $env:USERPROFILE '.m2\wrapper\dists') `
    -Recurse -Filter 'mvn.cmd' -ErrorAction SilentlyContinue | Select-Object -First 1
  if ($wrapper) {
    & $wrapper.FullName -o -B '-Dmaven.repo.local=.m2' test
    exit $LASTEXITCODE
  }

  throw "Maven (mvn) was not found. Install Maven or enable the VS Code Maven integration."
} finally {
  Pop-Location
}
