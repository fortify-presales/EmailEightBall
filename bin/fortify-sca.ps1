#
# Example script to perform Fortify SCA static analysis
#

# Import some supporting functions
Import-Module $PSScriptRoot\modules\FortifyFunctions.psm1

# Import local environment specific settings
$EnvSettings = $(ConvertFrom-StringData -StringData (Get-Content ".\.env" | Where-Object {-not ($_.StartsWith('#'))} | Out-String))
$AppName = $EnvSettings['SSC_APP_NAME']
$AppVersion = $EnvSettings['SSC_APP_VER_NAME']
$SSCUrl = $EnvSettings['SSC_URL']
$SSCAuthToken = $EnvSettings['SSC_AUTH_TOKEN'] # CIToken
$ScanSwitches = "-Dcom.fortify.sca.Phase0HigherOrder.Languages=javascript,typescript -Dcom.fortify.sca.EnableDOMModeling=true -Dcom.fortify.sca.follow.imports=true -Dcom.fortify.sca.exclude.unimported.node.modules=true"

# Test we have Fortify installed successfully
Test-Environment
if ([string]::IsNullOrEmpty($AppName)) { throw "Application Name has not been set" }

# Compile the application if bot already built
$DependenciesFile = Join-Path -Path (Get-Location) -ChildPath build\classpath.txt
if (-not (Test-Path -PathType Leaf -Path $DependenciesFile)) {
    Write-Host Cleaning up workspace...
    & sourceanalyzer '-Dcom.fortify.sca.ProjectRoot=.fortify' -b "$AppName" -clean
    Write-Host Re-compiling application...
    & .\gradlew.bat clean build writeClasspath -x test
}
$ClassPath = Get-Content -Path $DependenciesFile

Write-Host "Running translation..."
& sourceanalyzer '-Dcom.fortify.sca.ProjectRoot=.fortify' -b "$AppName" -verbose -jdk 1.8 -java-build-dir "build/classes" `
    -cp $ClassPath "src/main/java/**/*" "src/main/resources/**/*" "Dockerfile" "Dockerfile.win"

Write-Host "Running scan..."
& sourceanalyzer '-Dcom.fortify.sca.ProjectRoot=.fortify' -b "$AppName" -verbose -cp $ClassPath -java-build-dir "build/classes" `
	-build-project "$AppName" -build-version "$AppVersion" -build-label "SNAPSHOT" -scan -f "$($AppName).fpr"
# -filter etc\sca-filter.txt


Write-Host "Generating PDF report..."
& ReportGenerator '-Dcom.fortify.sca.ProjectRoot=.fortify' -user "Demo User" -format pdf -f "$($AppName).pdf" -source "$($AppName).fpr"

if (![string]::IsNullOrEmpty($SSCUrl)) {
    Write-Host Uploading results to SSC...
    & fortifyclient uploadFPR -file "$($AppName).fpr" -url $SSCUrl -authtoken $SSCAuthToken -application $AppName -applicationVersion $AppVersion
}

Write-Host "Done."
