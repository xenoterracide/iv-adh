import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.api.tasks.testing.logging.TestLogEvent
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import net.ltgt.gradle.nullaway.nullaway

group = "com.xenoterracide"
version = "1.0-SNAPSHOT"

plugins {
  `java-library`
  checkstyle
  id("net.ltgt.errorprone").version("1.3.0")
  id("net.ltgt.nullaway").version("1.0.2")
  id("org.checkerframework").version("0.5.16")
  // NOTE: external plugin version is specified in implementation dependency artifact of the project's build file
  id("com.github.spotbugs").version("4.6.0")
  id("com.diffplug.spotless").version("5.8.2")
}

repositories {
  mavenCentral()
}

dependencyLocking {
  lockAllConfigurations()
}

val sbv = "2.4.+"
dependencies {
  compileOnly(platform("org.springframework.boot:spring-boot-starter-parent:$sbv"))
  compileOnly("org.apache.logging.log4j:log4j-api")
  implementation(platform("org.springframework.boot:spring-boot-starter-parent:$sbv"))
  // Use JUnit Jupiter API for testing.
  testImplementation("org.springframework:spring-test")
  testImplementation("org.springframework.boot:spring-boot-test")
  testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("org.assertj:assertj-core")
  testImplementation("org.mockito:mockito-core")
  // Use JUnit Jupiter Engine for testing.
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  errorprone("com.google.errorprone:error_prone_core:2.5.+")
  errorprone("com.uber.nullaway:nullaway:0.8.+")
}

configurations.all {
  resolutionStrategy.dependencySubstitution.all {
    requested.let {
      if (it is ModuleComponentSelector && it.module == "spring-boot-starter-logging") {
        useTarget("org.springframework.boot:spring-boot-starter-log4j2:${it.version}", "Use Log4j2 instead of Logback")
      }
    }
  }
}

tasks.test {
  // Use junit platform for unit tests.
  useJUnitPlatform()

  testLogging {
    lifecycle {
      showStandardStreams = true
      displayGranularity = 2
      events.addAll(listOf(org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED, org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED, org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED, org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED))
    }
  }
  reports {
    html.isEnabled = false
    junitXml.isEnabled = true
  }
}

tasks.withType<Checkstyle>().configureEach {
  isShowViolations = true
  reports {
    html.isEnabled = false
    xml.isEnabled = false
  }
}

tasks.named<Checkstyle>("checkstyleMain") {
  configFile = file("config/checkstyle/main.xml")
}

tasks.named<Checkstyle>("checkstyleTest") {
  configFile = file("config/checkstyle/test.xml")
}

spotbugs {
  effort.set(Effort.MAX)
  reportLevel.set(Confidence.LOW)
}

tasks.withType<SpotBugsTask>().configureEach {
  reports.register("html") {
    enabled = true
  }
}

val header = "Copyright © \$YEAR Caleb Cushing. All rights reserved"
spotless {
  ratchetFrom = "HEAD"
  java {
    licenseHeader("/* $header */")
  }
  format("misc") {
    target("*.ts", "*.scss", "*.css")
    licenseHeader("/* $header */", "")
  }
  format("html") {
    target("*.html")
    licenseHeader("<!-- $header -->", "")
  }
  sql {
    target("src/main/resources/**/*.sql")
    licenseHeader("-- $header", "--liquibase formatted sql")
    dbeaver()
  }
}


java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

nullaway {
  annotatedPackages.add("com.xenoterracide")
}

checkerFramework {
  excludeTests = true
  extraJavacArgs.addAll(listOf("-Astubs=$buildDir/../config/stubs"))
  checkers.addAll(
    listOf(
      "org.checkerframework.checker.nullness.NullnessChecker"
    )
  )
}

