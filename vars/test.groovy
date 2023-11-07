def call(){
    echo "List of files in the current directory"
    sh 'ls -lrt'
    sh '''
        pwd
        whoami
    '''
}