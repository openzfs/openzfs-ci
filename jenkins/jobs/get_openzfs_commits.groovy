pipelineJob('get-openzfs-commits') {
    concurrentBuild(false)

    properties {
        rebuild {
            autoRebuild()
        }
    }

    parameters {
        stringParam('OPENZFSCI_REPOSITORY', System.getenv('OPENZFSCI_REPOSITORY'))
        stringParam('OPENZFSCI_BRANCH', System.getenv('OPENZFSCI_BRANCH'))

        stringParam('REPOSITORY', 'openzfs/openzfs')
        stringParam('BRANCH', 'master')
        stringParam('DIRECTORY', 'openzfs')
    }

    if (System.getenv('OPENZFSCI_PRODUCTION')) {
        triggers {
            scm('@hourly')
        }
    }

    environmentVariables {
        env('COMMIT_STATUS_ENABLED', 'yes')
    }

    definition {
        cps {
            script(readFileFromWorkspace('jenkins/jobs/pipelines/get_commits.groovy'))
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
