# Server Notes

## SSH Access

Use the DigitalOcean private key already stored on the Mac:

```bash
ssh -i ~/Desktop/digitalocean root@165.245.232.197
```

A local SSH alias was also configured in `~/.ssh/config`:

```sshconfig
Host digitalocean-cs
    HostName 165.245.232.197
    User root
    IdentityFile ~/Desktop/digitalocean
    IdentitiesOnly yes
```

This means future connections can use:

```bash
ssh digitalocean-cs
```

## Server Project Location

On the droplet, the deployed backend project is located at:

```bash
/root/cs-api
```

The deployed project currently has a Maven/Spring-style layout:

```bash
pom.xml
src
target
```

## Backend Status Verified

The following checks were run and passed on May 1, 2026:

### Local health on the droplet

```bash
curl http://127.0.0.1:8080/api/health
```

Response:

```json
{"status":"ok","service":"cs-api"}
```

### Public HTTPS health

```bash
curl https://cs.andromedax.org/api/health
```

Response:

```json
{"status":"ok","service":"cs-api"}
```

### Running Java process

The backend is currently running as:

```bash
/usr/bin/java -jar /root/cs-api/target/cs-api-1.0.0.jar
```

## Notes

- The backend was already running when checked, so no restart was required.
- The public API route is currently reachable through Cloudflare/HTTPS.
- `ripgrep` (`rg`) is not installed on the server, so use `grep` there if needed.
