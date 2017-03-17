pipelineJob('open-illumos-sync-pull') {
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

    }

    if (System.getenv('OPENZFSCI_PRODUCTION').toBoolean()) {
        triggers {
            scm('@weekly')
        }
    }

    environmentVariables {
        /*
         * This must be set to "origin" due to the requirements of the "hub" command.
         */
        env('OPENZFS_REMOTE', 'origin')

        env('OPENZFS_REPOSITORY', System.getenv('OPENZFS_REPOSITORY'))
        env('OPENZFS_BRANCH', System.getenv('OPENZFS_BRANCH'))
        env('OPENZFS_DIRECTORY', 'openzfs')

        env('ILLUMOS_REPOSITORY', 'illumos/illumos-gate')
        env('ILLUMOS_REMOTE', 'illumos')
        env('ILLUMOS_BRANCH', 'master')

    }

    definition {
        cps {
            script(readFileFromWorkspace('jenkins/pipelines/open_illumos_sync_pull.groovy'))
            sandbox()
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
