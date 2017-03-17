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

        stringParam('OPENZFS_REPOSITORY', System.getenv('OPENZFS_REPOSITORY'))

        choiceParam('PULL_REQUEST_VARIANT', ['head', 'merge', 'all'])
        choiceParam('TESTS_ENABLED', ['yes', 'no'])
    }

    if (System.getenv('OPENZFSCI_PRODUCTION').toBoolean()) {
        triggers {
            cron('H/5 * * * *')
        }
    }

    environmentVariables {
        if (System.getenv('OPENZFSCI_PRODUCTION').toBoolean()) {
            env('COMMIT_STATUS_ENABLED', 'yes')
        } else {
            env('COMMIT_STATUS_ENABLED', 'no')
        }
    }

    definition {
        cps {
            script(readFileFromWorkspace('jenkins/pipelines/get_pull_requests.groovy'))
            sandbox()
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
