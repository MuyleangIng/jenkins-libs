// vars/checkProjectType.groovy

def call() {
	// Read package.json file
	def packageJson = readPackageJson()

	// Check for key dependencies
	def projectType = 'Unknown'
	if (packageJson.dependencies.'next') {
		projectType = 'Next.js'
	} else if (packageJson.dependencies.'react') {
		projectType = 'React'
	} else if (packageJson.dependencies.'vue' || packageJson.dependencies.'nuxt') {
		projectType = packageJson.dependencies.'nuxt' ? 'Nuxt.js' : 'Vue.js'
	} else if (packageJson.dependencies.'@angular/core') {
		projectType = 'Angular'
	}

	echo "The project is of type: ${projectType}"
	return projectType
}

private readPackageJson() {
	def packageJsonText = readFile 'package.json'
	def jsonSlurper = new groovy.json.JsonSlurper()
	return jsonSlurper.parseText(packageJsonText)
}