tasks.withType<JavaCompile>().configureEach {
  options.errorprone {
    nullaway {
      severity.set(CheckSeverity.ERROR)
      acknowledgeRestrictiveAnnotations.set(true)
      handleTestAssertionLibraries.set(true)
    }
    disableWarningsInGeneratedCode.set(true)
    error(
      "AmbiguousMethodReference",
      "ArgumentSelectionDefectChecker",
      "ArrayAsKeyOfSetOrMap",
      "AssertEqualsArgumentOrderChecker",
      "AssertThrowsMultipleStatements",
      "AssertionFailureIgnored",
      "AssignmentToMock",
      "BadComparable",
      "BadImport",
      "BadInstanceof",
      "BigDecimalEquals",
      "BigDecimalLiteralDouble",
      "BoxedPrimitiveConstructor",
      "BoxedPrimitiveEquality",
      "ByteBufferBackingArray",
      "CacheLoaderNull",
      "CannotMockFinalClass",
      "CanonicalDuration",
      "CatchFail",
      "CatchAndPrintStackTrace",
      "ClassCanBeStatic",
      "ClassNewInstance", // sketchy
      "CollectionUndefinedEquality",
      "CollectorShouldNotUseState",
      "ComparableAndComparator",
      "CompareToZero",
      "ComplexBooleanConstant",
      "DateFormatConstant",
      "DefaultCharset",
      "DefaultPackage",
      "DoubleBraceInitialization",
      "DoubleCheckedLocking",
      "EmptyCatch",
      "EqualsGetClass",
      "EqualsIncompatibleType",
      "EqualsUnsafeCast",
      "EqualsUsingHashCode",
      "ExtendingJUnitAssert",
      "FallThrough",
      "Finally",
      "FloatCast",
      "FloatingPointLiteralPrecision",
      "FutureReturnValueIgnored",
      "GetClassOnEnum",
      "HidingField",
      "ImmutableAnnotationChecker",
      "ImmutableEnumChecker",
      "InconsistentCapitalization",
      "InconsistentHashCode",
      "IncrementInForLoopAndHeader",
      "InlineFormatString",
      "InputStreamSlowMultibyteRead",
      "InstanceOfAndCastMatchWrongType",
      "InvalidThrows",
      "IterableAndIterator",
      "JavaDurationGetSecondsGetNano",
      "JavaDurationWithNanos",
      "JavaDurationWithSeconds",
      "JavaInstantGetSecondsGetNano",
      "JavaLangClash",
      "JavaLocalDateTimeGetNano",
      "JavaLocalTimeGetNano",
      "JavaTimeDefaultTimeZone",
      // "JavaUtilDate",
      "LockNotBeforeTry",
      "LockOnBoxedPrimitive",
      "LogicalAssignment",
      "MissingCasesInEnumSwitch",
      "Overrides",
      "MissingOverride",
      "MixedMutabilityReturnType",
      "ModifiedButNotUsed",
      "ModifyCollectionInEnhancedForLoop",
      "ModifySourceCollectionInStream",
      "MultipleParallelOrSequentialCalls",
      "MultipleUnaryOperatorsInMethodCall",
      "MutableConstantField",
      "MutablePublicArray",
      "NestedInstanceOfConditions",
      "NonAtomicVolatileUpdate",
      "NonOverridingEquals",
      "NullOptional",
      "NullableConstructor",
      "NullablePrimitive",
      "NullableVoid",
      "ObjectToString",
      "ObjectsHashCodePrimitive",
      "OperatorPrecedence",
      "OptionalMapToOptional",
      "OrphanedFormatString",
      "OverrideThrowableToString",
      "PreconditionsCheckNotNullRepeated",
      "PrimitiveAtomicReference",
      "ProtectedMembersInFinalClass",
      "PreconditionsCheckNotNullRepeated",
      "ReferenceEquality",
      "ReturnFromVoid",
      "RxReturnValueIgnored",
      "SameNameButDifferent",
      "ShortCircuitBoolean",
      "StaticAssignmentInConstructor",
      "StaticGuardedByInstance",
      // "StaticMockMember",
      "StreamResourceLeak",
      // "StreamToIterable",
      "StringSplitter",
      "SynchronizeOnNonFinalField",
      "ThreadJoinLoop",
      "ThreadLocalUsage",
      "ThreeLetterTimeZoneID",
      "TimeUnitConversionChecker",
      "ToStringReturnsNull",
      "TreeToString",
      "TypeEquals",
      "TypeNameShadowing",
      "TypeParameterShadowing",
      "TypeParameterUnusedInFormals", // sketchy
      "URLEqualsHashCode",
      "UndefinedEquals",
      "UnnecessaryAnonymousClass",
      "UnnecessaryLambda",
      "UnnecessaryMethodInvocationMatcher",
      // "UnnecessaryMethodReference",
      "UnnecessaryParentheses", // sketchy
      "UnsafeFinalization",
      "UnsafeReflectiveConstructionCast",
      "UnusedMethod",
      "UnusedNestedClass",
      "UnusedVariable",
      "UseCorrectAssertInTests",
      // "UseTimeInScope",
      "VariableNameSameAsType",
      "WaitNotInLoop",
      "ClassName",
      "ComparisonContractViolated",
      "DeduplicateConstants",
      "DivZero",
      "EmptyIf",
      "FuzzyEqualsShouldNotBeUsedInEqualsMethod",
      "IterablePathParameter",
      "LongLiteralLowerCaseSuffix",
      "NumericEquality",
      "StaticQualifiedUsingExpression",
      "AnnotationPosition",
      "AssertFalse",
      "CheckedExceptionNotThrown",
      // "DifferentNameButSame",
      "EmptyTopLevelDeclaration",
      "EqualsBrokenForNull",
      "ExpectedExceptionChecker",
      "InconsistentOverloads",
      // "InitializeInline",
      "InterruptedExceptionSwallowed",
      "InterfaceWithOnlyStatics",
      "NonCanonicalStaticMemberImport",
      "PreferJavaTimeOverload",
      "ClassNamedLikeTypeParameter",
      "ConstantField",
      "FieldCanBeLocal",
      "FieldCanBeStatic",
      "ForEachIterable",
      "MethodCanBeStatic",
      "MultiVariableDeclaration",
      "MultipleTopLevelClasses",
      "PackageLocation",
      "RemoveUnusedImports",
      "Var",
      "WildcardImport"
    )
  }
}