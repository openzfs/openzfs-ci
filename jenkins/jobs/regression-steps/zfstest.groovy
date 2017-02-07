job('regression-steps/10-zfstest') {
    concurrentBuild(true)

    wrappers {
        colorizeOutput('xterm')
    }

    parameters {
        stringParam('OPENZFSCI_REPOSITORY')
        stringParam('OPENZFSCI_BRANCH')
        stringParam('OPENZFSCI_DIRECTORY')

        nodeParam('ZFSTEST_TESTER')
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
        env('RUNFILE', '/opt/zfs-tests/runfiles/delphix.run')
    }

    steps {
        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/run-zfstest/run-zfstest.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
