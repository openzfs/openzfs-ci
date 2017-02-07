job('regression-steps/07-snapshot-builder') {
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
        stringParam('BUILDER_SNAPSHOT')
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
        env('DCENTER_GUEST', '${BUILDER}')
        env('DCENTER_SNAPSHOT', '${BUILDER_SNAPSHOT}')
    }

    steps {
        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/dcenter-snapshot/dcenter-snapshot.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
