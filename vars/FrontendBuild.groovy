import org.begoingto.Notification

def call(Map params) {
    try {
        withCredentials([usernamePassword(credentialsId: env.REGISTRY_CREDENTIALS_ID, passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
            // docker build
            def imageFull = dockerBuild(username: USERNAME, 
                imageName: params.imageName, 
                tag: params.tag,
                registryName: env.REGISTRY_NAME    
            )
            // docker push
            if(env.REGISTRY_NAME == 'docker.io'){
                sh "docker login -u ${USERNAME} -p ${PASSWORD}"
                sh "docker push ${imageFull}"
            }else{
                sh "docker login -u ${USERNAME} -p ${PASSWORD} ${env.REGISTRY_NAME}"
                sh "docker push ${imageFull}"
            }
        }
    } catch (Exception e) {
        def notify = new Notification(steps, this)
        notify.sendTelegram("Build failed⛔(<:>) Error: ${e.getMessage()}")
        echo "Build failed⛔(<:>) Error: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
        throw e
    }
    
}

def String dockerBuild(Map params){
    def dockerImage = "${params.username}/${params.imageName}:${params.tag}"
    if(env.REGISTRY_NAME != 'docker.io'){
        dockerImage = "${env.REGISTRY_NAME}/${params.imageName}:${params.tag}"
    }
    sh "docker build -t ${dockerImage} ."
    return dockerImage
}