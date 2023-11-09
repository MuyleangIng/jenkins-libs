def call() {
    def ansible = new org.begoingto.Ansible(this,script)
    ansible.testScript()
    def exist = ansible.exitst()
    echo "Ansible is available: ${exist}"
}