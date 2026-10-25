# Repository map

```text
trails-auth-none/
|-- pom.xml                         SDK dependency, build, source/Javadoc packaging, release profile
|-- .mvn/wrapper/                   Maven wrapper configuration
|-- mvnw / mvnw.cmd                 Platform build entry points
|-- format/java-formatter.xml       SDK-aligned formatter
|-- src/main/java/.../authnone/      Boot registration and shared-identity request filter
|-- src/main/resources/META-INF/    Boot auto-configuration discovery
|-- src/test/java/.../authnone/      HTTP security and composition tests with SDK-only fixtures
|-- README.md                       Dependency selection and shared-profile semantics
`-- LICENSE                        Apache 2.0
```
