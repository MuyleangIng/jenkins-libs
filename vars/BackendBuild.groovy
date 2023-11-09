def call(Map params) {
    sh "${params.gradleHome}/bin/gradle --version"
    sh "${params.gradleHome}/bin/gradle clean build"
    sh 'ls -lrt'

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
}

def writeDockerfile(){
    def dockerfile = libraryResource('docker/grade.dockerfile')
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