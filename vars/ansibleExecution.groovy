import org.begoingto.Ansible

def call() {
    def ansible = new Ansible(this,script)
    
    ansible.testScript()
    def exist = ansible.exitst()
    echo "Ansible is available: ${exist}"
}