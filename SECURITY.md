# Security Policy

## Supported versions

Security fixes are applied to the latest released minor version and the current main branch.

## Reporting a vulnerability

Report vulnerabilities through GitHub Security Advisories for this repository. Do not open a
public issue containing exploit details, credentials, or customer data.

Include the affected component and version, reproduction steps, impact, and any proposed
mitigation. The maintainers aim to acknowledge reports within three business days and provide an
initial assessment within seven business days.

## Deployment requirements

JWT secrets and database credentials must be supplied externally. LoadUp deliberately rejects
missing, short, and known default JWT secrets. Generate an HMAC secret with
`openssl rand -base64 48` and store it in the deployment platform's secret manager.
