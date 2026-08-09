pipeline {

    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven-3.9.16'
        nodejs 'NodeJS-24'

    }

    environment {
        DEPLOY_DIR = 'C:\\Deployments\\Naukri'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/Swetha98867/team-zanskar-Naukri.git'
            }
        }

        stage('Backend Build') {
            steps {
                dir('backend') {
                    bat 'mvn clean verify'
                }
            }
        }

        stage('Backend Test') {
            steps {
                dir('backend') {
                    bat 'mvn test'
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('frontend') {
                    bat 'npm install'
                    bat 'npm run build'
                }
            }
        }

        stage('Copy Frontend to Electron') {
            steps {
                bat '''
                if exist electron\\renderer rmdir /S /Q electron\\renderer
                xcopy frontend\\dist electron\\renderer /E /I /Y
                '''
            }
        }

        stage('Electron Install') {
            steps {
                dir('electron') {
                    bat 'npm install'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    dir('backend') {
                         bat '''
                         mvn sonar:sonar ^
                        -Dsonar.projectKey=Naukri ^
                        -Dsonar.projectName=Naukri ^
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Electron Package') {
            steps {
                dir('electron') {
                    bat 'npm run dist'
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: '''
backend/target/*.jar,
dist/**/*.exe,
dist/**/*.zip
''', fingerprint: true
            }
        }

        stage('Deploy') {
            steps {
                    bat '''
                    if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

                    copy backend\\target\\*.jar "%DEPLOY_DIR%\\"
                    copy dist\\*.exe "%DEPLOY_DIR%\\"
                    '''
                }
            }
    }

    post {

        success {
            echo '===================================='
            echo 'CI/CD Pipeline Completed Successfully'
            echo '===================================='
        }

        failure {
            echo 'Pipeline Failed'
        }

        always {
            cleanWs()
        }
    }

}
