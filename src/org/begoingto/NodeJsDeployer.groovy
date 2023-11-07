package org.begoingto

class NodeJsDeployer {

    def steps
    def script
    def nodeVersion

    NodeJsDeployer(steps, script, nodeVersion) {
        this.steps = steps
        this.script = script
        this.nodeVersion = nodeVersion
    }

    def deploy(String sourceUrl, String branch) {

        script.echo "Starting deployment of Next.js app from ${sourceUrl} on branch ${branch}"

        // Use the defined Node.js tool
        def nodeHome = steps.tool(name: this.nodeVersion)

        script.echo "========= check project type in NodeJsDeployer ============"
        def pType = this.getProjectType()
        script.echo "Project type is: ${pType} 💯✅"

        script.withEnv(["PATH+NODE=${nodeHome}/bin"]) {
            // check node version
            steps.sh "node --version"
            steps.sh "npm --version"
            script.echo "Project Type: ${pType}💻"
        }
    }

    private String getProjectType() {
        // Attempt to read package.json file
        def packageJson = null
        try {
            packageJson = readPackageJson()
            script.echo "package.json ${packageJson}"
        } catch (Exception e) {
            script.echo "Failed to read or parse package.json: ${e.getMessage()}"
            return 'Unknown'
        }

        // Ensure packageJson and its dependencies or devDependencies are not null
        if (packageJson == null || (packageJson.dependencies == null && packageJson.devDependencies == null)) {
            script.echo "No package.json found or dependencies/devDependencies are not defined."
            return 'Unknown'
        }

        // Check for key dependencies to determine project type
        def projectType = 'Unknown'
        if (this.hasDependency(packageJson, 'next')) {
            projectType = 'next'
        } else if (this.hasDependency(packageJson, 'react')) {
            projectType = 'react'
        } else if (this.hasDependency(packageJson, 'vue') || this.hasDependency(packageJson, 'nuxt')) {
            projectType = this.hasDependency(packageJson, 'nuxt') ? 'nuxt' : 'vue'
        } else if (this.hasDependency(packageJson, '@angular/core')) {
            projectType = 'angular'
        }
        return projectType
    }

    private String readPackageJson() {
        def packageJsonText = readFile 'package.json'
        def jsonSlurper = new groovy.json.JsonSlurper()
        return jsonSlurper.parseText(packageJsonText)
    }

    private boolean hasDependency(def packageJson, String key) {
        // Check both dependencies and devDependencies for the specified key
        return (packageJson.dependencies?.containsKey(key) || packageJson.devDependencies?.containsKey(key))
    }
}
