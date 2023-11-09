package org.begoingto

class Ansible {
    def steps
    def script
    private String USERNAME 
    private String PASSWORD

    Ansible(steps,script, String username, String password) {
        this.steps = steps
        this.script = script
        this.USERNAME = username
        this.PASSWORD = password
    }


    def exitst() {
        
        try {
            // Try to execute `ansible --version`
            def output = steps.sh(script: "ansible --version", returnStdout: true).trim()
            steps.echo "Ansible is available: \n${output}"
            return true
        } catch (Exception e) {
            // If an error occurs, it means Ansible is not installed or not in PATH
            steps.echo "Ansible is not available: \n${e.message}"
            return false
        }
    }


    def testScript() {
        def s_domain = steps.libraryResource('ansible/setup.sh')
        
        steps.writeFile(file: 'set_domain.sh', text: s_domain)
        steps.sh 'chmod +x set_domain.sh'
        steps.sh './set_domain.sh'
    }


    def ansiblePlaybook(Map params){
        /***
         String hostIp,
                        String hostname,
                        String hostuser,
                        String registryName,
                        String imageName, 
                        String tag, 
                        int port_expose,
                        int port_out,
                        String credentialsId
        **/

        // write ansible hosts
        writeAnsibleHosts(params.hostIp, params.hostname, params.hostuser)

        steps.sh 'ls -lrt'
        steps.sh 'cat hosts.ini'

        steps.echo "Params: registryName: ${params.registryName}, imageName: ${params.imageName}, tag: ${params.tag}, portExpose: ${params.portExpose}, portOut: ${params.portOut}"

        // execute ansible playbook
        ansibleShExecute(params.registryName, params.imageName, params.tag, params.portExpose, params.portOut)

    }


    private ansibleShExecute(String registryName, String imageName, String tag, String portExpose, String portOut) {
        def playbookContent = steps.libraryResource('ansible/deploy.yml')
        steps.writeFile(file: 'playbook.yml', text: playbookContent)
        steps.sh 'cat playbook.yml'
        steps.echo "-------------- Execute Ansible playbook --------------"
        steps.sh """
        ansible-playbook -i hosts.ini playbook.yml -e "image_name=${imageName} image_tag=${tag} registry_username=${USERNAME} registry_password=${PASSWORD} registry_url=${registryName} container_name=${imageName} port_expose=${portExpose} port_out=${portOut}"
        """
        steps.echo "-------------- End Ansible playbook --------------"
    }

    private ansiblePluginExecute(String registry_name,
                        String imageName, 
                        String tag
        ) {
        steps.ansiblePlaybook(becomeUser: null, 
                inventory: '/mnt/d/istad/_devops/ansible/deployment/inventories/hosts.ini', 
                playbook: '/mnt/d/istad/_devops/ansible/deployment/playbooks/container/setup.yml', 
                sudoUser: null, 
                vaultCredentialsId: 'container-setup',
                extraVars: [
                    "registry_username": "${USERNAME}",
                    "registry_password": "${PASSWORD}",
                    "container_name": "${imageName}",
                    "registry_url": "${registry_name}",
                    "image_name": "${imageName}",
                    "port_expose": "${port_expose}"
                ])
    }

    
    private String getImage(String registry_name,String imageName, String tag) {
        return "${registry_name}/${imageName}:${tag}"
    }

    private writeAnsibleHosts(String hostIp, String hostname, String hostuser) {
        def hostsContent = """
        [all]
        ${hostname} ansible_host=${hostIp}
        [all:vars]
        ansible_user=${hostuser}
        ansible_host_key_checking=False
        """
        steps.writeFile(file: 'hosts.ini', text: hostsContent)
    }
}