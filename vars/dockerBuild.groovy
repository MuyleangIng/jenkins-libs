def call(String dockerfileDirectory, String imageName, String tag) {
    // The script requires Docker Pipeline Plugin to be installed in Jenkins.
    // Checks that the Jenkins instance has Docker available
    if (!dockerAvailable()) {
        error "Docker is not available on this Jenkins instance."
    }

    // Define the full image name with tag
    def fullImageName = "${imageName}:${tag}"

    // Execute the Docker build command
    script {
        docker.withRegistry('https://nd.begoingdev.me/', 'docker-hub-credentials') {
            // Change into the Dockerfile directory
            dir(dockerfileDirectory) {
                // Building the Docker image
                def customImage = docker.build(fullImageName, '--no-cache .')
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
