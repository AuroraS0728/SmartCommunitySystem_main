Set-Location "D:\SmartCommunitySystem_main"

$branch = "main"

git rev-parse --is-inside-work-tree *> $null
if ($LASTEXITCODE -ne 0) {
  Write-Host "Not a git repository."
  exit 1
}

git checkout $branch *> $null

git add -A
git diff --cached --quiet
if ($LASTEXITCODE -eq 0) {
  Write-Host "No changes to commit."
  exit 0
}

$msg = "auto: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
git commit -m $msg
if ($LASTEXITCODE -ne 0) {
  Write-Host "Commit failed."
  exit 1
}

git push origin $branch
if ($LASTEXITCODE -ne 0) {
  Write-Host "Push failed. Check GitHub auth/PAT and remote URL."
  exit 1
}

Write-Host "Auto push success."
