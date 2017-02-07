job('regression-steps/03-build') {
    concurrentBuild(true)

    wrappers {
        colorizeOutput('xterm')
    }

    parameters {
        stringParam('OPENZFSCI_DIRECTORY')
        stringParam('OPENZFS_DIRECTORY')

        stringParam('WORKSPACE')
        nodeParam('BUILDER')
    }

    customWorkspace('${WORKSPACE}')

    environmentVariables {
        env('SH_LIBRARY_PATH', '${OPENZFSCI_DIRECTORY}/jenkins/sh/library')
        env('OPENZFS_DIRECTORY', '${OPENZFS_DIRECTORY}')
        env('BUILD_NONDEBUG', 'yes')
        env('BUILD_DEBUG', 'yes')
        env('RUN_LINT', 'yes')
    }

    steps {
        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/nightly-build/nightly-build.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
