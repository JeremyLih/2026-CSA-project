$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$SrcDir = Join-Path $RootDir "src\main\java"
$OutDir = Join-Path $RootDir "out"
$EnvFile = Join-Path $RootDir ".env"
$M2Dir = Join-Path $HOME ".m2\repository"

if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -eq "" -or $line.StartsWith("#")) { return }
        $parts = $line.Split("=", 2)
        if ($parts.Count -eq 2) {
            [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), "Process")
        }
    }
}

$GsonJar = Join-Path $M2Dir "com\google\code\gson\gson\2.11.0\gson-2.11.0.jar"
$HikariJar = Join-Path $M2Dir "com\zaxxer\HikariCP\5.1.0\HikariCP-5.1.0.jar"
$PostgresJar = Join-Path $M2Dir "org\postgresql\postgresql\42.7.4\postgresql-42.7.4.jar"
$Slf4jJar = Join-Path $M2Dir "org\slf4j\slf4j-api\2.0.13\slf4j-api-2.0.13.jar"

foreach ($jar in @($GsonJar, $HikariJar, $PostgresJar, $Slf4jJar)) {
    if (-not (Test-Path $jar)) {
        Write-Error "Missing dependency jar: $jar"
    }
}

if (Test-Path $OutDir) {
    Remove-Item -LiteralPath $OutDir -Recurse -Force
}
New-Item -ItemType Directory -Path $OutDir | Out-Null

$Classpath = @($GsonJar, $HikariJar, $PostgresJar, $Slf4jJar) -join ";"
$JavaFiles = Get-ChildItem $SrcDir -Filter "*.java" | Where-Object { $_.Name -ne "Main.java" } | ForEach-Object { $_.FullName }

javac -encoding UTF-8 -cp $Classpath -d $OutDir $JavaFiles
java -cp "$OutDir;$Classpath" Application
