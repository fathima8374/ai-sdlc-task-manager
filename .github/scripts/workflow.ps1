[CmdletBinding()]
param(
    [string]$RepositoryRoot = (Join-Path $PSScriptRoot '..\..'),
    [ValidateSet('Validate', 'CheckPrReadiness')]
    [string]$Action = 'Validate'
)

function Get-WorkflowMember {
    param([AllowNull()][object]$InputObject, [Parameter(Mandatory = $true)][string]$Name)
    if ($null -eq $InputObject) { return $null }
    $property = $InputObject.PSObject.Properties[$Name]
    if ($null -ne $property) { return $property.Value }
    return $null
}

function Resolve-WorkflowPath {
    [OutputType([string])]
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [Parameter(Mandatory = $true)][string]$Path
    )
    $root = [System.IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('\', '/')
    if (-not (Test-Path -LiteralPath $root -PathType Container -ErrorAction Stop)) {
        throw [System.ArgumentException]::new('Repository root was not found or is not a directory.')
    }
    $relative = $Path.Replace('/', '\')
    if ([string]::IsNullOrWhiteSpace($relative) -or [System.IO.Path]::IsPathRooted($relative) -or
        $relative -match '[:*?\[\]]' -or $relative -match '(^|\\)\.\.(\\|$)') {
        throw [System.ArgumentException]::new('Workflow paths must be relative and cannot contain traversal, streams, or wildcards.')
    }
    foreach ($segment in $relative.Split('\')) {
        if ($segment -ne '.' -and $segment -match '[. ]$') {
            throw [System.ArgumentException]::new('Workflow path contains an ambiguous Windows path segment.')
        }
    }
    $full = [System.IO.Path]::GetFullPath((Join-Path $root $relative))
    if (-not $full.StartsWith($root + '\', [System.StringComparison]::OrdinalIgnoreCase)) {
        throw [System.ArgumentException]::new('Workflow path is outside the repository.')
    }
    # Reject reparse points conservatively, including existing parents of a missing file.
    $cursor = $full
    while (-not [string]::IsNullOrEmpty($cursor)) {
        if (Test-Path -LiteralPath $cursor -ErrorAction Stop) {
            $item = Get-Item -LiteralPath $cursor -Force -ErrorAction Stop
            if (($item.Attributes -band [System.IO.FileAttributes]::ReparsePoint) -ne 0) {
                throw [System.ArgumentException]::new('Workflow path traverses a reparse point.')
            }
        }
        $parent = [System.IO.Path]::GetDirectoryName($cursor)
        if ($parent -eq $cursor) { break }
        $cursor = $parent
    }
    return $full
}

function Test-WorkflowPath {
    [OutputType([bool])]
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [Parameter(Mandatory = $true)][string]$Path
    )
    $null = Resolve-WorkflowPath -RepositoryRoot $RepositoryRoot -Path $Path
    return $true
}

function Get-WorkflowEvidence {
    [OutputType([pscustomobject])]
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [Parameter(Mandatory = $true)][string]$Path
    )
    $full = Resolve-WorkflowPath -RepositoryRoot $RepositoryRoot -Path $Path
    if (-not (Test-Path -LiteralPath $full -PathType Leaf -ErrorAction Stop)) {
        throw [System.IO.FileNotFoundException]::new("Evidence Not Found: $Path")
    }
    $content = [System.IO.File]::ReadAllText($full)
    if ([string]::IsNullOrWhiteSpace($content)) {
        throw [System.IO.InvalidDataException]::new("Evidence is empty: $Path")
    }
    return [pscustomobject]@{
        Path = $Path.Replace('/', '\')
        Content = $content
        SHA256 = (Get-FileHash -LiteralPath $full -Algorithm SHA256 -ErrorAction Stop).Hash
    }
}

function Compare-DocumentationClaim {
    [OutputType([pscustomobject])]
    param(
        [Parameter(Mandatory = $true)][string]$Claim,
        [AllowNull()][object]$ExpectedValue,
        [AllowNull()][object]$ObservedValue,
        [ValidateSet('Available', 'Missing', 'Unresolved')][string]$EvidenceStatus = 'Available'
    )
    $classification = 'Unverified'
    if ($EvidenceStatus -eq 'Unresolved') { $classification = 'Unresolved' }
    elseif ($EvidenceStatus -eq 'Available' -and $null -ne $ExpectedValue -and $null -ne $ObservedValue) {
        if ([string]::Equals([string]$ExpectedValue, [string]$ObservedValue, [System.StringComparison]::Ordinal)) {
            $classification = 'Verified'
        }
        else { $classification = 'Contradicted by evidence' }
    }
    return [pscustomobject]@{ Claim = $Claim; Classification = $classification }
}

function Assert-WorkflowFields {
    param(
        [AllowNull()][object]$Value,
        [Parameter(Mandatory = $true)][AllowEmptyCollection()][string[]]$Fields,
        [Parameter(Mandatory = $true)][string]$Context,
        [string[]]$Nullable = @()
    )
    if ($null -eq $Value -or $Value -isnot [pscustomobject]) {
        throw [System.IO.InvalidDataException]::new("Expected JSON object: $Context")
    }
    foreach ($field in $Fields) {
        $property = $Value.PSObject.Properties[$field]
        if ($null -eq $property -or (($field -notin $Nullable) -and
            ($null -eq $property.Value -or ($property.Value -is [string] -and
                [string]::IsNullOrWhiteSpace($property.Value))))) {
            throw [System.IO.InvalidDataException]::new("Missing required field: $Context.$field")
        }
    }
}

function Get-WorkflowState {
    [OutputType([pscustomobject])]
    param([Parameter(Mandatory = $true)][string]$RepositoryRoot)
    $evidence = Get-WorkflowEvidence -RepositoryRoot $RepositoryRoot -Path 'sessions\workflow-state.json'
    try { $state = ConvertFrom-Json -InputObject $evidence.Content -ErrorAction Stop }
    catch [System.ArgumentException] {
        throw [System.IO.InvalidDataException]::new('Malformed JSON in sessions\workflow-state.json.')
    }
    Assert-WorkflowFields -Value $state -Context 'state' -Fields @(
        'stateKind', 'workflow', 'project', 'sessionId', 'triggerMode', 'currentStage',
        'currentTask', 'currentTaskStatus', 'stageSequence', 'stageStatus', 'taskStatus',
        'humanApproval', 'taskApprovals', 'artifacts'
    )
    return $state
}

function Get-WorkflowStageArtifacts {
    [OutputType([System.Collections.Specialized.OrderedDictionary])]
    param()
    return [ordered]@{
        'requirements' = 'docs\automated-documentation-sync\requirements.md'
        'architecture' = 'docs\automated-documentation-sync\architecture.md'
        'design-review' = 'docs\automated-documentation-sync\design-review.md'
        'implementation-planning' = 'docs\automated-documentation-sync\impl-plan.md'
        'implementation' = 'docs\automated-documentation-sync\impl-plan.md'
        'implementation-review' = 'docs\automated-documentation-sync\implementation-review.md'
        'verification' = 'docs\automated-documentation-sync\verification.md'
        'pull-request' = 'docs\automated-documentation-sync\pr.md'
    }
}

function Test-WorkflowApprovalRecord {
    [OutputType([pscustomobject])]
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [AllowNull()][object]$Approval,
        [Parameter(Mandatory = $true)][string]$Artifact,
        [Parameter(Mandatory = $true)][string]$Context,
        [string]$IdentityField,
        [string]$Identity
    )
    $blockers = New-Object 'System.Collections.Generic.List[string]'
    $expectedPath = Resolve-WorkflowPath -RepositoryRoot $RepositoryRoot -Path $Artifact
    $null = Get-WorkflowEvidence -RepositoryRoot $RepositoryRoot -Path $Artifact
    if ((Get-WorkflowMember $Approval 'status') -cne 'approved') {
        $blockers.Add("$Context requires explicit approved status.")
    }
    if ($IdentityField -and (Get-WorkflowMember $Approval $IdentityField) -cne $Identity) {
        $blockers.Add("$Context does not identify the exact stage/task.")
    }
    $approvedArtifact = Get-WorkflowMember $Approval 'approvedArtifact'
    if ($approvedArtifact -isnot [string] -or [string]::IsNullOrWhiteSpace($approvedArtifact)) {
        $blockers.Add("$Context has no exact approved artifact.")
    }
    else {
        $approvedPath = Resolve-WorkflowPath -RepositoryRoot $RepositoryRoot -Path $approvedArtifact
        if (-not [string]::Equals($expectedPath, $approvedPath, [System.StringComparison]::OrdinalIgnoreCase)) {
            $blockers.Add("$Context approves a different artifact.")
        }
    }
    $approver = Get-WorkflowMember $Approval 'approver'
    if ($approver -isnot [string] -or [string]::IsNullOrWhiteSpace($approver) -or
        $approver.Trim() -match '^(pending|unknown|not recorded|n/?a|none|tbd|null|agent|copilot|automation)$') {
        $blockers.Add("$Context has no recorded human approver.")
    }
    $time = [DateTimeOffset]::MinValue
    $approvedAt = Get-WorkflowMember $Approval 'approvedAt'
    if ($approvedAt -isnot [string] -or $approvedAt -notmatch '^\d{4}-\d{2}-\d{2}T.*(Z|[+-]\d{2}:\d{2})$' -or
        -not [DateTimeOffset]::TryParse([string]$approvedAt, [ref]$time)) {
        $blockers.Add("$Context has no valid recorded approval timestamp with timezone.")
    }
    $hashProperty = $null
    if ($null -ne $Approval) { $hashProperty = $Approval.PSObject.Properties['sha256'] }
    if ($null -ne $hashProperty) {
        $actualHash = (Get-WorkflowEvidence -RepositoryRoot $RepositoryRoot -Path $Artifact).SHA256
        if ([string]$hashProperty.Value -notmatch '^[a-fA-F0-9]{64}$' -or
            -not [string]::Equals([string]$hashProperty.Value, $actualHash, [System.StringComparison]::OrdinalIgnoreCase)) {
            $blockers.Add("$Context SHA256 does not match the current artifact.")
        }
    }
    return [pscustomobject]@{ Approved = ($blockers.Count -eq 0); Blockers = @($blockers.ToArray()) }
}

function Test-StageApproval {
    [OutputType([pscustomobject])]
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [Parameter(Mandatory = $true)][object]$State,
        [Parameter(Mandatory = $true)][string]$Stage,
        [Parameter(Mandatory = $true)][string]$Artifact
    )
    $record = Get-WorkflowMember (Get-WorkflowMember $State 'humanApproval') $Stage
    $result = Test-WorkflowApprovalRecord -RepositoryRoot $RepositoryRoot -Approval $record `
        -Artifact $Artifact -Context "Stage $Stage" -IdentityField 'approvedStage' -Identity $Stage
    $blockers = @($result.Blockers)
    $stageRecord = Get-WorkflowMember (Get-WorkflowMember $State 'stageStatus') $Stage
    if ((Get-WorkflowMember $stageRecord 'status') -cne 'approved') {
        $blockers += "Stage $Stage status is not approved."
    }
    $stageArtifact = Get-WorkflowMember $stageRecord 'artifact'
    if ($stageArtifact -isnot [string] -or [string]::IsNullOrWhiteSpace($stageArtifact)) {
        $blockers += "Stage $Stage has no artifact."
    }
    elseif ((Resolve-WorkflowPath $RepositoryRoot $stageArtifact) -ine (Resolve-WorkflowPath $RepositoryRoot $Artifact)) {
        $blockers += "Stage $Stage records a different artifact."
    }
    return [pscustomobject]@{ Stage = $Stage; Approved = ($blockers.Count -eq 0); Blockers = $blockers }
}

function Get-PrReadiness {
    [OutputType([pscustomobject])]
    param([Parameter(Mandatory = $true)][string]$RepositoryRoot)
    $state = Get-WorkflowState -RepositoryRoot $RepositoryRoot
    $blockers = New-Object 'System.Collections.Generic.List[string]'
    $artifacts = Get-WorkflowStageArtifacts
    foreach ($stage in @('implementation', 'implementation-review', 'verification', 'pull-request')) {
        $approval = Test-StageApproval -RepositoryRoot $RepositoryRoot -State $state -Stage $stage -Artifact $artifacts[$stage]
        foreach ($blocker in $approval.Blockers) { $blockers.Add($blocker) }
    }
    $tasks = [ordered]@{
        'ADS-PLAN-10' = $artifacts['implementation-review']
        'ADS-PLAN-11' = $artifacts['verification']
        'ADS-PLAN-12' = $artifacts['pull-request']
    }
    foreach ($task in $tasks.Keys) {
        $taskRecord = Get-WorkflowMember (Get-WorkflowMember $state 'taskStatus') $task
        if ((Get-WorkflowMember $taskRecord 'status') -cne 'completed') { $blockers.Add("Task $task is not completed.") }
        $taskArtifact = Get-WorkflowMember $taskRecord 'artifact'
        if ($taskArtifact -isnot [string] -or [string]::IsNullOrWhiteSpace($taskArtifact)) {
            $blockers.Add("Task $task has no exact artifact.")
        }
        elseif ((Resolve-WorkflowPath $RepositoryRoot $taskArtifact) -ine (Resolve-WorkflowPath $RepositoryRoot $tasks[$task])) {
            $blockers.Add("Task $task records a different artifact.")
        }
        $record = Get-WorkflowMember (Get-WorkflowMember $state 'taskApprovals') $task
        $approval = Test-WorkflowApprovalRecord -RepositoryRoot $RepositoryRoot -Approval $record `
            -Artifact $tasks[$task] -Context "Task $task" -IdentityField 'approvedTask' -Identity $task
        foreach ($blocker in $approval.Blockers) { $blockers.Add($blocker) }
    }
    $draft = Get-WorkflowMember (Get-WorkflowMember $state 'taskApprovals') 'updated-pr-draft'
    if ($null -ne $draft) {
        $approval = Test-WorkflowApprovalRecord -RepositoryRoot $RepositoryRoot -Approval $draft `
            -Artifact $artifacts['pull-request'] -Context 'Updated PR draft' -IdentityField 'approvedTask' -Identity 'ADS-PLAN-12'
        foreach ($blocker in $approval.Blockers) { $blockers.Add($blocker) }
    }
    $revision = Get-WorkflowMember $state 'activeRevision'
    if ($null -ne $state.PSObject.Properties['activeRevision']) {
        foreach ($field in @('humanApproval', 'submissionAuthorization')) {
            if ((Get-WorkflowMember $revision $field) -cne 'approved') {
                $blockers.Add("Active revision $field is not approved; historical approvals do not approve changed artifacts.")
            }
        }
    }
    $authorization = Test-WorkflowApprovalRecord -RepositoryRoot $RepositoryRoot `
        -Approval (Get-WorkflowMember $state 'submissionAuthorization') -Artifact $artifacts['pull-request'] `
        -Context 'PR submission authorization'
    foreach ($blocker in $authorization.Blockers) { $blockers.Add($blocker) }
    return [pscustomobject]@{ Ready = ($blockers.Count -eq 0); Blockers = @($blockers.ToArray()) }
}

function Assert-WorkflowHeadings {
    param([Parameter(Mandatory = $true)][string]$Content, [Parameter(Mandatory = $true)][string[]]$Headings)
    foreach ($heading in $Headings) {
        if ($Content -notmatch ('(?im)^#{2,6}\s+' + [regex]::Escape($heading) + '\s*#*\s*$')) {
            throw [System.IO.InvalidDataException]::new("Missing required heading: $heading")
        }
    }
}

function Test-DocumentationWorkflow {
    [OutputType([pscustomobject])]
    param([Parameter(Mandatory = $true)][string]$RepositoryRoot)
    $state = Get-WorkflowState -RepositoryRoot $RepositoryRoot
    $artifacts = Get-WorkflowStageArtifacts
    if ($state.stageSequence -isnot [array] -or
        ($state.stageSequence -join '|') -cne (@($artifacts.Keys) -join '|')) {
        throw [System.IO.InvalidDataException]::new('stageSequence must contain exactly the eight ordered SDLC stages.')
    }
    if ($state.currentStage -notin @($artifacts.Keys)) {
        throw [System.IO.InvalidDataException]::new('currentStage is not a known SDLC stage.')
    }
    foreach ($map in @('stageStatus', 'humanApproval', 'taskStatus', 'taskApprovals', 'artifacts')) {
        Assert-WorkflowFields -Value (Get-WorkflowMember $state $map) -Context $map -Fields @()
    }
    Assert-WorkflowFields -Value $state.artifacts -Context 'artifacts' -Fields @('actual')
    Assert-WorkflowFields -Value $state.artifacts.actual -Context 'artifacts.actual' -Fields @('prompts')
    $promptRecords = $state.artifacts.actual.prompts
    Assert-WorkflowFields -Value $promptRecords -Context 'artifacts.actual.prompts' -Fields @(
        '01-requirements', '02-architecture', '03-design-review', '04-implementation-planning',
        '05-implementation', '06-implementation-review', '07-verification', '08-pull-request'
    )
    foreach ($stage in $artifacts.Keys) {
        $stageRecord = Get-WorkflowMember $state.stageStatus $stage
        Assert-WorkflowFields -Value $stageRecord -Context "stageStatus.$stage" -Fields @('status', 'artifact')
        $null = Get-WorkflowEvidence $RepositoryRoot $stageRecord.artifact
        if ((Resolve-WorkflowPath $RepositoryRoot $stageRecord.artifact) -ine (Resolve-WorkflowPath $RepositoryRoot $artifacts[$stage])) {
            throw [System.IO.InvalidDataException]::new("Wrong output artifact for stage: $stage")
        }
        Assert-WorkflowFields -Value (Get-WorkflowMember $state.humanApproval $stage) `
            -Context "humanApproval.$stage" -Fields @('status', 'approver', 'approvedAt', 'approvedStage', 'approvedArtifact') `
            -Nullable @('approver', 'approvedAt', 'approvedStage', 'approvedArtifact')
    }
    foreach ($task in $state.taskStatus.PSObject.Properties) {
        Assert-WorkflowFields -Value $task.Value -Context "taskStatus.$($task.Name)" -Fields @('status', 'artifact')
        foreach ($path in @($task.Value.artifact)) { $null = Get-WorkflowEvidence $RepositoryRoot $path }
    }
    foreach ($task in $state.taskApprovals.PSObject.Properties) {
        Assert-WorkflowFields -Value $task.Value -Context "taskApprovals.$($task.Name)" `
            -Fields @('status', 'approver', 'approvedAt', 'approvedTask', 'approvedArtifact') `
            -Nullable @('approver', 'approvedAt', 'approvedTask', 'approvedArtifact')
    }
    $prompts = @(
        '01-requirements', '02-architecture', '03-design-review', '04-implementation-planning',
        '05-implementation', '06-implementation-review', '07-verification', '08-pull-request'
    )
    foreach ($prompt in $prompts) {
        $expectedPrompt = ".github\prompts\$prompt.prompt.md"
        $recordedPrompt = Get-WorkflowMember $promptRecords $prompt
        $null = Get-WorkflowEvidence $RepositoryRoot $recordedPrompt
        if ((Resolve-WorkflowPath $RepositoryRoot $recordedPrompt) -ine (Resolve-WorkflowPath $RepositoryRoot $expectedPrompt)) {
            throw [System.IO.InvalidDataException]::new("Wrong recorded prompt artifact: $prompt")
        }
    }
    foreach ($agent in @(
        'orchestrator', 'requirements', 'architecture', 'design-reviewer', 'implementation-planner',
        'implementation', 'documentation-sync', 'implementation-reviewer', 'verification', 'pr'
    )) { $null = Get-WorkflowEvidence $RepositoryRoot ".github\agents\$agent.agent.md" }
    foreach ($skill in @('evidence-contract', 'drift-detection')) {
        $null = Get-WorkflowEvidence $RepositoryRoot ".github\skills\$skill\SKILL.md"
    }
    foreach ($path in @(
        '.github\copilot-instructions.md', '.github\instructions\documentation-sync.instructions.md',
        '.github\scripts\workflow.ps1', '.github\tests\workflow.tests.ps1',
        'docs\automated-documentation-sync\technical-reference.md', 'docs\automated-documentation-sync\CHANGELOG.md'
    )) { $null = Get-WorkflowEvidence $RepositoryRoot $path }
    $areas = @('Correctness', 'Security', 'Error Handling', 'Test Coverage', 'Code Clarity', 'DRY Principle', 'Dependency Safety')
    $review = Get-WorkflowEvidence $RepositoryRoot $artifacts['implementation-review']
    Assert-WorkflowHeadings -Content $review.Content -Headings $areas
    $promptReview = Get-WorkflowEvidence $RepositoryRoot '.github\prompts\06-implementation-review.prompt.md'
    foreach ($area in $areas) {
        if ($promptReview.Content -notmatch [regex]::Escape($area)) {
            throw [System.IO.InvalidDataException]::new("Review prompt is missing checklist area: $area")
        }
    }
    $pr = Get-WorkflowEvidence $RepositoryRoot $artifacts['pull-request']
    Assert-WorkflowHeadings -Content $pr.Content -Headings @('Summary', 'Changes Made', 'Test Evidence', 'Known Limitations', 'Reviewer Checklist')
    $readme = Get-WorkflowEvidence $RepositoryRoot 'README.md'
    $controller = Get-WorkflowEvidence $RepositoryRoot 'src\main\java\com\example\todoapp\controller\TaskControllerREST.java'
    $pom = Get-WorkflowEvidence $RepositoryRoot 'pom.xml'
    $pomText = [regex]::Replace($pom.Content, '(?s)<!--.*?-->', '')
    if ($pomText -notmatch '<java\.version>\s*11\s*</java\.version>' -or
        $pomText -notmatch '(?s)<parent>\s*<groupId>\s*org\.springframework\.boot\s*</groupId>\s*<artifactId>\s*spring-boot-starter-parent\s*</artifactId>\s*<version>\s*2\.3\.0\.RELEASE\s*</version>') {
        throw [System.IO.InvalidDataException]::new('Source smoke check failed: expected declared Java 11 and Spring Boot parent 2.3.0.RELEASE.')
    }
    $priority = Get-WorkflowEvidence $RepositoryRoot 'src\main\java\com\example\todoapp\domain\Priority.java'
    $priorityText = [regex]::Replace($priority.Content, '(?s)/\*.*?\*/|//[^\r\n]*', '')
    if ($priorityText -notmatch 'enum\s+Priority\s*\{\s*HIGH\s*,\s*MEDIUM\s*,\s*LOW\s*;?\s*\}') {
        throw [System.IO.InvalidDataException]::new('Source smoke check failed: Priority must declare HIGH, MEDIUM, LOW.')
    }
    $task = Get-WorkflowEvidence $RepositoryRoot 'src\main\java\com\example\todoapp\domain\Task.java'
    $taskText = [regex]::Replace($task.Content, '(?s)/\*.*?\*/|//[^\r\n]*', '')
    if ($taskText -notmatch '\bPriority\s+priority\s*;') {
        throw [System.IO.InvalidDataException]::new('Source smoke check failed: Task must declare its Priority field.')
    }
    $controllerText = [regex]::Replace($controller.Content, '(?s)/\*.*?\*/|//[^\r\n]*', '')
    if ($controllerText -notmatch '@RequestMapping\s*\(\s*"/api/todo"\s*\)' -or
        $controllerText -notmatch '@PostMapping\s*\(\s*"/add"\s*\)') {
        throw [System.IO.InvalidDataException]::new('Source smoke check failed: expected REST base /api/todo and POST /add mappings; known drift cannot be established otherwise.')
    }
    $reference = Get-WorkflowEvidence $RepositoryRoot 'docs\automated-documentation-sync\technical-reference.md'
    $referenceText = $reference.Content.Replace('\', '/')
    foreach ($citation in @(
        'pom.xml', 'src/main/java/com/example/todoapp/domain/Priority.java',
        'src/main/java/com/example/todoapp/domain/Task.java',
        'src/main/java/com/example/todoapp/controller/TaskControllerREST.java'
    )) {
        if ($referenceText -notmatch [regex]::Escape($citation)) {
            throw [System.IO.InvalidDataException]::new("Technical reference is missing source citation: $citation")
        }
    }
    foreach ($classification in @('Verified', 'Contradicted by evidence', 'Unverified', 'Unresolved')) {
        if ($reference.Content -notmatch ('(?i)\b' + [regex]::Escape($classification) + '\b')) {
            throw [System.IO.InvalidDataException]::new("Technical reference is missing evidence classification: $classification")
        }
    }
    $drift = New-Object 'System.Collections.Generic.List[object]'
    if ($readme.Content -match '/api/todo/add/\{id\}') {
        if ($controller.Content -notmatch '@RequestMapping\s*\(\s*"/api/todo"\s*\)' -or
            $controller.Content -notmatch '@PostMapping\s*\(\s*"/add"\s*\)') {
            throw [System.IO.InvalidDataException]::new('Known README route drift cannot be established from current controller mappings.')
        }
        $reference = Get-WorkflowEvidence $RepositoryRoot 'docs\automated-documentation-sync\technical-reference.md'
        if ($reference.Content -notmatch '/api/todo/add/\{id\}' -or
            $reference.Content -notmatch '(?i)contradicted|drift') {
            throw [System.IO.InvalidDataException]::new('README route drift is present but is not explicitly documented.')
        }
        $drift.Add((Compare-DocumentationClaim -Claim 'README Add route' `
            -ExpectedValue '/api/todo/add/{id}' -ObservedValue '/api/todo/add'))
    }
    return [pscustomobject]@{
        Valid = $true
        StageCount = $artifacts.Count
        PromptCount = $prompts.Count
        ReviewAreaCount = $areas.Count
        SourceSmokeChecks = 'Declared Java/Boot versions, Priority enum, Task field, REST mappings, and technical-reference citations/classifications only; not comprehensive semantic proof.'
        KnownDrift = @($drift.ToArray())
    }
}

if ($MyInvocation.InvocationName -ne '.') {
    $ErrorActionPreference = 'Stop'
    try {
        if ($Action -eq 'CheckPrReadiness') {
            $result = Get-PrReadiness -RepositoryRoot $RepositoryRoot
            if (-not $result.Ready) {
                Write-Output 'FAIL: PR readiness is blocked.'
                foreach ($blocker in $result.Blockers) { Write-Output "BLOCKED: $blocker" }
                exit 1
            }
            Write-Output 'PASS: Explicit PR readiness gates are satisfied. No submission or merge was performed.'
        }
        else {
            $result = Test-DocumentationWorkflow -RepositoryRoot $RepositoryRoot
            Write-Output "PASS: $($result.StageCount) stages, $($result.PromptCount) prompts, $($result.ReviewAreaCount) review areas."
            foreach ($drift in $result.KnownDrift) { Write-Output "KNOWN DRIFT: $($drift.Claim): $($drift.Classification)" }
            Write-Output 'Structure validation does not grant human approval or PR submission authorization.'
        }
        exit 0
    }
    catch {
        Write-Output ("FAIL: " + $_.Exception.Message)
        exit 1
    }
}
