job('regression-steps/08-create-testers') {
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
        stringParam('BUILDER_SNAPSHOT')

        stringParam('ZLOOP_TESTER')
        stringParam('ZFSTEST_TESTER')
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
        env('DCENTER_SNAPSHOT', '${BUILDER_SNAPSHOT}')

        env('ROLES', 'openzfs.build-slave openzfs.jenkins-slave')
        env('WAIT_FOR_SSH', 'yes')
    }

    steps {
        environmentVariables {
            env('DCENTER_GUEST', '${BUILDER}')
            env('DCENTER_CLONE', '${ZLOOP_TESTER}')
        }

        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/dcenter-clone/dcenter-clone.sh')

        environmentVariables {
            env('DCENTER_GUEST', '${ZLOOP_TESTER}')
            env('EXTRA_VARS', 'jenkins_slave_name=${ZLOOP_TESTER} jenkins_master_url=${JENKINS_URL}')
        }

        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/ansible-deploy-roles/ansible-deploy-roles.sh')

        environmentVariables {
            env('DCENTER_GUEST', '${BUILDER}')
            env('DCENTER_CLONE', '${ZFSTEST_TESTER}')
        }

        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/dcenter-clone/dcenter-clone.sh')

        environmentVariables {
            env('DCENTER_GUEST', '${ZFSTEST_TESTER}')
            env('EXTRA_VARS', 'jenkins_slave_name=${ZFSTEST_TESTER} jenkins_master_url=${JENKINS_URL}')
        }

        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/ansible-deploy-roles/ansible-deploy-roles.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
