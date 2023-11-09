package org.begoingto

class Ansible {
    private Boolean existed
    private final steps
    private final script

    Ansible(steps,script) {
        this.steps = steps
        this.script = script
    }


    def exitst() {
        
        try {
            // Try to execute `ansible --version`
            def output = sh(script: "ansible --version", returnStdout: true).trim()
            echo "Ansible is available: \n${output}"
            return true
        } catch (Exception e) {
            // If an error occurs, it means Ansible is not installed or not in PATH
            echo "Ansible is not available: \n${e.message}"
            return false
        }
    }


    def testScript() {
        def s_domain = libraryResource 'ansible/setup.sh'
        
        writeFile file: 'set_domain.sh', text: s_domain
        sh 'chmod +x set_domain.sh'
        sh './set_domain.sh'
    }
}