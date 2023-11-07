package org.begoingto

class NextJsDeployer {

    def steps
    def script
    def nodeVersion

    NextJsDeployer(steps, script, nodeVersion) {
        this.steps = steps
        this.script = script
        this.nodeVersion = nodeVersion
    }

    def deploy(String sourceUrl, String branch) {

        script.echo "Starting deployment of Next.js app from ${sourceUrl} on branch ${branch}"

        // Use the defined Node.js tool
        def nodeHome = steps.tool(name: this.nodeVersion)

        script.withEnv(["PATH+NODE=${nodeHome}/bin"]) {
            // check node version
            steps.sh "node --version"
            steps.sh "npm --version"
        }
    }
}
