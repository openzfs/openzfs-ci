if (!OPENZFS_REPOSITORY)
    error('Empty OPENZFS_REPOSITORY parameter.')
else
    currentBuild.displayName = "#${env.BUILD_NUMBER} ${OPENZFS_REPOSITORY}"

node('master') {
    def common = null
    stage('setup') {
        checkout([$class: 'GitSCM', changelog: false, poll: false,
                  userRemoteConfigs: [[name: 'origin', url: "https://github.com/${OPENZFSCI_REPOSITORY}"]],
                  branches: [[name: OPENZFSCI_BRANCH]],
                  extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: OPENZFSCI_DIRECTORY]]])
        common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")
    }

    stage('send mails') {
        common.openzfscish('openzfs-ci', 'send-illumos-mails', false, [
            ['REPOSITORY', OPENZFS_REPOSITORY],
        ])
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
