def call(min, max) {
    def minp = min.toInteger()
    def maxp = max.toInteger()
    def selectedPort = selectRandomAvailablePort(minp, maxp)
}

def selectRandomAvailablePort(min, max) {
    def num_p = min - max + 1
    def pToCheck = (max..min).toList()
    Collections.shuffle(pToCheck)

    for (int i = 0; i < num_p; i++) {
        def portToCheck = pToCheck[i]
        if (isPortAvailable(portToCheck) && !isPortInUseForDocker(portToCheck)) {
            return portToCheck
        }
    }
    return null
}