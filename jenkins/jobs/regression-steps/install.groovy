job('regression-steps/05-install') {
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
        env('INSTALL_DEBUG', 'yes')
    }

    steps {
        shell('${OPENZFSCI_DIRECTORY}/jenkins/sh/nightly-install/nightly-install.sh')
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
