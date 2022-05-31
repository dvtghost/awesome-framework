pipeline {
    agent any
    tools {
        jdk 'openjdk-11'
    }
    stages {
        stage('Build app'){
            steps {
                script{
                    runBuild = isRunBuild();
                    triggerUser = getBuildUser()
                    if (triggerUser == "") {
                        def submitter = sh(script: '''git --no-pager show -s --format='%an' $GIT_COMMIT''', returnStdout: true)
                        println submitter
                        echo "Code commited by ${triggerUser}"
                        if (runBuild){
                            notifyBuild('STARTED', submitter)
                        } else {
                            notifyCommit(submitter)
                        }
                    } else {
                        echo "Job started by ${triggerUser}"
                        if (runBuild){
                            notifyBuild('STARTED', triggerUser)
                        } else {
                            notifyCommit(triggerUser)
                        }
                    }
                }
                sh '''
                    pwd
                    ls
                    java -version
                '''
//                 withMaven(maven: 'maven-3.8.1'){
//                     sh "mvn clean package -Pproduction"
//                 }
            }
        }
        stage('Start docker'){
            steps {
                sh '''
                    sudo docker-compose -f docker/docker-compose-webapp.yml up -d --build
                '''
            }
        }
    }
    post {
        failure{
            script{
                currentBuild.result = "FAILURE"
            }
        }
        cleanup{
            script{
                notifyBuild(currentBuild.result)
            }
        }
    }
 }

def isRunBuild() {
    return getCommitMsg().contains("into develop");
}

def getBuildUser() {
        try {
            return currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserName()
        } catch (e) {
            return ""
        }
    }

def getCommitMsg() {
    def commitMsg = sh(script: 'git log -1 --pretty="%s"', returnStdout: true)
    commitMsg = commitMsg.replaceAll("'", "");
    return commitMsg;
}

def getSlackChannelName() {
    return "#đừng-ai-vào"
}
def getDiscordWebhookURL() {
    return "https://discord.com/api/webhooks/867797614819934228/TzzBeiXuibm2DZ6JClMLb6jnsDcIFF4TmeVeYJr9Ku9kkoxFpDON78zvbLN55CddTFkh"
}

def notifyBuild(String buildStatus, String name = ''){
    def commitMsg = getCommitMsg()
    def subject = "${buildStatus}: Job <${env.BUILD_URL}/console|${env.JOB_NAME} #${env.BUILD_NUMBER}> by name\n ${commitMsg}"
    def colorName = 'green'

    def discordSubject = "${buildStatus}: Job  #${env.BUILD_NUMBER} by name"
    def discordLink = "${env.BUILD_URL}/console|${env.JOB_NAME}"
    def discordDesc = "${commitMsg}"

    def buildSuccessIcons = [" :100:", " :heart_eyes_cat:", " :sunglasses:"]
    def randomIndex = (new Random()).nextInt(buildSuccessIcons.size())

    if (name == '') {
        subject = subject.replaceAll("by name", "")
        discordSubject = discordSubject.replaceAll("by name", "")
    } else {
        subject = subject.replaceAll("name", name)
        discordSubject = discordSubject.replaceAll("name", name)
    }

    if (buildStatus == 'FAILURE') {
        colorName = 'red'
        subject = subject + ":interrobang:"
    } else if (buildStatus == 'SUCCESS'){
        subject = subject.replaceAll("/console", "")
        subject = subject + "${buildSuccessIcons[randomIndex]}"
        discordDesc = discordDesc + "${buildSuccessIcons[randomIndex]}"
    } else {
        subject = subject.replaceAll("/console", "")
    }

    slackSend (channel: getSlackChannelName(), failOnError: true, color: colorName, message: subject)
    discordSend(title: discordSubject, link: discordLink, description: discordDesc, result: buildStatus,webhookURL: getDiscordWebhookURL())
}

def notifyCommit(String name = ''){
    def commitMsg = getCommitMsg()

    def subject = "STARTED: Job <${env.BUILD_URL}/console|${env.JOB_NAME} #${env.BUILD_NUMBER}> by name ${commitMsg}"
    def colorName = 'green'

    def discordSubject = "STARTED: Job  #${env.BUILD_NUMBER} by name"
    def discordLink = "${env.BUILD_URL}/console|${env.JOB_NAME}"
    def discordDesc = "${commitMsg}"

    def buildSuccessIcons = [" :100:", " :heart_eyes_cat:", " :sunglasses:"]
    def randomIndex = (new Random()).nextInt(buildSuccessIcons.size())

    if (name == '') {
        subject = subject.replaceAll("by name", "")
        discordSubject = discordSubject.replaceAll("by name", "")
    } else {
        subject = subject.replaceAll("name", name)
        discordSubject = discordSubject.replaceAll("name", name)
    }

    slackSend (channel: getSlackChannelName(), failOnError: true, color: colorName, message: subject)
    discordSend(title: discordSubject, link: discordLink, description: discordDesc, result: 'SUCCESS', webhookURL: getDiscordWebhookURL())
}

//======

