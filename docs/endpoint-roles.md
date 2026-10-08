# PNC REST Endpoint Role Requirements

All endpoints under `/v2` require at least the `pnc-users` role. No public/unauthenticated access is permitted.

## Legend

- **`pnc-users`** = baseline access (all authenticated PNC users)
- Specific roles = elevated access (only users with that particular role)
- All endpoints implicitly also accept `pnc-users-admin` (admin can do everything)

## Endpoints (rest-api module — 23 interfaces, ~175 routes)

### 1. `/artifacts` (ArtifactEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/artifacts` | `pnc-users` |
| GET | `/artifacts/filter` | `pnc-users` |
| GET | `/artifacts/{id}` | `pnc-users` |
| GET | `/artifacts/purl/{purl}` | `pnc-users` |
| POST | `/artifacts` | `pnc-app-artifact-user`, `pnc-users-admin` |
| PUT | `/artifacts/{id}` | `pnc-app-artifact-user`, `pnc-users-admin` |
| POST | `/artifacts/{id}/artifacts/quality` | `pnc-users` (+ programmatic checks inside for elevated qualities) |
| GET | `/artifacts/{id}/dependant-builds` | `pnc-users` |
| GET | `/artifacts/{id}/milestones` | `pnc-users` |
| GET | `/artifacts/{id}/revisions` | `pnc-users` |
| GET | `/artifacts/{id}/revisions/{rev}` | `pnc-users` |

### 2. `/attachments` (AttachmentEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/attachments` | `pnc-users` |
| GET | `/attachments/{id}` | `pnc-users` |
| POST | `/attachments` | `pnc-app-attachment-user`, `pnc-app-rex-user`, `pnc-users-admin` |
| PUT | `/attachments/{id}` | `pnc-app-attachment-user`, `pnc-app-rex-user`, `pnc-users-admin` |

### 3. `/build-configs` (BuildConfigurationEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/build-configs` | `pnc-users` |
| GET | `/build-configs/x-with-latest-build` | `pnc-users` |
| POST | `/build-configs` | `pnc-users` |
| GET | `/build-configs/{id}` | `pnc-users` |
| PUT | `/build-configs/{id}` | `pnc-users` |
| PATCH | `/build-configs/{id}` | `pnc-users` |
| POST | `/build-configs/{id}/build` | `pnc-users` |
| GET | `/build-configs/{id}/builds` | `pnc-users` |
| POST | `/build-configs/{id}/clone` | `pnc-users` |
| GET | `/build-configs/{id}/group-configs` | `pnc-users` |
| GET | `/build-configs/{id}/dependencies` | `pnc-users` |
| GET | `/build-configs/{id}/dependants` | `pnc-users` |
| POST | `/build-configs/{id}/dependencies` | `pnc-users` |
| DELETE | `/build-configs/{id}/dependencies/{depId}` | `pnc-users` |
| GET | `/build-configs/{id}/revisions` | `pnc-users` |
| POST | `/build-configs/{id}/revisions` | `pnc-users` |
| GET | `/build-configs/{id}/revisions/{rev}` | `pnc-users` |
| POST | `/build-configs/{id}/revisions/{rev}/build` | `pnc-users` |
| POST | `/build-configs/{id}/revisions/{rev}/restore` | `pnc-users` |
| POST | `/build-configs/create-with-scm` | `pnc-users` |
| GET | `/build-configs/supported-parameters` | `pnc-users` |
| GET | `/build-configs/default-alignment-parameters/{buildType}` | `pnc-users` |

