pipelineJob('send-illumos-mails') {
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
    }

    if (System.getenv('OPENZFSCI_PRODUCTION').toBoolean()) {
        triggers {
            cron('H/5 * * * *')
        }
    }

    definition {
        cps {
            script(readFileFromWorkspace('jenkins/pipelines/send_illumos_mail.groovy'))
            sandbox()
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
