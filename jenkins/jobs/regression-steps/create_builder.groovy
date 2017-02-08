job('regression-steps/01-create-builder') {
    label('master')
    concurrentBuild(true)

    wrappers {
        colorizeOutput('xterm')
    }

    parameters {
        stringParam('OPENZFSCI_REPOSITORY')
        stringParam('OPENZFSCI_BRANCH')
        stringParam('OPENZFSCI_DIRECTORY')

        stringParam('DCENTER_HOST')
        stringParam('DCENTER_IMAGE')

        stringParam('BUILDER')
    }

    multiscm {
        git {
            remote {
                name('origin')
                github('${OPENZFSCI_REPOSITORY}')
            }

            branch('${OPENZFSCI_BRANCH}')

            extensions {
                relativeTargetDirectory('${OPENZFSCI_DIRECTORY}')
            }
        }
    }

    environmentVariables {
        env('SH_LIBRARY_PATH', '${OPENZFSCI_DIRECTORY}/jenkins/sh/library')

        env('DCENTER_HOST', '${DCENTER_HOST}')
        env('DCENTER_IMAGE', '${DCENTER_IMAGE}')
        env('DCENTER_GUEST', '${BUILDER}')

        env('EXTRA_VARS', 'jenkins_slave_name=${BUILDER} jenkins_master_url=${JENKINS_URL}')
        env('ROLES', 'openzfs.build-slave openzfs.jenkins-slave')
        env('WAIT_FOR_SSH', 'yes')
    }

    steps {
        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/dcenter-clone-latest/dcenter-clone-latest.sh')
        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/ansible-deploy-roles/ansible-deploy-roles.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