### 4. `/builds` (BuildEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/builds` | `pnc-users` |
| GET | `/builds/{id}` | `pnc-users` |
| DELETE | `/builds/{id}` | `pnc-app-build-delete`, `pnc-app-build-user`, `pnc-users-admin` |
| PUT | `/builds/{id}` | `pnc-app-build-user`, `pnc-users-admin` |
| GET | `/builds/{id}/artifacts/built` | `pnc-users` |
| PUT | `/builds/{id}/artifacts/built` | `pnc-app-build-user`, `pnc-users-admin` |
| POST | `/builds/{id}/artifacts/built/quality` | `pnc-users` (+ programmatic checks inside) |
| GET | `/builds/{id}/artifacts/dependencies` | `pnc-users` |
| GET | `/builds/{id}/attachments` | `pnc-users` |
| PUT | `/builds/{id}/artifacts/dependencies` | `pnc-app-build-user`, `pnc-users-admin` |
| GET | `/builds/{id}/scm-archive` | `pnc-users` |
| POST | `/builds/{id}/attributes` | `pnc-users` |
| DELETE | `/builds/{id}/attributes` | `pnc-users` |
| GET | `/builds/{id}/brew-push` | `pnc-users` (deprecated) |
| GET | `/builds/{id}/build-push-operations` | `pnc-users` |
| POST | `/builds/{id}/brew-push` | `pnc-users` |
| DELETE | `/builds/{id}/brew-push` | `pnc-users` |
| POST | `/builds/{id}/brew-push/complete` | `pnc-users` |
| GET | `/builds/{id}/build-config-revision` | `pnc-users` |
| POST | `/builds/{id}/cancel` | `pnc-users` |
| GET | `/builds/{id}/dependency-graph` | `pnc-users` |
| GET | `/builds/{id}/logs/align` | `pnc-users` |
| GET | `/builds/{id}/logs/build` | `pnc-users` |
| GET | `/builds/ssh-credentials/{id}` | `pnc-users` |
| GET | `/builds/count` | `pnc-users` |
| GET | `/builds/independent-temporary-older-than-timestamp` | `pnc-users` |
| GET | `/builds/build-insights-newer-than-timestamp` | `pnc-users` (internal tag) |
| GET | `/builds/{id}/implicit-dependency-graph` | `pnc-users` |

### 5. `/build-pushes` (BuildPushesEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/build-pushes/{id}` | `pnc-users` |

### 6. `/cache` (CacheEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/cache/statistics` | `pnc-users` |
| GET | `/cache/entity-statistics` | `pnc-users` |
| GET | `/cache/region-statistics` | `pnc-users` |
| GET | `/cache/collection-statistics` | `pnc-users` |
| DELETE | `/cache` | `pnc-users-admin` |

### 7. `/deliverable-analyses` (DeliverableAnalyzerReportEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/deliverable-analyses` | `pnc-users` |
| GET | `/deliverable-analyses/{id}` | `pnc-users` |
| GET | `/deliverable-analyses/{id}/analyzed-artifacts` | `pnc-users` |
| POST | `/deliverable-analyses/{id}/add-label` | `pnc-users` |
| POST | `/deliverable-analyses/{id}/remove-label` | `pnc-users` |
| GET | `/deliverable-analyses/{id}/labels-history` | `pnc-users` |

### 8. `/environments` (EnvironmentEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/environments` | `pnc-users` |
| GET | `/environments/{id}` | `pnc-users` |
| POST | `/environments` | `pnc-app-environment-user`, `pnc-users-admin` |
| POST | `/environments/{id}/deprecate` | `pnc-app-environment-user`, `pnc-users-admin` |

### 9. `/generic-setting` (GenericSettingEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `announcement-banner` | `pnc-users` (deprecated) |
| POST | `announcement-banner` | `pnc-users-admin` (deprecated) |
| GET | `pnc-version` | `pnc-users` (deprecated) |
| POST | `pnc-version` | `pnc-users-admin` (deprecated) |
| GET | `in-maintenance-mode` | `pnc-users` (deprecated) |
| GET | `is-user-allowed-to-trigger-builds` | `pnc-users` |
| POST | `activate-maintenance-mode` | `pnc-users-admin` (deprecated) |
| POST | `deactivate-maintenance-mode` | `pnc-users-admin` (deprecated) |
| GET | `limited-build-users` | `pnc-users` |
| POST | `limited-build-users/{username}` | `pnc-users-admin` |
| DELETE | `limited-build-users/{username}` | `pnc-users-admin` |

