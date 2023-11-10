import org.begoingto.Notification

def call(Map params) {
    try {
        def packageType = env.PACKAGE_TYPE.startsWith("gradle") ? "gradle clean build" : "mvn clean package";
        def gradleHome = tool env.PACKAGE_TYPE
        sh "${gradleHome}/bin/${packageType}"
        writeDockerfile()
        withCredentials([usernamePassword(credentialsId: params.registryCredentialsId, passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
            // docker build
            dockerBuild(username: USERNAME, 
                imageName: params.imageName, 
                tag: params.tag,
                registryName: params.registryName    
            )
            // docker push
            if(params.registryName == 'docker.io'){
                sh "docker login -u ${USERNAME} -p ${PASSWORD}"
                sh "docker push ${USERNAME}/${params.imageName}:${params.tag}"
            }else{
                sh "docker login -u ${USERNAME} -p ${PASSWORD} ${params.registryName}"
                sh "docker push ${params.registryName}/${params.imageName}:${params.tag}"
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

def writeDockerfile(){
    def dockerfile = libraryResource("docker/${env.PACKAGE_TYPE.startsWith('gradle') ? 'gradle': 'maven'}.Dockerfile")
    writeFile(file: 'Dockerfile', text: dockerfile)
}

def String dockerBuild(Map params){
    def dockerImage = "${params.username}/${params.imageName}:${params.tag}"
    if(params.registryName != 'docker.io'){
        dockerImage = "${params.registryName}/${params.imageName}:${params.tag}"
    }
    sh "docker build -t ${dockerImage} ."
    return dockerImage
}