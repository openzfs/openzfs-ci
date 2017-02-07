job('regression-steps/06-unregister-builder') {
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
        env('DCENTER_EXPIRATION', '${DCENTER_EXPIRATION}')
        env('DCENTER_GUEST', '${BUILDER}')
    }

    steps {
        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/dcenter-unregister/dcenter-unregister.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
