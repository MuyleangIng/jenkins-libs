import org.begoingto.Notification

def call(Map params) {
    try {
        sh "${params.gradleHome}/bin/gradle clean build"
        writeDockerfile()
        withCredentials([usernamePassword(credentialsId: params.registryCredentialsId, passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
            // docker build
            dockerBuild(username: USERNAME, 
                imageName: params.imageName, 
                tag: params.tag,
                registryName: params.registryName    
            )
            // docker push
            if(params.registryName == 'hub.docker.io'){
                sh "docker login -u ${USERNAME} -p ${PASSWORD}"
                sh "docker push ${USERNAME}/${params.imageName}:${params.tag}"
            }else{
                sh "docker login -u ${USERNAME} -p ${PASSWORD} ${params.registryName}"
                sh "docker push ${params.registryName}/${params.imageName}:${params.tag}"
            }
        }
    } catch (Exception e) {
        def notify = new Notification(steps, this)
        notify.sendTelegram(
            botId: params.botId, 
            botToken: params.botToken, 
            chatId: params.chatId, 
            message: "Build failed⛔, Error: ${e.getMessage()}"
        )
        throw e
    }
    
}

def writeDockerfile(){
    def dockerfile = libraryResource('docker/gradle.Dockerfile')
    writeFile(file: 'Dockerfile', text: dockerfile)
}

def String dockerBuild(Map params){
    def dockerImage = "${params.username}/${params.imageName}:${params.tag}"
    if(params.registryName != 'hub.docker.io'){
        dockerImage = "${params.registryName}/${params.imageName}:${params.tag}"
    }
    sh "docker build -t ${dockerImage} ."
    return dockerImage
}