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

package org.gradle.integtests.composite

class ConfigCacheIntegrationTest extends AbstractCompositeBuildIntegrationTest {

    def "included build with precompiled plugin and library - only library dependency"() {
        given:
        includePluginAndLibraryBuild('included-build')

        when:
        buildFile << """
            plugins {
                id("java-library")
            }
            dependencies {
                implementation("com.example:included-build")
            }
        """
        file("src/main/java/Foo.java") << "class Foo { Bar newBar() { return new Bar(); }}"

        then:
        succeeds("build")
    }

    def "included build with library and precompiled plugin - only plugin dependency"() {
        given:
        includePluginAndLibraryBuild('included-build')

        when:
        buildFile << """
            plugins {
                id("java-library")
                id("included-build.project-plugin")
            }
        """
        file("src/main/java/Foo.java") << "class Foo {}"

        then:
        succeeds("build")
    }

    def "included build with library only"() {
        given:
        includeLibraryBuild('included-build')

        when:
        buildFile << """
            plugins {
                id("java-library")
            }
            dependencies {
                implementation("com.example:included-build")
            }
        """
        file("src/main/java/Foo.java") << "class Foo { Bar newBar() { return new Bar(); }}"

        then:
        succeeds("build")
    }

    def "included build with precompiled plugin only"() {
        given:
        includePluginBuild('included-build')

        when:
        buildFile << """
            plugins {
                id("java-library")
                id("included-build.project-plugin")
            }
        """
        file("src/main/java/Foo.java") << "class Foo {}"

        then:
        succeeds("build")
    }

    private void includePluginBuild(String buildName) {
        file("$buildName/settings.gradle") << "rootProject.name = '$buildName'"
        file("$buildName/build.gradle") << """
            plugins {
                id("groovy-gradle-plugin")
            }
        """
        file("$buildName/src/main/groovy/${buildName}.project-plugin.gradle") << "println('$buildName project plugin applied')"

        settingsFile << """
            rootProject.name = 'root'
            includeBuild('included-build')
        """
    }

    private void includeLibraryBuild(String buildName) {
        file("$buildName/settings.gradle") << "rootProject.name = '$buildName'"
        file("$buildName/build.gradle") << """
            plugins {
                id("java-library")
            }
            group = "com.example"
            version = "1.0"
        """
        file("$buildName/src/main/java/Bar.java") << "class Bar {}"

        settingsFile << """
            rootProject.name = 'root'
            includeBuild('$buildName')
        """
    }

    private void includePluginAndLibraryBuild(String buildName) {
        file("$buildName/settings.gradle") << "rootProject.name = '$buildName'"
        file("$buildName/build.gradle") << """
            plugins {
                id("groovy-gradle-plugin")
                id("java-library")
            }

            group = "com.example"
            version = "1.0"
        """
        file("$buildName/src/main/groovy/${buildName}.project-plugin.gradle") << "println('$buildName project plugin applied')"
        file("$buildName/src/main/java/Bar.java") << "class Bar {}"

        settingsFile << """
            rootProject.name = 'root'
            includeBuild('$buildName')
        """
    }
}
