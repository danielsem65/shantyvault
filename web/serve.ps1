$port = 3000
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://localhost:$port/")
$listener.Start()

Write-Host "ShantyVault running at http://localhost:$port" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow

while ($listener.IsListening) {
    $ctx = $listener.GetContext()
    $path = $ctx.Request.Url.AbsolutePath
    if ($path -eq "/") { $path = "/index.html" }

    $file = Join-Path $root $path
    if (Test-Path $file -PathType Leaf) {
        $ext = [System.IO.Path]::GetExtension($file)
        $mime = switch ($ext) {
            ".html" { "text/html; charset=utf-8" }
            ".js"   { "application/javascript; charset=utf-8" }
            ".css"  { "text/css; charset=utf-8" }
            ".png"  { "image/png" }
            ".svg"  { "image/svg+xml" }
            ".ico"  { "image/x-icon" }
            default { "application/octet-stream" }
        }
        $bytes = [System.IO.File]::ReadAllBytes($file)
        $ctx.Response.ContentType = $mime
        $ctx.Response.ContentLength64 = $bytes.Length
        $ctx.Response.OutputStream.Write($bytes, 0, $bytes.Length)
    } else {
        # SPA fallback: serve index.html for any unmatched path
        $file = Join-Path $root "index.html"
        if (Test-Path $file) {
            $bytes = [System.IO.File]::ReadAllBytes($file)
            $ctx.Response.ContentType = "text/html; charset=utf-8"
            $ctx.Response.ContentLength64 = $bytes.Length
            $ctx.Response.OutputStream.Write($bytes, 0, $bytes.Length)
        } else {
            $ctx.Response.StatusCode = 404
        }
    }
    $ctx.Response.Close()
}
