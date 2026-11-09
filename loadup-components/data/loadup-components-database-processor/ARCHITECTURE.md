# Database Processor Architecture

This build-time module owns LoadUp's fixed MyBatis-Flex generation policy. It has no dependency on Spring or the
runtime database component. The framework BOM manages its version and the official processor version together.

`LoadUpMyBatisFlexProcessor` extends the official processor. Before the upstream `init`, it writes the bundled
UTF-8 policy into the current compilation's CLASS_OUTPUT and verifies that the upstream filesystem search can
locate a Maven/Gradle root. Policy preparation failures emit a compiler error and preserve the original cause.
The upstream processor retains annotation handling, generation, supported types and source version behavior.

The compiler plugin explicitly selects the LoadUp entry because the upstream transitive dependency also advertises
an SPI provider. Consumers must not select the upstream entry as well. MapStruct and Spring Boot metadata processing
remain independent entries on the same path. The processor disables processing when compiling its own sources.

Entity modules keep no policy copies. The output policy stops ancestor lookup, making generation independent of
module-local and repository-local configuration. Main and test compilation each prepare their own output policy.
Framework entity modules use a provided/optional dependency to establish reactor ordering without runtime export;
independent consumer repositories resolve the published processor through annotationProcessorPaths.

The JDK compiler regression tests check generated source compilation, fixed package/naming conventions, main/test
output isolation and failure to prepare the policy. These tests are also the compatibility boundary for upstream
processor upgrades; no upstream implementation is copied or modified.
