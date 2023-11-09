package org.begoingto
import org.begoingto.Notification

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

        // steps.sh 'ls -lrt'
        // steps.sh 'cat hosts.ini'

        // steps.echo "Params: registryName: ${params.registryName}, imageName: ${params.imageName}, tag: ${params.tag}, portExpose: ${params.portExpose}, portOut: ${params.portOut}"

        // execute ansible playbook
        ansibleShExecute(params.registryName, params.imageName, params.tag, params.portExpose, params.portOut)

    }

    def setupDomainName(Map params){
        int port = params.targetPort.toInteger()
        steps.echo "------------setup nginx config------------"
        writeNginxConfig(params.domainName, port)
        steps.echo "------------end nginx config------------"

        steps.echo "------------setup domain------------"
        ansibleSetupDomain(params.domainName)
        steps.echo "------------end domain------------"
    }


    private ansibleShExecute(String registryName, String imageName, String tag, String portExpose, String portOut) {
        def playbookContent = steps.libraryResource('ansible/deploy.yml')
        steps.writeFile(file: 'deploy.yml', text: playbookContent)
        steps.echo "-------------- Execute Ansible playbook --------------"
        steps.sh """
        ansible-playbook -i hosts.ini deploy.yml -e "image_name=${imageName} image_tag=${tag} registry_username=${USERNAME} registry_password=${PASSWORD} registry_url=${registryName} container_name=${imageName} port_expose=${portExpose} port_out=${portOut}"
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

    private writeNginxConfig(String domainName, int targetPort){
        steps.echo "-------------- Write Nginx config --------------"	
        def nginxContent = """
        server {
            server_name ${domainName} www.${domainName};

            location / {
                include /etc/nginx/proxy_params;
                proxy_pass http://localhost:${targetPort};
                proxy_redirect http://localhost:${targetPort} https://${domainName};
            }

            listen 443 ssl http2;
            listen [::]:443 ssl http2;
            ssl_certificate /etc/ssl/begoingdev.me/cert.pem;
            ssl_certificate_key /etc/ssl/begoingdev.me/private.pem;
        }
        
        server {
            if (\$host = ${domainName}) {
                return 301 https://\$host\$request_uri;
            }

            listen 80;
            server_name ${domainName} www.${domainName};
            return 404;
        }
        """
        steps.writeFile(file: domainName, text: nginxContent)
        // steps.sh "cat ${domainName}"
        steps.echo "-------------- End Nginx config --------------"
    }

    private ansibleSetupDomain(String domainName){
        def playbookContent = steps.libraryResource('ansible/domain.yml')
        steps.writeFile(file: 'domain.yml', text: playbookContent)
        steps.echo "-------------- Execute Ansible playbook Domain --------------"
        steps.sh """
        ansible-playbook -i hosts.ini domain.yml -e "domain_name=${domainName}"
        """
        steps.echo "-------------- End Ansible playbook Domain --------------"
        def notify = new Notification(steps, this)
        notify.sendTelegram("Deploy successfully(✅⚜)(❁´◡`❁) Domain: ${domainName}")
    }
}