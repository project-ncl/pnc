# @RolesAllowed: Provider vs Endpoint comparison

Only methods that have `@RolesAllowed` on **at least one** layer are listed.
Methods that rely solely on the class-level `@RolesAllowed({ USERS, USERS_ADMIN })` are omitted.

## Build

| Method | Provider (`BuildProviderImpl`) | Endpoint (`BuildEndpointImpl`) |
|---|---|---|
| `delete(id)` | `BUILD_ADMIN, ADMIN` | N/A (not exposed via REST) |
| `delete(id, callback)` | `BUILD_DELETE, BUILD_ADMIN, ADMIN` | `BUILD_DELETE, BUILD_ADMIN, ADMIN` |
| `update` | `BUILD_ADMIN, ADMIN` | `BUILD_ADMIN, ADMIN` |
| `setBuiltArtifacts` | `BUILD_ADMIN, ADMIN` | `BUILD_ADMIN, ADMIN` |
| `setDependentArtifacts` | `BUILD_ADMIN, ADMIN` | `BUILD_ADMIN, ADMIN` |

## GroupBuild

| Method | Provider (`GroupBuildProviderImpl`) | Endpoint (`GroupBuildEndpointImpl`) |
|---|---|---|
| `update` | `BUILD_ADMIN, ADMIN` | N/A (not exposed via REST) |
| `delete` | _(none)_ | `BUILD_DELETE, BUILD_ADMIN, ADMIN` |

## Artifact

| Method | Provider (`ArtifactProviderImpl`) | Endpoint (`ArtifactEndpointImpl`) |
|---|---|---|
| `store` / `create` | `ARTIFACT_ADMIN, ADMIN` | `ARTIFACT_ADMIN, ADMIN` |
| `update` | _(none)_ | `ARTIFACT_ADMIN, ADMIN` |

## Attachment

| Method | Provider (`AttachmentProviderImpl`) | Endpoint (`AttachmentEndpointImpl`) |
|---|---|---|
| `store` / `create` | `ATTACHMENT_ADMIN, REX, ADMIN` | `ATTACHMENT_ADMIN, REX, ADMIN` |
| `update` | `ATTACHMENT_ADMIN, REX, ADMIN` | `ATTACHMENT_ADMIN, REX, ADMIN` |

## Environment

| Method | Provider (`EnvironmentProviderImpl`) | Endpoint (`EnvironmentEndpointImpl`) |
|---|---|---|
| `store` / `createNew` | `ENVIRONMENT_ADMIN, ADMIN` | `ENVIRONMENT_ADMIN, ADMIN` |
| `deprecateEnvironment` / `deprecate` | `ENVIRONMENT_ADMIN, ADMIN` | `ENVIRONMENT_ADMIN, ADMIN` |

## Operation

| Method | Provider (`OperationProviderImpl`) | Endpoint (`OperationEndpointImpl`) |
|---|---|---|
| `update` / `updateDeliverableAnalyzer` | `ADMIN` | `ADMIN` |

## TargetRepository

| Method | Provider (`TargetRepositoryProviderImpl`) | Endpoint (`TargetRepositoryEndpointImpl`) |
|---|---|---|
| `store` / `createNew` | `BUILD_ADMIN, ARTIFACT_ADMIN, ADMIN` | `BUILD_ADMIN, ARTIFACT_ADMIN, ADMIN` |
