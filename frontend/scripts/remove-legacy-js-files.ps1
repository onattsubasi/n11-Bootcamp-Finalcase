# Run from frontend root if you choose to merge instead of deleting src.
Get-ChildItem -Path .\src -Recurse -Include *.js,*.jsx | Remove-Item -Force
