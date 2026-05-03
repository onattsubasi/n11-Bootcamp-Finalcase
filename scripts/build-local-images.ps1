$ErrorActionPreference = "Stop"

$services = @(
    "discovery-service",
    "api-gateway",
    "auth-service",
    "user-service",
    "catalog-service",
    "basket-service",
    "inventory-service",
    "promotion-service",
    "search-service",
    "order-service",
    "checkout-service",
    "payment-service",
    "shipment-service",
    "notification-service",
    "review-service"
)

$root = (Get-Location).Path
$backendPom = Join-Path $root "backend\pom.xml"

if (Test-Path ".\mvnw.cmd") {
    $maven = ".\mvnw.cmd"
}
else {
    $maven = "mvn"
}

Write-Host "Using Maven command: $maven" -ForegroundColor Yellow
Write-Host "Using backend pom: $backendPom" -ForegroundColor Yellow

Write-Host ""
Write-Host "Installing backend modules into local Maven repository..." -ForegroundColor Cyan

& $maven -f $backendPom -B -ntp clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Backend install failed." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Backend modules installed successfully." -ForegroundColor Green

foreach ($service in $services) {
    $module = "services/$service"
    $image = "finalcase/${service}:local"
    $tarPath = Join-Path $root "backend\services\$service\target\jib-image.tar"

    Write-Host ""
    Write-Host "Building $service as $image..." -ForegroundColor Cyan

    & $maven -f $backendPom -B -ntp -pl $module compile com.google.cloud.tools:jib-maven-plugin:3.4.4:buildTar "-Dimage=$image"

    if ($LASTEXITCODE -ne 0) {
        Write-Host "Jib buildTar failed for $service" -ForegroundColor Red
        exit $LASTEXITCODE
    }

    if (!(Test-Path $tarPath)) {
        Write-Host "Expected image tar not found: $tarPath" -ForegroundColor Red
        exit 1
    }

    Write-Host "Loading $image into Docker..." -ForegroundColor Cyan

    docker load -i $tarPath

    if ($LASTEXITCODE -ne 0) {
        Write-Host "docker load failed for $service" -ForegroundColor Red
        exit $LASTEXITCODE
    }

    Write-Host "Loaded image: $image" -ForegroundColor Green
}

Write-Host ""
Write-Host "All service images built and loaded successfully." -ForegroundColor Green