### 10. `/group-builds` (GroupBuildEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/group-builds` | `pnc-users` |
| GET | `/group-builds/{id}` | `pnc-users` |
| DELETE | `/group-builds/{id}` | `pnc-users` |
| GET | `/group-builds/{id}/builds` | `pnc-users` |
| POST | `/group-builds/{id}/brew-push` | `pnc-users` |
| POST | `/group-builds/{id}/cancel` | `pnc-users` |
| GET | `/group-builds/{id}/dependency-graph` | `pnc-users` |

### 11. `/group-configs` (GroupConfigurationEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/group-configs` | `pnc-users` |
| POST | `/group-configs` | `pnc-users` |
| GET | `/group-configs/{id}` | `pnc-users` |
| PUT | `/group-configs/{id}` | `pnc-users` |
| PATCH | `/group-configs/{id}` | `pnc-users` |
| POST | `/group-configs/{id}/build` | `pnc-users` |
| GET | `/group-configs/{id}/build-configs` | `pnc-users` |
| POST | `/group-configs/{id}/build-configs` | `pnc-users` |
| DELETE | `/group-configs/{id}/build-configs/{configId}` | `pnc-users` |
| GET | `/group-configs/{id}/builds` | `pnc-users` |
| GET | `/group-configs/{id}/group-builds` | `pnc-users` |

### 12. `/operations` (OperationEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| POST | `/operations/{id}/complete` | `pnc-users` |
| PUT | `/operations/deliverable-analyzer/{id}` | `pnc-users-admin` |
| GET | `/operations/deliverable-analyzer/{id}` | `pnc-users` |
| GET | `/operations/deliverable-analyzer` | `pnc-users` |
| POST | `/operations/deliverable-analyzer/start` | `pnc-users` |
| GET | `/operations/build-pushes/{id}` | `pnc-users` |
| GET | `/operations/build-pushes` | `pnc-users` |

### 13. `/pnc-status` (PncStatusEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| POST | `/pnc-status` | `pnc-users-admin` |
| GET | `/pnc-status` | `pnc-users` |

### 14. `/products` (ProductEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/products` | `pnc-users` |
| POST | `/products` | `pnc-users` |
| GET | `/products/{id}` | `pnc-users` |
| PUT | `/products/{id}` | `pnc-users` |
| PATCH | `/products/{id}` | `pnc-users` |
| GET | `/products/{id}/versions` | `pnc-users` |

### 15. `/product-milestones` (ProductMilestoneEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| POST | `/product-milestones` | `pnc-users` |
| GET | `/product-milestones/{id}` | `pnc-users` |
| PUT | `/product-milestones/{id}` | `pnc-users` |
| PATCH | `/product-milestones/{id}` | `pnc-users` |
| GET | `/product-milestones/{id}/builds` | `pnc-users` |
| POST | `/product-milestones/{id}/close` | `pnc-users` |
| DELETE | `/product-milestones/{id}/close` | `pnc-users` |
| GET | `/product-milestones/{id}/build-push-operations` | `pnc-users` |
| GET | `/product-milestones/{id}/delivered-artifacts` | `pnc-users` |
| GET | `/product-milestones/{id}/deliverables-analyzer-operations` | `pnc-users` |
| GET | `/product-milestones/{id}/statistics` | `pnc-users` |
| POST | `/product-milestones/validate-version` | `pnc-users` |
| POST | `/product-milestones/{id}/analyze-deliverables` | `pnc-users` |
| GET | `/product-milestones/comparisons/delivered-artifacts` | `pnc-users` |
| GET | `/product-milestones/{id}/interconnection-graph` | `pnc-users` |
| GET | `/product-milestones/delivered-artifacts/shared` | `pnc-users` |

### 16. `/product-releases` (ProductReleaseEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| POST | `/product-releases` | `pnc-users` |
| GET | `/product-releases/{id}` | `pnc-users` |
| PUT | `/product-releases/{id}` | `pnc-users` |
| PATCH | `/product-releases/{id}` | `pnc-users` |
| GET | `/product-releases/support-levels` | `pnc-users` |

