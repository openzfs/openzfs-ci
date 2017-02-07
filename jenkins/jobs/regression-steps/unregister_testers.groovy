job('regression-steps/11-unregister-testers') {
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
        stringParam('DCENTER_EXPIRATION')

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
        env('DCENTER_EXPIRATION', '${DCENTER_EXPIRATION}')
    }

    steps {
        environmentVariables {
            env('DCENTER_GUEST', '${ZLOOP_TESTER}')
        }

        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/dcenter-unregister/dcenter-unregister.sh')

        environmentVariables {
            env('DCENTER_GUEST', '${ZFSTEST_TESTER}')
        }

        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/dcenter-unregister/dcenter-unregister.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
