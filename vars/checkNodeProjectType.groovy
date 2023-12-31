// vars/checkProjectType.groovy

def call() {
	
	// Attempt to read package.json file
	def packageJson = null
	try {
		packageJson = readPackageJson()
		echo "package.json ${packageJson}"
	} catch (Exception e) {
		echo "Failed to read or parse package.json: ${e.getMessage()}"
		return 'Unknown'
	}

	 // Ensure packageJson and its dependencies or devDependencies are not null
	if (packageJson == null || (packageJson.dependencies == null && packageJson.devDependencies == null)) {
		echo "No package.json found or dependencies/devDependencies are not defined."
		return 'Unknown'
	}

	// Check for key dependencies to determine project type
	def projectType = 'Unknown'
	if (hasDependency(packageJson, 'next')) {
		projectType = 'next'
	} else if (hasDependency(packageJson, 'react')) {
		projectType = 'react'
	} else if (hasDependency(packageJson, 'vue') || hasDependency(packageJson, 'nuxt')) {
		projectType = hasDependency(packageJson, 'nuxt') ? 'nuxt' : 'vue'
	} else if (hasDependency(packageJson, '@angular/core')) {
		projectType = 'angular'
	}

	echo "The project is of type: ${projectType}"
	return projectType
}

private readPackageJson() {
	def packageJsonText = readFile 'package.json'
	def jsonSlurper = new groovy.json.JsonSlurper()
	return jsonSlurper.parseText(packageJsonText)
}

private boolean hasDependency(def packageJson, String key) {
  // Check both dependencies and devDependencies for the specified key
  return (packageJson.dependencies?.containsKey(key) || packageJson.devDependencies?.containsKey(key))
}