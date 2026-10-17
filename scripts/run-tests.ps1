# Runs the offline Maven test suite using the project-local repository.
#
$ErrorActionPreference = 'Stop'
$repo = Resolve-Path (Join-Path $PSScriptRoot '..')
Push-Location $repo
try {
  $localMvn = Join-Path $repo '.tools\apache-maven-3.9.9\bin\mvn.cmd'
  if (Test-Path $localMvn) {
    & $localMvn -o -B '-Dmaven.repo.local=.m2' test
    exit $LASTEXITCODE
  }

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
