/*
 * Copyright 2020 the original author or authors.
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

plugins {
    id("java-library")
    id("org.gradle.kotlin.kotlin-dsl") // this is 'kotlin-dsl' without version
    id("gradlebuild.code-quality")
    id("org.gradle.kotlin-dsl.ktlint-convention")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api(platform(project(":build-platform")))
    implementation("gradlebuild:code-quality")
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

ktlint {
    filter {
        exclude("gradle/kotlin/dsl/accessors/_*/**")
    }
}



// TODO get rid?
afterEvaluate {
    if (tasks.withType<ValidatePlugins>().isEmpty()) {
        val validatePlugins by tasks.registering(ValidatePlugins::class) {
            outputFile.set(project.reporting.baseDirectory.file("task-properties/report.txt"))

            val mainSourceSet = project.sourceSets.main.get()
            classes.setFrom(mainSourceSet.output.classesDirs)
            dependsOn(mainSourceSet.output)
            classpath.setFrom(mainSourceSet.runtimeClasspath)
        }
        tasks.check { dependsOn(validatePlugins) }
    }
}

tasks.withType<ValidatePlugins>().configureEach {
    failOnWarning.set(true)
    enableStricterValidation.set(true)
}

