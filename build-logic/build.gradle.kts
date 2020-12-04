/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

tasks.register("clean") {
    dependsOn(subprojects.map { "${it.name}:clean" })
}
tasks.register("check") {
    dependsOn(subprojects.map { "${it.name}:check" })
}

// TODO the logic in this file can either go away or needs to be generalized for all builds of the composite

fun readProperties(propertiesFile: File) = java.util.Properties().apply {
    propertiesFile.inputStream().use { fis ->
        load(fis)
    }
}

val checkSameDaemonArgs by tasks.registering {
    val buildSrcPropertiesFile = project.rootDir.resolve("gradle.properties")
    val rootPropertiesFile = project.rootDir.resolve("../gradle.properties")
    doLast {
        val buildSrcProperties = readProperties(buildSrcPropertiesFile)
        val rootProperties = readProperties(rootPropertiesFile)
        val jvmArgs = listOf(buildSrcProperties, rootProperties).map { it.getProperty("org.gradle.jvmargs") }.toSet()
        if (jvmArgs.size > 1) {
            throw GradleException("gradle.properties and buildSrc/gradle.properties have different org.gradle.jvmargs " +
                "which may cause two daemons to be spawned on CI and in IDEA. " +
                "Use the same org.gradle.jvmargs for both builds.")
        }
    }
}

// tasks.build { dependsOn(checkSameDaemonArgs) }

val isCiServer: Boolean by extra { "CI" in System.getenv() }

if (isCiServer) {
    println("Current machine has ${Runtime.getRuntime().availableProcessors()} CPU cores")

    if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        require(Runtime.getRuntime().availableProcessors() >= 6) { "Windows CI machines must have at least 6 CPU cores!" }
    }
    gradle.buildFinished {
        allprojects.forEach { project ->
            project.tasks.all {
                if (this is Reporting<*> && state.failure != null) {
                    prepareReportForCIPublishing(project.name, this.reports["html"].destination)
                }
            }
        }
    }
}

fun Project.prepareReportForCIPublishing(projectName: String, report: File) {
    if (report.isDirectory) {
        val destFile = File("${rootProject.buildDir}/report-$projectName-${report.name}.zip")
        ant.withGroovyBuilder {
            "zip"("destFile" to destFile) {
                "fileset"("dir" to report)
            }
        }
    } else {
        copy {
            from(report)
            into(rootProject.buildDir)
            rename { "report-$projectName-${report.parentFile.name}-${report.name}" }
        }
    }
}

