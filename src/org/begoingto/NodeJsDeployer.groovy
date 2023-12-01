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

    def deploy(String sourceUrl, String branch, String projectType) {

        // script.echo "Starting deployment of Next.js app from ${sourceUrl} on branch ${branch}"

        // Use the defined Node.js tool
        def nodeHome = steps.tool(name: this.nodeVersion)

        script.withEnv(["PATH+NODE=${nodeHome}/bin"]) {
            // check node version
            // steps.sh "node --version"
            // steps.sh "npm --version"
            script.echo "Project type: ${projectType} 💯✅"
            steps.sh "npm install"
            steps.sh "npm run build"
        }

        // Create Dockerfile
        createDockerfile(projectType)
    }

    // private checkFileExists(String fileName) {
    //     def file = new File(script.pwd(), fileName)
    //     return file.exists()
    // }

    private createDockerfile(String projectType){
        def dockerfileContent = ""
        if(projectType == 'react'){
            def buildPath = script.fileExists('vite.config.js') ? 'dist' : 'build'
            dockerfileContent = """
            FROM nginx:1.23.2
            RUN rm -rf /usr/share/nginx/html/*
            COPY ./%s/ /usr/share/nginx/html 
            EXPOSE 80 
            CMD ["nginx", "-g", "daemon off;"]
            """.formatted(buildPath)

            steps.writeFile(file: 'Dockerfile', text: dockerfileContent)

        }else if(projectType == 'next') {
            dockerfileContent = """
            # Yser oficial nodejs runtime as a parent image
            FROM node:18-alpine AS builder
            
            # set app working directory
            WORKDIR /app
            
            #copy package.json and package-lock.json file to the container
            COPY package*.json ./
            
            # install dependencies the same npm i and install only production dependencies
            RUN npm ci --only=production
            
            # Copy the rest
            COPY . .
            
            # build app for production with minification
            RUN npm run build
            
            # remove dev dependencies
            RUN npm prune --production
            
            # using lightweight alpine image for final image
            FROM node:18-alpine
            
            # set app working directory /app
            WORKDIR /app
            
            # copy the app from builder stage
            COPY --from=builder /app/.next ./.next
            COPY --from=builder /app/public ./public
            COPY --from=builder /app/node_modules ./node_modules
            COPY --from=builder /app/package.json ./package.json
            # start the application
            CMD ["npm", "start"]
            """
            steps.writeFile(file: 'Dockerfile', text: dockerfileContent)
        }else if(projectType == 'angular') {
            dockerfileContent = """
            FROM nginx:alpine
            RUN rm -rf /usr/share/nginx/html/*
            # Copy the output from Stage 1 to replace the default nginx contents.
            COPY ./dist/* /usr/share/nginx/html/
            # Expose port 80
            EXPOSE 80
            # Start Nginx and keep it running in the foreground
            CMD ["nginx", "-g", "daemon off;"]
            """
            steps.writeFile(file: 'Dockerfile', text: dockerfileContent)
        }else if(projectType == 'nuxt'){
            dockerfileContent = """
            # using lightweight alpine image for final image
            FROM node:18-alpine
            # set app working directory /app
            WORKDIR /app
            # copy the app from builder stage
            COPY ./.output ./.output
            COPY ./public ./public
            COPY ./node_modules ./node_modules
            COPY ./package.json ./package.json
            EXPOSE 3000
            # start the application
            CMD ["node", ".output/server/index.mjs"]
            """	
            steps.writeFile(file: 'Dockerfile', text: dockerfileContent)
        }else{
            echo 'not support project type'	
        }
    }
}
