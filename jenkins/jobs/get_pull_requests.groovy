pipelineJob('get-pull-requests') {
    concurrentBuild(false)

    properties {
        rebuild {
            autoRebuild()
        }
    }

    parameters {
        stringParam('OPENZFSCI_REPOSITORY', System.getenv('OPENZFSCI_REPOSITORY'))
        stringParam('OPENZFSCI_BRANCH', System.getenv('OPENZFSCI_BRANCH'))
        stringParam('OPENZFSCI_DIRECTORY', 'openzfs-ci')

        stringParam('OPENZFS_REPOSITORY', 'openzfs/openzfs')

        choiceParam('PULL_REQUEST_VARIANT', ['head', 'merge', 'all'])
        choiceParam('TESTS_ENABLED', ['yes', 'no'])
    }

    if (System.getenv('OPENZFSCI_PRODUCTION')) {
        triggers {
            cron('H/5 * * * *')
        }
    }

    environmentVariables {
        env('COMMIT_STATUS_ENABLED', 'yes')
    }

    definition {
        cps {
            script(readFileFromWorkspace('jenkins/jobs/pipelines/get_pull_requests.groovy'))
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
