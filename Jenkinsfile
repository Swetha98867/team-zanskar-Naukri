pipeline {

    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven-3.9.16'
        nodejs 'NodeJS-24'
    }

    environment {
        DEPLOY_DIR = 'C:\\Deployments\\Naukri'

        AZURE_STORAGE_ACCOUNT = 'naukribuildartifacts'
        BACKEND_CONTAINER = 'backend-artifacts'
        FRONTEND_CONTAINER = 'frontend-artifacts'
    }

    stages {

        // ==========================================================
        // CHECKOUT
        // ==========================================================

        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-naukri',
                    url: 'https://github.com/Swetha98867/team-zanskar-Naukri.git'
            }
        }


        // ==========================================================
        // BACKEND BUILD
        // ==========================================================

        stage('Backend Build') {
            steps {
                dir('backend') {
                    bat 'mvn clean verify'
                }
            }
        }


        // ==========================================================
        // BACKEND TEST
        // ==========================================================

        stage('Backend Test') {
            steps {
                dir('backend') {
                    bat 'mvn test'
                }
            }
        }


        // ==========================================================
        // FRONTEND BUILD
        // ==========================================================

        stage('Frontend Build') {
            steps {
                dir('frontend') {
                    bat 'npm install'
                    bat 'npm run build'
                }
            }
        }


        // ==========================================================
        // COPY FRONTEND TO ELECTRON
        // ==========================================================

        stage('Copy Frontend to Electron') {
            steps {
                bat '''
                if exist electron\\renderer rmdir /S /Q electron\\renderer

                xcopy frontend\\dist electron\\renderer /E /I /Y

                if errorlevel 1 (
                    echo ERROR: Failed to copy frontend build to Electron.
                    exit /b 1
                )
                '''
            }
        }


        // ==========================================================
        // ELECTRON INSTALL
        // ==========================================================

        stage('Electron Install') {
            steps {
                dir('electron') {
                    bat 'npm install'
                }
            }
        }


        // ==========================================================
        // PREPARE BUNDLED JRE
        // ==========================================================

        stage('Prepare Bundled JRE') {
            steps {
            bat '''
            echo ====================================
            echo Preparing Bundled JRE
            echo ====================================

            if not exist "C:\\Tools\\Naukri\\jre\\bin\\java.exe" (
                echo ERROR: Bundled JRE does not exist.
                echo Expected:
                echo C:\\Tools\\Naukri\\jre\\bin\\java.exe
                exit /b 1
             )
    
        if not exist "electron\\resources\\jre" (
            mkdir "electron\\resources\\jre"
        )

        xcopy "C:\\Tools\\Naukri\\jre\\*" ^
              "electron\\resources\\jre\\" ^
              /E /I /Y

        if errorlevel 1 (
            echo ERROR: Failed to copy bundled JRE.
            exit /b 1
        )

        echo Bundled JRE copied successfully.
        '''
    }
}

        // ==========================================================
        // SONARQUBE ANALYSIS
        // ==========================================================

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


        // ==========================================================
        // QUALITY GATE
        // ==========================================================

        stage('Quality Gate') {
            steps {

                timeout(time: 10, unit: 'MINUTES') {

                    waitForQualityGate abortPipeline: true
                }
            }
        }


        // ==========================================================
        // VERIFY RESOURCES
        // ==========================================================

        stage('Verify Electron Resources') {
            steps {

                bat '''
                echo ====================================
                echo Checking bundled JRE...
                echo ====================================

                if not exist "electron\\resources\\jre\\bin\\java.exe" (
                    echo ERROR: Bundled Java runtime is missing!
                    exit /b 1
                )

                echo Bundled JRE found.


                echo.
                echo ====================================
                echo Checking backend JAR...
                echo ====================================

                if not exist "backend\\target\\naukri-be.jar" (
                    echo ERROR: Backend JAR is missing!
                    exit /b 1
                )

                echo Backend JAR found.


                echo.
                echo ====================================
                echo Checking frontend artifact...
                echo ====================================

                if not exist "frontend\\dist\\index.html" (
                    echo ERROR: Frontend build artifact is missing!
                    exit /b 1
                )

                echo Frontend artifact found.


                echo.
                echo ====================================
                echo Resource verification successful.
                echo ====================================
                '''
            }
        }


        // ==========================================================
        // ELECTRON PACKAGE
        // ==========================================================

        stage('Electron Package') {
            steps {

                dir('electron') {

                    bat 'npm run dist'
                }
            }
        }


        // ==========================================================
        // ARCHIVE JENKINS ARTIFACTS
        // ==========================================================

        stage('Archive Artifacts') {
            steps {

                archiveArtifacts(
                    artifacts: 'backend/target/*.jar,dist/**/*.exe,dist/**/*.zip',
                    fingerprint: true
                )
            }
        }


        // ==========================================================
        // AZURE LOGIN
        // ==========================================================

        stage('Azure Login') {
            steps {

                bat '''
                echo ====================================
                echo Azure Login using VM Managed Identity
                echo ====================================

                az login --identity

                if errorlevel 1 (
                    echo ERROR: Azure Managed Identity login failed.
                    exit /b 1
                )

                echo Azure login successful.
                '''
            }
        }


        // ==========================================================
        // UPLOAD BACKEND ARTIFACT
        // ==========================================================

        stage('Upload Backend to Azure Storage') {
            steps {

                bat '''
                echo ====================================
                echo Uploading Backend Artifact
                echo ====================================

                az storage blob upload ^
                    --account-name %AZURE_STORAGE_ACCOUNT% ^
                    --container-name %BACKEND_CONTAINER% ^
                    --name naukri-be.jar ^
                    --file "backend\\target\\naukri-be.jar" ^
                    --auth-mode login ^
                    --overwrite true

                if errorlevel 1 (
                    echo ERROR: Backend upload failed.
                    exit /b 1
                )

                echo Backend artifact uploaded successfully.
                '''
            }
        }


        // ==========================================================
        // UPLOAD FRONTEND ARTIFACT
        // ==========================================================

        stage('Upload Frontend to Azure Storage') {
            steps {

                bat '''
                echo ====================================
                echo Uploading Frontend Artifacts
                echo ====================================

                az storage blob upload-batch ^
                    --account-name %AZURE_STORAGE_ACCOUNT% ^
                    --destination %FRONTEND_CONTAINER% ^
                    --source "frontend\\dist" ^
                    --auth-mode login ^
                    --overwrite

                if errorlevel 1 (
                    echo ERROR: Frontend upload failed.
                    exit /b 1
                )

                echo Frontend artifacts uploaded successfully.
                '''
            }
        }


        // ==========================================================
        // DEPLOY
        // ==========================================================

        stage('Deploy') {
            steps {

                bat '''
                echo ====================================
                echo Starting Local Deployment
                echo ====================================


                if not exist "%DEPLOY_DIR%" (
                    mkdir "%DEPLOY_DIR%"
                )


                echo Removing old deployment files...

                del /Q "%DEPLOY_DIR%\\*.exe" 2>NUL
                del /Q "%DEPLOY_DIR%\\*.jar" 2>NUL


                echo.
                echo Copying backend JAR...

                copy /Y backend\\target\\*.jar "%DEPLOY_DIR%\\"

                if errorlevel 1 (
                    echo ERROR: Backend JAR deployment failed.
                    exit /b 1
                )


                echo.
                echo Copying Electron EXE files...

                copy /Y dist\\*.exe "%DEPLOY_DIR%\\"

                if errorlevel 1 (
                    echo ERROR: Electron EXE deployment failed.
                    exit /b 1
                )


                echo.
                echo ====================================
                echo DEPLOYED FILES
                echo ====================================

                dir "%DEPLOY_DIR%"


                echo.
                echo ====================================
                echo Deployment completed successfully
                echo ====================================
                '''
            }
        }
    }


    // ==========================================================
    // POST ACTIONS
    // ==========================================================

    post {

        success {

            echo '''
            ====================================
            CI/CD PIPELINE COMPLETED SUCCESSFULLY
            ====================================
            '''
        }

        failure {

            echo '''
            ====================================
            PIPELINE FAILED
            ====================================
            '''
        }

        always {

            cleanWs()
        }
    }
}
