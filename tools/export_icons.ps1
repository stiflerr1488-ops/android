# Script to export PNG icons from SVG using Inkscape
param (
    [string]$SvgFile = ".\app\src\main\assets\icon_source.svg",
    [string]$OutputDir = ".\app\src\main\res"
)

# Check if Inkscape is available
if (!(Get-Command "inkscape" -ErrorAction SilentlyContinue)) {
    Write-Host "Inkscape is not installed or not in PATH." -ForegroundColor Red
    Write-Host "Please install Inkscape from https://inkscape.org/ and ensure it's in your system PATH." -ForegroundColor Yellow
    Write-Host "Currently, Android adaptive icons (VectorDrawable) are configured, so legacy devices might see default Android icons." -ForegroundColor Yellow
    exit 1
}

$densities = @(
    @{ name = "mdpi"; size = 48 },
    @{ name = "hdpi"; size = 72 },
    @{ name = "xhdpi"; size = 96 },
    @{ name = "xxhdpi"; size = 144 },
    @{ name = "xxxhdpi"; size = 192 }
)

Write-Host "Exporting PNG icons from $SvgFile..." -ForegroundColor Green

foreach ($density in $densities) {
    $size = $density.size
    $folderName = "mipmap-$($density.name)"
    $targetDir = Join-Path $OutputDir $folderName
    
    if (!(Test-Path $targetDir)) {
        New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
    }

    $outFile = Join-Path $targetDir "ic_launcher.png"
    $outFileRound = Join-Path $targetDir "ic_launcher_round.png"

    Write-Host "Generating $($density.name) ($size x $size)..."
    inkscape --export-type=png --export-filename="$outFile" -w $size -h $size "$SvgFile"
    inkscape --export-type=png --export-filename="$outFileRound" -w $size -h $size "$SvgFile"
}

# Play Store icon
$playStoreIcon = ".\app\src\main\assets\playstore-icon.png"
Write-Host "Generating Play Store Icon (512x512)..."
inkscape --export-type=png --export-filename="$playStoreIcon" -w 512 -h 512 ".\app\src\main\assets\playstore-icon.svg"

Write-Host "Done!" -ForegroundColor Green
