import org.apache.commons.lang.RandomStringUtils

currentBuild.displayName = "#${env.BUILD_NUMBER} ${OPENZFS_REPOSITORY} ${OPENZFS_BRANCH}"

node('master') {
    def common = null

    stage('setup master') {
        checkout([$class: 'GitSCM', changelog: false, poll: false,
                  userRemoteConfigs: [[name: 'origin', url: "https://github.com/${OPENZFSCI_REPOSITORY}"]],
                  branches: [[name: OPENZFSCI_BRANCH]],
                  extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: OPENZFSCI_DIRECTORY]]])
        stash(name: 'openzfs-ci', includes: "${OPENZFSCI_DIRECTORY}/**")

        common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")
    }

    /*
     * When running the Jenkins slave on an OpenIndiana based system, the slave will often fail, outputting a
     * "connection timed out" error message. Until this is resolved, we wrap the bulk of this job's logic in a
     * "retry" block; since the failure isn't consistent, this will hopefully allow each invocation of this job
     * to successfully complete, even if it has to retry multiple times to acheive success.
     */
    retry(count: 3) {
        env.DCENTER_GUEST = 'openzfsci-iso-' + RandomStringUtils.randomAlphanumeric(16)

        env.ANSIBLE_ROLES = 'openzfs.build-slave openzfs.jenkins-slave'
        env.ANSIBLE_WAIT_FOR_SSH = 'yes'
        env.ANSIBLE_EXTRA_VARS = "jenkins_slave_name=${env.DCENTER_GUEST} jenkins_master_url=${env.JENKINS_URL}"

        try {
            stage('create vm') {
                common.openzfscish(OPENZFSCI_DIRECTORY, 'dcenter-clone-latest', false, [
                    ['DCENTER_HOST', env.DCENTER_HOST],
                    ['DCENTER_GUEST', env.DCENTER_GUEST],
                    ['DCENTER_IMAGE', env.DCENTER_IMAGE],
                ])
            }

            stage('ansibilize vm') {
                common.openzfscish(OPENZFSCI_DIRECTORY, 'ansible-deploy-roles', false, [
                    ['DCENTER_HOST', env.DCENTER_HOST],
                    ['DCENTER_GUEST', env.DCENTER_GUEST],
                    ['DCENTER_IMAGE', env.DCENTER_IMAGE],
                    ['ROLES', env.ANSIBLE_ROLES],
                    ['WAIT_FOR_SSH', env.ANSIBLE_WAIT_FOR_SSH],
                    ['EXTRA_VARS', env.ANSIBLE_EXTRA_VARS],
                ])
            }

            node(DCENTER_GUEST) {
                stage('setup vm') {
                    parallel('unstash openzfs-ci': {
                        unstash(name: 'openzfs-ci')
                    }, 'checkout openzfs': {
                        checkout([$class: 'GitSCM', changelog: false, poll: false,
                                  userRemoteConfigs: [
                                      [name: 'origin', url: "https://github.com/${OPENZFS_REPOSITORY}"]],
                                  branches: [[name: OPENZFS_BRANCH]],
                                  extensions: [
                                      [$class: 'RelativeTargetDirectory', relativeTargetDir: OPENZFS_DIRECTORY]]])
                    })
                }

                stage('nightly build') {
                    common.openzfscish(OPENZFSCI_DIRECTORY, 'nightly-build', false, [
                        ['OPENZFS_DIRECTORY', OPENZFS_DIRECTORY],
                        ['BUILD_NONDEBUG', 'yes'],
                        ['BUILD_DEBUG', 'no'],
                        ['RUN_LINT', 'no']
                    ])
                }

                stage('iso build') {
                    common.openzfscish(OPENZFSCI_DIRECTORY, 'nightly-iso-build', false, [
                        ['OPENZFS_DIRECTORY', OPENZFS_DIRECTORY],
                        ['INSTALL_DEBUG', 'no']
                    ])
                }
            }

            stage('upload media') {
                retry(count: 3) {
                    common.openzfscish(OPENZFSCI_DIRECTORY, 'upload-remote-install-media-to-manta', false, [
                        ['DCENTER_HOST', env.DCENTER_HOST],
                        ['DCENTER_GUEST', env.DCENTER_GUEST],
                        ['DCENTER_IMAGE', env.DCENTER_IMAGE],
                        ['REMOTE_DIRECTORY', env.MEDIA_DIRECTORY],
                        ['PREFIX', env.MANTA_DIRECTORY_PREFIX],
                        ['REPOSITORY', OPENZFS_REPOSITORY],
                        ['BRANCH', OPENZFS_BRANCH],
                    ])
                }
            }
        } finally {
            stage('unregister vm') {
                common.openzfscish(OPENZFSCI_DIRECTORY, 'dcenter-unregister', false, [
                    ['DCENTER_HOST', env.DCENTER_HOST],
                    ['DCENTER_GUEST', env.DCENTER_GUEST],
                    ['DCENTER_EXPIRATION', env.DCENTER_EXPIRATION],
                ])
            }
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
