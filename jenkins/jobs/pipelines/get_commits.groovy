if (!REPOSITORY)
    error('Empty REPOSITORY parameter.')
else
    currentBuild.displayName = "#${env.BUILD_NUMBER} ${REPOSITORY}"

node('master') {
    stage('setup') {
        checkout([$class: 'GitSCM', changelog: true, poll: true,
                  userRemoteConfigs: [[name: 'origin', url: "https://github.com/${REPOSITORY}"]],
                  branches: [[name: BRANCH]],
                  extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: DIRECTORY]]])
    }

    def commit = null

    stage('get commits') {
        dir(DIRECTORY) {
            commit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
        }
    }

    stage('process commits') {
        if (!commit)
            error('no commit found')

        build(job: '00-regression-tests', propagate: true, quietPeriod: 0, wait: true, parameters: [
            [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
            [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
            [$class: 'StringParameterValue', name: 'OPENZFS_REPOSITORY', value: REPOSITORY],
            [$class: 'StringParameterValue', name: 'OPENZFS_COMMIT', value: commit],
            [$class: 'StringParameterValue', name: 'COMMIT_STATUS_ENABLED', value: env.COMMIT_STATUS_ENABLED],
        ])
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
