# test-api.ps1 - Windows PowerShell script for Zorvyn API tests
# Usage:
#   $env:API_BASE_URL='http://localhost:8081'; .\test-api.ps1
#   .\test-api.ps1    (defaults to http://localhost:8081)

$baseUrl = $env:API_BASE_URL ? $env:API_BASE_URL : 'http://localhost:8081'
$username = $env:TEST_USER ? $env:TEST_USER : 'testuser'
$email = $env:TEST_EMAIL ? $env:TEST_EMAIL : 'testuser@example.com'
$password = $env:TEST_PASS ? $env:TEST_PASS : 'test@123'

function Invoke-Api($method, $path, $body = $null, $token = $null) {
    $uri = "$baseUrl$path"
    $headers = @{ 'Content-Type' = 'application/json' }
    if ($token) { $headers.Authorization = "Bearer $token" }

    $bodyJson = if ($body) { $body | ConvertTo-Json -Depth 5 } else { $null }

    $response = Invoke-RestMethod -Method $method -Uri $uri -Headers $headers -Body $bodyJson -ErrorAction Stop
    return $response
}

Write-Host "API base url: $baseUrl"

Write-Host "`n1) Signup user"
$signup = Invoke-Api -method 'POST' -path '/api/auth/signup' -body @{ username = $username; email = $email; password = $password }
Write-Host "Signup response: $(ConvertTo-Json $signup -Depth 5)"

Write-Host "`n2) Login user"
$login = Invoke-Api -method 'POST' -path '/api/auth/login' -body @{ username = $username; password = $password }
Write-Host "Login response: $(ConvertTo-Json $login -Depth 5)"
$token = $login.token
if (-not $token) { throw 'Login token missing' }

Write-Host "`n3) Get all records"
$records = Invoke-Api -method 'GET' -path '/api/records' -token $token
Write-Host "Records: $(ConvertTo-Json $records -Depth 5)"

Write-Host "`n4) Create record"
$created = Invoke-Api -method 'POST' -path '/api/records' -token $token -body @{ type='INCOME'; category='Salary'; amount=5000; description='Test record'; date=(Get-Date -Format yyyy-MM-dd) }
Write-Host "Created: $(ConvertTo-Json $created -Depth 5)"
$recordId = $created.id
if (-not $recordId) { $recordId = $created._id }
if (-not $recordId) { throw 'Record ID missing' }

Write-Host "`n5) Get dashboard"
$dashboard = Invoke-Api -method 'GET' -path '/api/records/dashboard' -token $token
Write-Host "Dashboard: $(ConvertTo-Json $dashboard -Depth 5)"

Write-Host "`n6) Filter records (INCOME)"
$filtered = Invoke-Api -method 'GET' -path '/api/records/filter?type=INCOME' -token $token
Write-Host "Filtered: $(ConvertTo-Json $filtered -Depth 5)"

Write-Host "`n7) Update record"
$updated = Invoke-Api -method 'PUT' -path "/api/records/$recordId" -token $token -body @{ type='INCOME'; category='Salary Updated'; amount=6000; description='Updated'; date=(Get-Date -Format yyyy-MM-dd) }
Write-Host "Updated: $(ConvertTo-Json $updated -Depth 5)"

Write-Host "`n8) Get record by id"
$single = Invoke-Api -method 'GET' -path "/api/records/$recordId" -token $token
Write-Host "Single: $(ConvertTo-Json $single -Depth 5)"

Write-Host "`n9) Delete record"
Invoke-Api -method 'DELETE' -path "/api/records/$recordId" -token $token
Write-Host "Record $recordId deleted"

Write-Host "`n10) Confirm records list"
$final = Invoke-Api -method 'GET' -path '/api/records' -token $token
Write-Host "Final Records: $(ConvertTo-Json $final -Depth 5)"

Write-Host "`nAPI tests completed."