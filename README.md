# Trails No Authentication Adapter

This authentication adapter gives every visitor unrestricted access to Trails, including profile
and administrative operations. No login, user provisioning, token, role mapping, or external provider is needed.

```xml
<dependency>
    <groupId>io.github.yaforster</groupId>
    <artifactId>trails-auth-none</artifactId>
    <version>0.0.1</version>
</dependency>
```

The version may be omitted when importing the matching Trails BOM. There are no YAML settings for this adapter.
Select exactly one authentication adapter: combining this dependency with OIDC or another provider fails startup.

## Shared identity

Every request uses UUID `803e31d2-04dd-4c20-9ab7-9861a753f497`. It is a stable persistence key, not a credential.
All visitors share profile fields and the avatar. The persistence adapter creates the profile on first access and
retains it across restarts. Deleting its data permits the ordinary profile lifecycle to recreate it on next access.
Authorization headers, including malformed or expired tokens, do not change this identity or restrict access.

`AUTHENTICATION_REQUIRED=false` and `USER_FEATURES_ACTIVE=true` are exposed at `/api/capabilities`.

No bearer token is needed to call the Trails API methods if this adapter is used.
All Trails authorization checks allow access. Domain validation, resource existence checks, execution limits, and
persistence constraints still apply. Omitting all authentication adapters also permits access, but disables profiles.
**This adapter explicitly selects unrestricted access with a shared identity.**