job('regression-steps/09-zloop') {
    concurrentBuild(true)

    wrappers {
        colorizeOutput('xterm')
    }

    parameters {
        stringParam('OPENZFSCI_REPOSITORY')
        stringParam('OPENZFSCI_BRANCH')
        stringParam('OPENZFSCI_DIRECTORY')

        nodeParam('ZLOOP_TESTER')
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
        env('RUN_TIME', '9000')
        env('ENABLE_WATCHPOINTS', 'no')
    }

    steps {
        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/run-zloop/run-zloop.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
