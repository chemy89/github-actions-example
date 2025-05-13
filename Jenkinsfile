def failureReason = ''
def logFile = 'build.log'

pipeline {
    agent any

    environment {
        MAVEN_HOME = '/opt/maven'
        PATH = "${MAVEN_HOME}/bin:${env.PATH}"
        REMOTE_SSH_PORT = '22'
        REMOTE_USER = 'jeongsejong'
        REMOTE_HOST = 'host.docker.internal'
        DEPLOY_DIR = '/Users/jeongsejong/dev-deploy'
        DEPLOY_FILENAME = 'app.war'
        TOMCAT_WEBAPPS_DIR = '/opt/homebrew/opt/tomcat/libexec/webapps'
        TOMCAT_BIN_DIR = '/opt/homebrew/opt/tomcat/libexec/bin'
        SLACK_CHANNEL = '#jenkins-notification-test'
        SLACK_CREDENTIAL_ID = 'slack-token'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    try {
                        echo "🔍 Git 저장소 체크아웃"
                        checkout scm
                        sh "git rev-parse --abbrev-ref HEAD"
                    } catch (e) {
                        failureReason = 'CHECKOUT'
                        throw e
                    }
                }
            }
        }

        stage('Lint') {
            steps {
                script {
                    echo "🔨 Lint 검증 시작"
                    try {
                        sh "bash -o pipefail -c 'mvn checkstyle:check -Dcheckstyle.config.location=checkstyle.xml -Dcheckstyle.consoleOutput=true -Dcheckstyle.failOnViolation=true | tee ${logFile}'"

                    } catch (e) {
                        failureReason = 'LINT'
                        throw e
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    try {
                        echo "🔨 Maven 빌드 시작"
                        logFile = 'build.log'
                        def status = sh(
                            script: "bash -o pipefail -c 'mvn clean package -DskipTests | tee ${logFile}'",
                            returnStatus: true
                        )
                        if (status != 0) {
                            failureReason = 'BUILD'
                            error("Maven 빌드 실패")
                        }
                        echo "✅ 빌드 완료"
                    } catch (e) {
                        throw e
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def WAR_FILE = "target/*.war"
                    logFile = 'deploy.log'

                    try {
                        switch (env.JOB_NAME) {
                            case 'sample-dev-pipeline': echo "🚀 Dev 환경 배포 진행"; break
                            case 'sample-stage-pipeline': echo "🚀 Stage 환경 배포 진행"; break
                            case 'sample-prod-pipeline': echo "🚀 Prod 환경 배포 진행"; break
                            default:
                                failureReason = 'DEPLOY_INVALID_JOB'
                                error("⚠️ 알 수 없는 Job 이름: ${env.JOB_NAME}")
                        }

                        echo "📦 .war 파일 호스트로 복사"
                        try {
                            sh "bash -o pipefail -c 'scp -o StrictHostKeyChecking=no -P ${REMOTE_SSH_PORT} ${WAR_FILE} ${REMOTE_USER}@${REMOTE_HOST}:${DEPLOY_DIR}/${DEPLOY_FILENAME} | tee ${logFile}'"
                        } catch (e) {
                            failureReason = 'DEPLOY_COPY_FAILED'
                            throw e
                        }

                        echo "🚀 Tomcat 배포 및 재시작"
                        try {
                            sh "bash -o pipefail -c 'ssh -o StrictHostKeyChecking=no -p ${REMOTE_SSH_PORT} ${REMOTE_USER}@${REMOTE_HOST} \"${TOMCAT_BIN_DIR}/shutdown.sh || true; sleep 3; rm -rf ${TOMCAT_WEBAPPS_DIR}/ROOT; cp ${DEPLOY_DIR}/${DEPLOY_FILENAME} ${TOMCAT_WEBAPPS_DIR}/ROOT.war; ${TOMCAT_BIN_DIR}/startup.sh\" | tee ${logFile}'"
                        } catch (e) {
                            failureReason = 'DEPLOY_TOMCAT_FAILED'
                            throw e
                        }

                    } catch (e) {
                        if (!failureReason) {
                            failureReason = 'DEPLOY'
                        }
                        throw e
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                if (!failureReason) {
                    slackSend (
                        channel: "${SLACK_CHANNEL}",
                        color: 'good',
                        message: "✅ 빌드 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        tokenCredentialId: "${SLACK_CREDENTIAL_ID}"
                    )
                }
            }
        }

        failure {
            script {
                def logExcerpt = ''
                try {
                    logExcerpt = sh(
                        script: "tail -n 100 ${logFile} | grep '\\[ERROR\\]' || true",
                        returnStdout: true
                    ).trim()
                } catch (e) {
                    logExcerpt = "⚠️ 로그를 읽을 수 없습니다: ${e.message}"
                }

                def reasonMessage = ''
                switch (failureReason) {
                    case 'CHECKOUT':
                        reasonMessage = '❌ Git 저장소 Checkout 실패'
                        break
                    case 'LINT':
                        reasonMessage = "❌ Lint 검증 실패"
                        break
                    case 'BUILD':
                        reasonMessage = '❌ Maven 빌드 실패'
                        break
                    case 'DEPLOY_COPY_FAILED':
                        reasonMessage = '❌ War 파일 복사 실패'
                        break
                    case 'DEPLOY_TOMCAT_FAILED':
                        reasonMessage = '❌ Tomcat 배포 및 재시작 실패'
                        break
                    case 'DEPLOY_INVALID_JOB':
                        reasonMessage = '❌ 잘못된 Job 이름으로 배포 실패'
                        break
                    case 'DEPLOY':
                        reasonMessage = '❌ 배포 실패'
                        break
                    default:
                        reasonMessage = '❌ 빌드 실패 (원인 미상)'
                }

                def formattedLog = "```" + (logExcerpt ?: "❗ [ERROR] 로그가 없습니다.") + "```"

                slackSend (
                    channel: "${SLACK_CHANNEL}",
                    color: 'danger',
                    message: "${reasonMessage}: ${env.JOB_NAME} #${env.BUILD_NUMBER}\n\n${formattedLog}\n🔗 ${env.BUILD_URL}",
                    tokenCredentialId: "${SLACK_CREDENTIAL_ID}"
                )
            }
        }
    }
}
