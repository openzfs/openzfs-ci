if (!OPENZFS_REPOSITORY)
    error('Empty OPENZFS_REPOSITORY parameter.')
else
    currentBuild.displayName = "#${env.BUILD_NUMBER} ${OPENZFS_REPOSITORY} ${PULL_REQUEST_VARIANT}"

node('master') {
    def common = null
    stage('setup') {
        checkout([$class: 'GitSCM', changelog: false, poll: false,
                  userRemoteConfigs: [[name: 'origin', url: "https://github.com/${OPENZFSCI_REPOSITORY}"]],
                  branches: [[name: OPENZFSCI_BRANCH]],
                  extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: OPENZFSCI_DIRECTORY]]])
        common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")
    }

    def commits = null
    stage('get commits') {
        /*
         * This script will return a list of all the commit shas for pull requests that should be tested for
         * the specified repository. The output will be a list commit shas, each seperated by a newline; i.e.
         * each commit sha is on a seperate line. This makes it easy to consume, as we can then "tokenize" the
         * output, to get a list of commits, and then iterate over this list.
         */
        def output = common.openzfscish('openzfs-ci', 'get-pull-request-commits', true, [
            ['REPOSITORY', OPENZFS_REPOSITORY],
            ['PULL_REQUEST_VARIANT', PULL_REQUEST_VARIANT],
        ])

        commits = output.tokenize('\n')
    }

    stage('process pulls') {
        for (commit in commits) {
            def tokens = commit.tokenize(" ")
            if (TESTS_ENABLED == 'yes') {
                build(job: '00-regression-tests', propagate: false, quietPeriod: 0, wait: false, parameters: [
                    [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
                    [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
                    [$class: 'StringParameterValue', name: 'OPENZFS_REPOSITORY', value: OPENZFS_REPOSITORY],
                    [$class: 'StringParameterValue', name: 'OPENZFS_COMMIT', value: tokens[0]],
                    [$class: 'StringParameterValue', name: 'OPENZFS_COMMIT_BASE', value: tokens[1]],
                    [$class: 'StringParameterValue', name: 'OPENZFS_PULL_NUMBER', value: tokens[2]],
                    [$class: 'StringParameterValue', name: 'COMMIT_STATUS_ENABLED', value: env.COMMIT_STATUS_ENABLED],
                ])
            }
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
