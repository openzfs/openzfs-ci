pipelineJob('00-regression-tests') {
    concurrentBuild(true)
    throttleConcurrentBuilds {
        maxTotal 8
    }

    parameters {
        stringParam('OPENZFSCI_REPOSITORY', System.getenv('OPENZFSCI_REPOSITORY'))
        stringParam('OPENZFSCI_BRANCH', System.getenv('OPENZFSCI_BRANCH'))
        stringParam('OPENZFSCI_DIRECTORY', 'openzfs-ci')

        stringParam('OPENZFS_REPOSITORY', System.getenv('OPENZFS_REPOSITORY'))
        stringParam('OPENZFS_DIRECTORY', 'openzfs')

        stringParam('OPENZFS_COMMIT')
        stringParam('OPENZFS_COMMIT_BASE')
        stringParam('OPENZFS_PULL_NUMBER')

        choiceParam('COMMIT_STATUS_ENABLED', ['no', 'yes'])
    }

    environmentVariables {
        env('DCENTER_HOST', 'dcenter')
        env('DCENTER_IMAGE', 'omnios-r151020')
        env('DCENTER_EXPIRATION', '1')

        env('MANTA_DIRECTORY_PREFIX', 'test-results')
    }

    definition {
        cps {
            script(readFileFromWorkspace('jenkins/pipelines/regression_tests.groovy'))
            sandbox()
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
