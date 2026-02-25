$ErrorActionPreference = "Stop"

Write-Output "[1/7] Repo hygiene"
powershell -ExecutionPolicy Bypass -File .\tools\ci\check_repo_hygiene.ps1
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "[2/7] PendingIntent flags"
python .\tools\ci\check_pending_intent_flags.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "[3/7] Android guardrails"
python .\tools\ci\check_android_guards.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "[4/7] Deprecated APIs"
python .\tools\ci\check_deprecated_apis.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "[5/7] Release hardening"
python .\tools\ci\check_release_hardening.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "[6/7] Proguard rules"
python .\tools\ci\check_proguard_rules.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "[7/7] Secret scan"
python .\tools\ci\check_secrets.py
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Output "All guard checks passed."
