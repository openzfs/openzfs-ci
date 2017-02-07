job('regression-steps/04-nits') {
    concurrentBuild(true)

    wrappers {
        colorizeOutput('xterm')
    }

    parameters {
        stringParam('OPENZFSCI_DIRECTORY')

        stringParam('OPENZFS_DIRECTORY')
        stringParam('OPENZFS_BASE_COMMIT')

        stringParam('WORKSPACE')
        nodeParam('BUILDER')
    }

    customWorkspace('${WORKSPACE}')

    environmentVariables {
        env('SH_LIBRARY_PATH', '${OPENZFSCI_DIRECTORY}/jenkins/sh/library')
        env('OPENZFS_DIRECTORY', '${OPENZFS_DIRECTORY}')
        env('BASE_COMMIT', '${OPENZFS_BASE_COMMIT}')
    }

    steps {
        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/nightly-nits/nightly-nits.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
