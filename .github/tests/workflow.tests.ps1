[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 2.0
$scriptPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\scripts\workflow.ps1'))
. $scriptPath
$fixtureParent = Join-Path $PSScriptRoot ('.workflow-fixtures-' + [guid]::NewGuid().ToString('N'))
$null = New-Item -ItemType Directory -Path $fixtureParent -ErrorAction Stop
$script:passed = 0
$script:failed = 0
$script:skipped = 0
$script:fixtureIndex = 0

function Assert-True {
    param([bool]$Condition, [string]$Message = 'Assertion failed.')
    if (-not $Condition) { throw $Message }
}

function Assert-Throws {
    param([scriptblock]$Body, [string]$MessagePattern)
    $caught = $false
    try { $null = & $Body }
    catch {
        $caught = $true
        if ($_.Exception.Message -notmatch $MessagePattern) {
            throw "Unexpected error; expected pattern '$MessagePattern': $($_.Exception.Message)"
        }
    }
    if (-not $caught) { throw "Expected an error matching '$MessagePattern'." }
}

function Write-FixtureFile {
    param([string]$Root, [string]$Path, [string]$Content)
    $full = Join-Path $Root $Path
    $null = [System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($full))
    [System.IO.File]::WriteAllText($full, $Content)
}

function New-Approval {
    param([string]$Artifact, [string]$Stage, [string]$Task)
    $record = [ordered]@{
        status = 'approved'
        approver = 'Fixture human reviewer'
        approvedAt = '2026-11-16T10:00:00+00:00'
        approvedArtifact = $Artifact
    }
    if ($Stage) { $record['approvedStage'] = $Stage }
    if ($Task) { $record['approvedTask'] = $Task }
    return [pscustomobject]$record
}

function Save-FixtureState {
    param([string]$Root, [object]$State)
    Write-FixtureFile $Root 'sessions\workflow-state.json' ($State | ConvertTo-Json -Depth 30)
}

function New-Fixture {
    $script:fixtureIndex++
    $root = Join-Path $fixtureParent ([string]$script:fixtureIndex)
    $null = [System.IO.Directory]::CreateDirectory($root)
    # This fixture declares the external contract; it does not use production manifests.
    $outputs = [ordered]@{
        requirements = 'requirements.md'
        architecture = 'architecture.md'
        'design-review' = 'design-review.md'
        'implementation-planning' = 'impl-plan.md'
        implementation = 'impl-plan.md'
        'implementation-review' = 'implementation-review.md'
        verification = 'verification.md'
        'pull-request' = 'pr.md'
    }
    $state = [pscustomobject]@{
        stateKind = 'test-fixture'
        workflow = 'Automated Documentation Sync'
        project = 'Isolated fixture'
        sessionId = 'fixture'
        triggerMode = 'manual-invocation-only'
        currentStage = 'pull-request'
        currentTask = 'ADS-PLAN-12'
        currentTaskStatus = 'completed'
        stageSequence = @($outputs.Keys)
        stageStatus = [pscustomobject]@{}
        humanApproval = [pscustomobject]@{}
        taskStatus = [pscustomobject]@{}
        taskApprovals = [pscustomobject]@{}
        artifacts = [pscustomobject]@{ actual = [pscustomobject]@{ prompts = [pscustomobject]@{} } }
        submissionAuthorization = New-Approval 'docs\automated-documentation-sync\pr.md'
    }
    foreach ($stage in $outputs.Keys) {
        $path = 'docs\automated-documentation-sync\' + $outputs[$stage]
        Write-FixtureFile $root $path "# Fixture $stage`r`nEvidence."
        $state.stageStatus | Add-Member -NotePropertyName $stage -NotePropertyValue ([pscustomobject]@{ status = 'approved'; artifact = $path })
        $state.humanApproval | Add-Member -NotePropertyName $stage -NotePropertyValue (New-Approval $path $stage)
    }
    foreach ($entry in @(
        @('ADS-PLAN-10', 'implementation-review.md'),
        @('ADS-PLAN-11', 'verification.md'),
        @('ADS-PLAN-12', 'pr.md')
    )) {
        $path = 'docs\automated-documentation-sync\' + $entry[1]
        $state.taskStatus | Add-Member -NotePropertyName $entry[0] -NotePropertyValue ([pscustomobject]@{ status = 'completed'; artifact = $path })
        $state.taskApprovals | Add-Member -NotePropertyName $entry[0] -NotePropertyValue (New-Approval -Artifact $path -Task $entry[0])
    }
    $state.taskApprovals | Add-Member -NotePropertyName 'updated-pr-draft' `
        -NotePropertyValue (New-Approval -Artifact 'docs\automated-documentation-sync\pr.md' -Task 'ADS-PLAN-12')
    $areas = @('Correctness', 'Security', 'Error Handling', 'Test Coverage', 'Code Clarity', 'DRY Principle', 'Dependency Safety')
    $headings = ($areas | ForEach-Object { "## $_`r`nReviewed fixture evidence." }) -join "`r`n"
    Write-FixtureFile $root 'docs\automated-documentation-sync\implementation-review.md' $headings
    Write-FixtureFile $root 'docs\automated-documentation-sync\pr.md' `
        ((@('Summary', 'Changes Made', 'Test Evidence', 'Known Limitations', 'Reviewer Checklist') |
            ForEach-Object { "## $_`r`nFixture." }) -join "`r`n")
    foreach ($name in @(
        '01-requirements', '02-architecture', '03-design-review', '04-implementation-planning',
        '05-implementation', '06-implementation-review', '07-verification', '08-pull-request'
    )) {
        $promptPath = ".github\prompts\$name.prompt.md"
        Write-FixtureFile $root $promptPath ('Stage contract. ' + ($areas -join ', '))
        $state.artifacts.actual.prompts | Add-Member -NotePropertyName $name -NotePropertyValue $promptPath
    }
    foreach ($name in @(
        'orchestrator', 'requirements', 'architecture', 'design-reviewer', 'implementation-planner',
        'implementation', 'documentation-sync', 'implementation-reviewer', 'verification', 'pr'
    )) { Write-FixtureFile $root ".github\agents\$name.agent.md" 'Specialist contract.' }
    foreach ($name in @('evidence-contract', 'drift-detection')) {
        Write-FixtureFile $root ".github\skills\$name\SKILL.md" 'Reusable skill contract.'
    }
    Write-FixtureFile $root '.github\copilot-instructions.md' 'Shared instructions.'
    Write-FixtureFile $root '.github\instructions\documentation-sync.instructions.md' 'Documentation instructions.'
    Write-FixtureFile $root '.github\scripts\workflow.ps1' '# Fixture implementation output.'
    Write-FixtureFile $root '.github\tests\workflow.tests.ps1' '# Fixture regression output.'
    Write-FixtureFile $root 'docs\automated-documentation-sync\CHANGELOG.md' '# Changelog'
    Write-FixtureFile $root 'docs\automated-documentation-sync\technical-reference.md' @'
Known drift: /api/todo/add/{id} is contradicted by evidence from /api/todo/add.
Verified; Unverified; Unresolved.
pom.xml; src/main/java/com/example/todoapp/domain/Priority.java;
src/main/java/com/example/todoapp/domain/Task.java;
src/main/java/com/example/todoapp/controller/TaskControllerREST.java
'@
    Write-FixtureFile $root 'pom.xml' '<project><parent><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-parent</artifactId><version>2.3.0.RELEASE</version></parent><properties><java.version>11</java.version></properties></project>'
    Write-FixtureFile $root 'src\main\java\com\example\todoapp\domain\Priority.java' 'public enum Priority { HIGH, MEDIUM, LOW }'
    Write-FixtureFile $root 'src\main\java\com\example\todoapp\domain\Task.java' 'public class Task { private Priority priority; }'
    Write-FixtureFile $root 'README.md' 'Add= /api/todo/add/{id}'
    Write-FixtureFile $root 'src\main\java\com\example\todoapp\controller\TaskControllerREST.java' `
        '@RequestMapping("/api/todo") public class Fixture { @PostMapping("/add") public void add() {} }'
    Save-FixtureState $root $state
    return [pscustomobject]@{ Root = $root; State = $state }
}

function Invoke-Case {
    param([string]$Name, [scriptblock]$Body)
    try {
        $fixture = New-Fixture
        & $Body $fixture
        $script:passed++
        Write-Output "PASS: $Name"
    }
    catch {
        $script:failed++
        Write-Output "FAIL: $Name -- $($_.Exception.Message)"
    }
}

function Get-FixtureSnapshot {
    param([string]$Root)
    return ((Get-ChildItem -LiteralPath $Root -File -Recurse -Force | Sort-Object FullName | ForEach-Object {
        $_.FullName + '|' + $_.LastWriteTimeUtc.Ticks + '|' + (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash
    }) -join "`n")
}

try {
    Invoke-Case 'all eight stages validate independently of readiness' {
        param($f)
        $result = Test-DocumentationWorkflow $f.Root
        Assert-True ($result.Valid -and $result.StageCount -eq 8 -and $result.PromptCount -eq 8 -and $result.ReviewAreaCount -eq 7)
        Assert-True ($result.KnownDrift.Count -eq 1)
    }
    Invoke-Case 'fully explicit approvals permit PR readiness' {
        param($f)
        Assert-True (Get-PrReadiness $f.Root).Ready
    }
    Invoke-Case 'missing evidence reports Not Found' {
        param($f)
        Assert-Throws { Get-WorkflowEvidence $f.Root 'absent.md' } 'Evidence Not Found'
    }
    Invoke-Case 'whitespace evidence is rejected' {
        param($f)
        Write-FixtureFile $f.Root 'empty.md' " `r`n`t"
        Assert-Throws { Get-WorkflowEvidence $f.Root 'empty.md' } 'Evidence is empty'
    }
    Invoke-Case 'directory is not file evidence' {
        param($f)
        Assert-Throws { Get-WorkflowEvidence $f.Root 'docs' } 'Evidence Not Found'
    }
    Invoke-Case 'empty repository fails clearly' {
        param($f)
        $empty = Join-Path $f.Root 'empty-repo'
        $null = [System.IO.Directory]::CreateDirectory($empty)
        Assert-Throws { Test-DocumentationWorkflow $empty } 'Evidence Not Found'
    }
    Invoke-Case 'nonexistent repository is rejected' {
        param($f)
        Assert-Throws { Test-WorkflowPath (Join-Path $f.Root 'absent') 'file.md' } 'root was not found'
    }
    Invoke-Case 'malformed state is rejected without echoing raw input' {
        param($f)
        Write-FixtureFile $f.Root 'sessions\workflow-state.json' '{"privateMarker": invalid}'
        Assert-Throws { Test-DocumentationWorkflow $f.Root } '^Malformed JSON'
    }
    Invoke-Case 'JSON null is not workflow state' {
        param($f)
        Write-FixtureFile $f.Root 'sessions\workflow-state.json' 'null'
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Expected JSON object'
    }
    Invoke-Case 'missing top-level field is rejected' {
        param($f)
        $f.State.PSObject.Properties.Remove('workflow')
        Save-FixtureState $f.Root $f.State
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Missing required field: state.workflow'
    }
    Invoke-Case 'missing nested schema field is rejected' {
        param($f)
        $f.State.stageStatus.verification.PSObject.Properties.Remove('artifact')
        Save-FixtureState $f.Root $f.State
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'stageStatus.verification.artifact'
    }
    Invoke-Case 'missing artifact manifest field is rejected' {
        param($f)
        $f.State.artifacts.actual.PSObject.Properties.Remove('prompts')
        Save-FixtureState $f.Root $f.State
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'artifacts.actual.prompts'
    }
    Invoke-Case 'recorded prompt cannot silently point to a different file' {
        param($f)
        $f.State.artifacts.actual.prompts.'08-pull-request' = 'README.md'
        Save-FixtureState $f.Root $f.State
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Wrong recorded prompt artifact'
    }
    Invoke-Case 'implementation requires executable validator output, not just a plan' {
        param($f)
        Remove-Item -LiteralPath (Join-Path $f.Root '.github\scripts\workflow.ps1')
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Evidence Not Found'
    }
    Invoke-Case 'wrong stage order fails' {
        param($f)
        $f.State.stageSequence = @('verification', 'requirements')
        Save-FixtureState $f.Root $f.State
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'eight ordered SDLC stages'
    }
    Invoke-Case 'missing stage prompt fails' {
        param($f)
        Remove-Item -LiteralPath (Join-Path $f.Root '.github\prompts\08-pull-request.prompt.md')
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Evidence Not Found'
    }
    Invoke-Case 'missing specialist fails' {
        param($f)
        Remove-Item -LiteralPath (Join-Path $f.Root '.github\agents\verification.agent.md')
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Evidence Not Found'
    }
    Invoke-Case 'missing skill fails' {
        param($f)
        Remove-Item -LiteralPath (Join-Path $f.Root '.github\skills\evidence-contract\SKILL.md')
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Evidence Not Found'
    }
    Invoke-Case 'review headings cannot be replaced by an incidental mention' {
        param($f)
        Write-FixtureFile $f.Root 'docs\automated-documentation-sync\implementation-review.md' 'Correctness Security Error Handling Test Coverage Code Clarity DRY Principle Dependency Safety'
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Missing required heading: Correctness'
    }
    Invoke-Case 'review prompt requires all seven checklist areas' {
        param($f)
        Write-FixtureFile $f.Root '.github\prompts\06-implementation-review.prompt.md' 'Correctness'
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'missing checklist area: Security'
    }
    Invoke-Case 'PR requires all five sections' {
        param($f)
        Write-FixtureFile $f.Root 'docs\automated-documentation-sync\pr.md' "## Summary`r`nOnly one section."
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Missing required heading: Changes Made'
    }
    Invoke-Case 'changelog is required' {
        param($f)
        Remove-Item -LiteralPath (Join-Path $f.Root 'docs\automated-documentation-sync\CHANGELOG.md')
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Evidence Not Found'
    }
    Invoke-Case 'known route drift must be reported in technical reference' {
        param($f)
        Write-FixtureFile $f.Root 'docs\automated-documentation-sync\technical-reference.md' 'No discrepancy.'
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'missing source citation|not explicitly documented'
    }
    Invoke-Case 'changed source cannot silently reuse the known drift exception' {
        param($f)
        Write-FixtureFile $f.Root 'src\main\java\com\example\todoapp\controller\TaskControllerREST.java' '@PostMapping("/different")'
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'cannot be established'
    }
    Invoke-Case 'current README route does not fabricate drift' {
        param($f)
        Write-FixtureFile $f.Root 'README.md' 'Add= /api/todo/add'
        Assert-True ((Test-DocumentationWorkflow $f.Root).KnownDrift.Count -eq 0)
    }
    Invoke-Case 'source smoke rejects changed Java declaration' {
        param($f)
        $path = Join-Path $f.Root 'pom.xml'
        [System.IO.File]::WriteAllText($path, [System.IO.File]::ReadAllText($path).Replace('<java.version>11', '<java.version>17'))
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'expected declared Java 11'
    }
    Invoke-Case 'source smoke rejects changed Boot parent declaration' {
        param($f)
        $path = Join-Path $f.Root 'pom.xml'
        [System.IO.File]::WriteAllText($path, [System.IO.File]::ReadAllText($path).Replace('2.3.0.RELEASE', '3.0.0'))
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'expected declared Java 11'
    }
    Invoke-Case 'source smoke rejects changed priority constants' {
        param($f)
        Write-FixtureFile $f.Root 'src\main\java\com\example\todoapp\domain\Priority.java' 'public enum Priority { HIGH, MEDIUM, LOW, URGENT }'
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Priority must declare'
    }
    Invoke-Case 'source smoke rejects absent Task priority field' {
        param($f)
        Write-FixtureFile $f.Root 'src\main\java\com\example\todoapp\domain\Task.java' 'public class Task { /* private Priority priority; */ }'
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'Task must declare'
    }
    Invoke-Case 'technical reference must state all four classifications' {
        param($f)
        $path = Join-Path $f.Root 'docs\automated-documentation-sync\technical-reference.md'
        [System.IO.File]::WriteAllText($path, [System.IO.File]::ReadAllText($path).Replace('Unresolved', 'Unknown'))
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'missing evidence classification: Unresolved'
    }
    Invoke-Case 'relative safe and slash-normalized paths are accepted' {
        param($f)
        Assert-True (Test-WorkflowPath $f.Root 'docs\automated-documentation-sync\pr.md')
        Assert-True (Test-WorkflowPath $f.Root 'docs/automated-documentation-sync/pr.md')
    }
    Invoke-Case 'traversal is rejected even when normalized target would be inside' {
        param($f)
        Assert-Throws { Test-WorkflowPath $f.Root 'docs\..\README.md' } 'traversal'
        Assert-Throws { Test-WorkflowPath $f.Root '..\README.md' } 'traversal'
    }
    Invoke-Case 'rooted paths inside and outside repository are rejected' {
        param($f)
        Assert-Throws { Test-WorkflowPath $f.Root (Join-Path $f.Root 'README.md') } 'must be relative'
        Assert-Throws { Test-WorkflowPath $f.Root '\Windows\win.ini' } 'must be relative'
    }
    Invoke-Case 'sibling-prefix path is not a child path' {
        param($f)
        Assert-Throws { Test-WorkflowPath $f.Root ($f.Root + '-sibling\README.md') } 'must be relative'
    }
    Invoke-Case 'streams wildcards and ambiguous Windows names are rejected' {
        param($f)
        foreach ($path in @('README.md:stream', '*.md', 'docs.\file.md', 'docs \file.md')) {
            Assert-Throws { Test-WorkflowPath $f.Root $path } 'streams|ambiguous'
        }
    }
    Invoke-Case 'JSON artifact traversal is rejected' {
        param($f)
        $f.State.stageStatus.verification.artifact = '..\outside.md'
        Save-FixtureState $f.Root $f.State
        Assert-Throws { Test-DocumentationWorkflow $f.Root } 'traversal'
    }
    Invoke-Case 'existing reparse escape and nonexistent child are rejected' {
        param($f)
        $outside = Join-Path $fixtureParent ('outside-' + [guid]::NewGuid().ToString('N'))
        $null = [System.IO.Directory]::CreateDirectory($outside)
        $link = Join-Path $f.Root 'escape'
        $null = New-Item -ItemType Junction -Path $link -Target $outside -ErrorAction Stop
        try {
            Write-FixtureFile $outside 'proof.md' 'Outside fixture repository.'
            Assert-Throws { Test-WorkflowPath $f.Root 'escape\proof.md' } 'reparse point'
            Assert-Throws { Test-WorkflowPath $f.Root 'escape\missing.md' } 'reparse point'
        }
        finally { [System.IO.Directory]::Delete($link) }
    }
    Invoke-Case 'missing human approval blocks progression' {
        param($f)
        $f.State.humanApproval.PSObject.Properties.Remove('implementation')
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'wrong artifact approval is rejected' {
        param($f)
        $f.State.humanApproval.verification.approvedArtifact = 'README.md'
        $result = Test-StageApproval $f.Root $f.State 'verification' 'docs\automated-documentation-sync\verification.md'
        Assert-True (-not $result.Approved -and ($result.Blockers -join ' ') -match 'different artifact')
    }
    Invoke-Case 'wrong approved stage is rejected' {
        param($f)
        $f.State.humanApproval.verification.approvedStage = 'implementation'
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'denied approval is rejected' {
        param($f)
        $f.State.humanApproval.verification.status = 'denied'
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'pending approval is rejected despite approved stage status' {
        param($f)
        $f.State.humanApproval.'implementation-review'.status = 'pending'
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'missing actual approver and timestamp are blockers' {
        param($f)
        $f.State.humanApproval.verification.approver = $null
        $f.State.humanApproval.verification.approvedAt = $null
        $result = Test-StageApproval $f.Root $f.State 'verification' 'docs\automated-documentation-sync\verification.md'
        Assert-True (-not $result.Approved -and $result.Blockers.Count -eq 2)
    }
    Invoke-Case 'invalid timestamp is not approval evidence' {
        param($f)
        $f.State.humanApproval.verification.approvedAt = 'yesterday'
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'agent identity cannot stand in for a human' {
        param($f)
        $f.State.humanApproval.verification.approver = 'Copilot'
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'matching optional SHA256 is accepted then changed evidence blocks approval' {
        param($f)
        $path = 'docs\automated-documentation-sync\verification.md'
        $hash = (Get-WorkflowEvidence $f.Root $path).SHA256
        $f.State.humanApproval.verification | Add-Member -NotePropertyName sha256 -NotePropertyValue $hash
        Assert-True (Test-StageApproval $f.Root $f.State 'verification' $path).Approved
        Write-FixtureFile $f.Root $path 'Changed after approval.'
        $result = Test-StageApproval $f.Root $f.State 'verification' $path
        Assert-True (-not $result.Approved -and ($result.Blockers -join ' ') -match 'SHA256')
    }
    Invoke-Case 'empty optional SHA256 is rejected rather than ignored' {
        param($f)
        $f.State.humanApproval.verification | Add-Member -NotePropertyName sha256 -NotePropertyValue ''
        Assert-True (-not (Test-StageApproval $f.Root $f.State 'verification' 'docs\automated-documentation-sync\verification.md').Approved)
    }
    Invoke-Case 'incomplete PR task blocks readiness' {
        param($f)
        $f.State.taskStatus.'ADS-PLAN-12'.status = 'in_progress'
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'completed task cannot name a different output artifact' {
        param($f)
        $f.State.taskStatus.'ADS-PLAN-12'.artifact = 'README.md'
        Save-FixtureState $f.Root $f.State
        $result = Get-PrReadiness $f.Root
        Assert-True (-not $result.Ready -and ($result.Blockers -join ' ') -match 'Task ADS-PLAN-12 records a different artifact')
    }
    Invoke-Case 'task approval must identify the exact task' {
        param($f)
        $f.State.taskApprovals.'ADS-PLAN-12'.approvedTask = 'ADS-PLAN-11'
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'updated draft requires its own approval when recorded' {
        param($f)
        $f.State.taskApprovals.'updated-pr-draft'.status = 'pending'
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'stage approval alone is not submission authorization' {
        param($f)
        $f.State.PSObject.Properties.Remove('submissionAuthorization')
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'bare authorization flag lacks human decision evidence' {
        param($f)
        $f.State.submissionAuthorization = 'approved'
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'active amendment pending acceptance overrides historical approvals' {
        param($f)
        $f.State | Add-Member -NotePropertyName activeRevision -NotePropertyValue ([pscustomobject]@{
            status = 'awaiting-human-review'; humanApproval = 'pending'; submissionAuthorization = 'approved'
        })
        Save-FixtureState $f.Root $f.State
        $result = Get-PrReadiness $f.Root
        Assert-True (-not $result.Ready -and ($result.Blockers -join ' ') -match 'Active revision humanApproval')
        Assert-True (Test-DocumentationWorkflow $f.Root).Valid
    }
    Invoke-Case 'null active amendment cannot bypass acceptance gates' {
        param($f)
        $f.State | Add-Member -NotePropertyName activeRevision -NotePropertyValue $null
        Save-FixtureState $f.Root $f.State
        $result = Get-PrReadiness $f.Root
        Assert-True (-not $result.Ready -and ($result.Blockers -join ' ') -match 'Active revision humanApproval')
    }
    Invoke-Case 'active amendment pending submission overrides historical authorization' {
        param($f)
        $f.State | Add-Member -NotePropertyName activeRevision -NotePropertyValue ([pscustomobject]@{
            status = 'awaiting-human-review'; humanApproval = 'approved'; submissionAuthorization = 'pending'
        })
        Save-FixtureState $f.Root $f.State
        Assert-True (-not (Get-PrReadiness $f.Root).Ready)
    }
    Invoke-Case 'explicit accepted and authorized amendment can pass all gates' {
        param($f)
        $f.State | Add-Member -NotePropertyName activeRevision -NotePropertyValue ([pscustomobject]@{
            status = 'approved'; humanApproval = 'approved'; submissionAuthorization = 'approved'
        })
        Save-FixtureState $f.Root $f.State
        Assert-True (Get-PrReadiness $f.Root).Ready
    }
    Invoke-Case 'claim classifications distinguish evidence and uncertainty' {
        param($f)
        Assert-True ((Compare-DocumentationClaim 'matching source' 'actual' 'actual').Classification -eq 'Verified')
        Assert-True ((Compare-DocumentationClaim 'different source' 'claim' 'actual').Classification -eq 'Contradicted by evidence')
        Assert-True ((Compare-DocumentationClaim 'no source' 'claim' $null).Classification -eq 'Unverified')
        Assert-True ((Compare-DocumentationClaim 'missing source' 'claim' 'actual' -EvidenceStatus Missing).Classification -eq 'Unverified')
        Assert-True ((Compare-DocumentationClaim 'ambiguous effect' 'claim' 'actual' -EvidenceStatus Unresolved).Classification -eq 'Unresolved')
    }
    Invoke-Case 'repeatable checks do not modify any repository file' {
        param($f)
        $before = Get-FixtureSnapshot $f.Root
        $one = Test-DocumentationWorkflow $f.Root | ConvertTo-Json -Depth 10
        $null = Get-PrReadiness $f.Root
        $two = Test-DocumentationWorkflow $f.Root | ConvertTo-Json -Depth 10
        $null = Get-PrReadiness $f.Root
        Assert-True ($one -ceq $two)
        Assert-True ($before -ceq (Get-FixtureSnapshot $f.Root))
    }
    Invoke-Case 'CLI Validate succeeds without writing or approving state' {
        param($f)
        $before = Get-FixtureSnapshot $f.Root
        $output = & (Join-Path $PSHOME 'powershell.exe') -NoProfile -NonInteractive -File $scriptPath -RepositoryRoot $f.Root -Action Validate 2>&1
        Assert-True ($LASTEXITCODE -eq 0) ($output -join "`n")
        Assert-True (($output -join "`n") -match 'PASS: 8 stages')
        Assert-True ($before -ceq (Get-FixtureSnapshot $f.Root))
    }
    Invoke-Case 'CLI readiness returns nonzero on pending authorization' {
        param($f)
        $f.State.submissionAuthorization.status = 'pending'
        Save-FixtureState $f.Root $f.State
        $output = & (Join-Path $PSHOME 'powershell.exe') -NoProfile -NonInteractive -File $scriptPath -RepositoryRoot $f.Root -Action CheckPrReadiness 2>&1
        Assert-True ($LASTEXITCODE -ne 0)
        Assert-True (($output -join "`n") -match 'BLOCKED: PR submission authorization')
    }
    Invoke-Case 'CLI malformed state fails without disclosing JSON contents' {
        param($f)
        Write-FixtureFile $f.Root 'sessions\workflow-state.json' '{"privateMarker": invalid}'
        $output = & (Join-Path $PSHOME 'powershell.exe') -NoProfile -NonInteractive -File $scriptPath -RepositoryRoot $f.Root 2>&1
        Assert-True ($LASTEXITCODE -ne 0)
        Assert-True (($output -join "`n") -notmatch 'privateMarker')
    }
}
finally {
    $expectedParent = [System.IO.Path]::GetFullPath($PSScriptRoot).TrimEnd('\')
    if ([System.IO.Path]::GetDirectoryName($fixtureParent) -cne $expectedParent -or
        [System.IO.Path]::GetFileName($fixtureParent) -notmatch '^\.workflow-fixtures-[a-f0-9]{32}$') {
        throw 'Refusing unsafe fixture cleanup.'
    }
    Remove-Item -LiteralPath $fixtureParent -Recurse -Force -ErrorAction Stop
}
Write-Output ("Results: {0} passed, {1} failed, {2} skipped." -f $script:passed, $script:failed, $script:skipped)
if ($script:failed -gt 0) { exit 1 }
exit 0
