import org.begoingto.Notification

def call(Map params) {
    try {
        def gradleHome = tool env.GRADLE_VERSION
        def commandBuild = 'gradle clean build'
        if(env.PACKAGE_TYPE != 'Gradle'){
            gradleHome = tool env.MAVEN_VERSION
            commandBuild= 'mvn clean package'
        }
        sh "${gradleHome}/bin/${commandBuild}"
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
    def dockerfile = libraryResource("docker/gradle.Dockerfile")
    if(env.PACKAGE_TYPE != 'Gradle'){
        dockerfile = libraryResource("docker/maven.Dockerfile")
    }
    writeFile(file: 'Dockerfile', text: dockerfile)
}

def String dockerBuild(Map params){
    def dockerImage = "${params.username}/${params.imageName}:${params.tag}"
    if(params.registryName != 'docker.io'){
        dockerImage = "${params.registryName}/${params.imageName}:${params.tag}"
    }
    sh 'cat Dockerfile'
    sh "docker build -t ${dockerImage} ."
    return dockerImage
}