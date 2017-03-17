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

        stringParam('REPOSITORY', System.getenv('OPENZFS_REPOSITORY'))
        stringParam('BRANCH', System.getenv('OPENZFS_BRANCH'))
        stringParam('DIRECTORY', 'openzfs')
    }

    if (System.getenv('OPENZFSCI_PRODUCTION').toBoolean()) {
        triggers {
            scm('@hourly')
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
            script(readFileFromWorkspace('jenkins/pipelines/get_commits.groovy'))
            sandbox()
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
