# Runs the Maven test suite with the project Maven Wrapper.
$ErrorActionPreference = 'Stop'
$repo = Resolve-Path (Join-Path $PSScriptRoot '..')
Push-Location $repo
try {
  $wrapper = Join-Path $repo 'mvnw.cmd'
  if (-not (Test-Path $wrapper)) {
    throw "Maven Wrapper was not found at '$wrapper'. Restore the complete project files and try again."
  }

  & $wrapper -B test
  if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
  }
} finally {
  Pop-Location
}
