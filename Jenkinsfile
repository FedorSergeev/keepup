pipeline {
    agent any
    tools {
        jdk "openjdk15"
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'develop',
                        url: 'https://github.com/FedorSergeev/keepup.git'
            }
        }
        stage('Chmod for Gradle wrapper') {
            steps {
                script {
                    sh 'chmod +x gradlew'
                }
            }
        }
        stage('Build executable') {
            steps {
                script {
                    sh './gradlew clean build --no-daemon'
                }
            }

        }
        stage('Publish to Maven local') {
            steps {
                script {
                    sh './gradlew publishToMavenLocal --no-daemon'
                }
            }

        }

       stage('Publish project in Nexus') {
           steps {
               script {
                   sh './gradlew :core:generatePomFileForMavenPublication --no-daemon'
               }
               script {
                   pom = readMavenPom file: "pom.xml"
                   pom.packaging = "jar"
                   filesByGlob = findFiles(glob: "./build/libs/*-2.0-SNAPSHOT.jar")
                   echo "${filesByGlob}"
                   echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                   artifactPath = filesByGlob[0].path;
                   artifactExists = fileExists artifactPath;
                   if (artifactExists) {
                       echo "*** File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}";
                       nexusArtifactUploader(
                               nexusVersion: "nexus2",
                               protocol: "http",
                               nexusUrl: "nexus:8081/nexus",
                               groupId: pom.groupId,
                               version: pom.version,
                               repository: "transit-snapshot",
                               credentialsId: "nexus-local",
                               artifacts: [
                                       [artifactId: pom.artifactId,
                                        classifier: '',
                                        file      : artifactPath,
                                        type      : pom.packaging],
                                       [artifactId: pom.artifactId,
                                        classifier: '',
                                        file      : "pom.xml",
                                        type      : "pom"]
                               ]
                       );
                   } else {
                       error "*** File: ${artifactPath}, could not be found";
                   }
               }
           }
       }
    }
}