### 17. `/product-versions` (ProductVersionEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| POST | `/product-versions` | `pnc-users` |
| GET | `/product-versions/{id}` | `pnc-users` |
| PUT | `/product-versions/{id}` | `pnc-users` |
| PATCH | `/product-versions/{id}` | `pnc-users` |
| GET | `/product-versions/{id}/build-configs` | `pnc-users` |
| GET | `/product-versions/{id}/group-configs` | `pnc-users` |
| GET | `/product-versions/{id}/milestones` | `pnc-users` |
| GET | `/product-versions/{id}/releases` | `pnc-users` |
| GET | `/product-versions/{id}/statistics` | `pnc-users` |
| GET | `/product-versions/{id}/artifact-quality-statistics` | `pnc-users` |
| GET | `/product-versions/{id}/repository-type-statistics` | `pnc-users` |

### 18. `/projects` (ProjectEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/projects` | `pnc-users` |
| POST | `/projects` | `pnc-users` |
| GET | `/projects/{id}` | `pnc-users` |
| PUT | `/projects/{id}` | `pnc-users` |
| PATCH | `/projects/{id}` | `pnc-users` |
| GET | `/projects/{id}/build-configs` | `pnc-users` |
| GET | `/projects/{id}/builds` | `pnc-users` |

### 19. `/scm-repositories` (SCMRepositoryEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/scm-repositories` | `pnc-users` |
| GET | `/scm-repositories/{id}` | `pnc-users` |
| PUT | `/scm-repositories/{id}` | `pnc-users` |
| PATCH | `/scm-repositories/{id}` | `pnc-users` |
| POST | `/scm-repositories/create-and-sync` | `pnc-users` |
| GET | `/scm-repositories/{id}/build-configs` | `pnc-users` |

### 20. `/slsa/build-provenance/v1` (SlsaProvenanceV1Endpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | (all 6 sub-paths) | `pnc-users` |

### 21. `/target-repositories` (TargetRepositoryEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/target-repositories` | `pnc-users` |
| GET | `/target-repositories/{id}` | `pnc-users` |
| POST | `/target-repositories` | `pnc-app-build-user`, `pnc-app-artifact-user`, `pnc-users-admin` |
| GET | `/target-repositories/{id}/artifacts` | `pnc-users` |

### 22. `/users` (UserEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/users/current` | `pnc-users` |
| GET | `/users/login/{redirectPath}` | `pnc-users` (triggers SSO) |
| GET | `/users/logout/{redirectPath}` | `pnc-users` (triggers SSO) |
| GET | `/users/{id}/builds` | `pnc-users` |

### 23. `/version` (VersionEndpoint)
| Method | Path | Required Roles |
|--------|------|---------------|
| GET | `/version` | `pnc-users` |

## Internal Endpoints (rest-api-internal module — 5 interfaces)

These are service-to-service endpoints not exposed externally, secured with `@RolesAllowed`.

| Interface | Base Path | Annotation |
|-----------|-----------|------------|
| BpmEndpoint | `/bpm` | `pnc-users`, `pnc-users-admin` (`@RolesAllowed`) |
| BuildTaskEndpoint | `/build-tasks` | `pnc-app-build-tasks-user`, `pnc-users-admin` (`@RolesAllowed`) |
| DeliverableAnalysisEndpoint | `/deliverable-analyses/complete` | `pnc-users` (`@RolesAllowed`) |
| DebugEndpoint | `/debug` | `pnc-users-admin` (`@RolesAllowed`) |
| HealthCheckEndpoint | `/health` | `pnc-users-admin` (`@RolesAllowed`) |

## Other Endpoints (outside rest-api)

| Module | Type | Path | Roles |
|--------|------|------|-------|
| `rest` | JAX-RS | `/build-records/{id}` | `pnc-users` (301 redirect to `/builds/{id}`) |
| `web` | Servlet | `/config.js`, `/config.json` | Outside JAX-RS scope (UI config servlets) |
