pipeline {
    agent any
    
    environment {
        MAVEN_HOME = '/usr/share/maven'
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk'
        PROJECT_DIR = 'skillrat'
        SONAR_HOST_URL = credentials('sonar-host-url')
        SONAR_AUTH_TOKEN = credentials('sonar-auth-token')
    }
    
    parameters {
        choice(name: 'BRANCH', choices: ['main', 'develop', 'staging'], description: 'Branch to build')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit tests')
        booleanParam(name: 'RUN_SONAR', defaultValue: true, description: 'Run SonarQube analysis')
        booleanParam(name: 'BUILD_DOCKER', defaultValue: false, description: 'Build Docker image')
    }
    
    options {
        timeout(time: 1, unit: 'HOURS')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }
    
    triggers {
        githubPush()
        pollSCM('H/15 * * * *')
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '========== Checking out code =========='
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[url: 'https://github.com/hltsolutionsadmin/skillrat_backend_new.git']]
                ])
            }
        }
        
        stage('Build') {
            steps {
                echo '========== Building with Maven =========='
                dir("${PROJECT_DIR}") {
                    sh 'mvn -B clean package -DskipTests'
                }
            }
        }
        
        stage('Unit Tests') {
            when {
                expression { params.RUN_TESTS == true }
            }
            steps {
                echo '========== Running Unit Tests =========='
                dir("${PROJECT_DIR}") {
                    sh 'mvn -B test'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/TEST-*.xml'
                }
            }
        }
        
        stage('Code Quality') {
            when {
                expression { params.RUN_SONAR == true }
            }
            steps {
                echo '========== SonarQube Analysis =========='
                dir("${PROJECT_DIR}") {
                    sh 'mvn sonar:sonar'
                }
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: '${PROJECT_DIR}/target/**.jar', allowEmptyArchive: true
            cleanWs(deleteDirs: true, patterns: [[pattern: '**/target/**', type: 'INCLUDE']])
        }
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
