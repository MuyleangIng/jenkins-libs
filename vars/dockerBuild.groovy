def call(String dockerfileDirectory, String imageName, String tag) {
    // The script requires Docker Pipeline Plugin to be installed in Jenkins.
    // Checks that the Jenkins instance has Docker available
    if (!dockerAvailable()) {
        error "Docker is not available on this Jenkins instance."
    }

    // Define the full image name with tag
    def fullImageName = "nd.begoingdev.me/${imageName}:${tag}"

    // Execute the Docker build command
    script {
        withDockerRegistry(credentialsId: 'nd.begoingdev.me', url: 'https://nd.begoingdev.me/') {
            // Change into the Dockerfile directory
            dir(dockerfileDirectory) {
                // Building the Docker image
                def customImage = docker.build(fullImageName, '--no-cache .')
                sh "docker images"
            }
        }
    }

    echo "Successfully built Docker image ${fullImageName}"
}

private boolean dockerAvailable() {
    try {
        def output = script.sh(script: 'docker --version', returnStdout: true).trim()
        echo "Docker version: ${output}"
        return true
    } catch (Exception e) {
        echo "Docker not available: ${e.getMessage()}"
        return false
    }
}
