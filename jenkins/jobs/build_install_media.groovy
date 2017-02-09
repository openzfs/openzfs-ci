pipelineJob('build-install-media') {
    parameters {
        stringParam('OPENZFSCI_REPOSITORY', System.getenv('OPENZFSCI_REPOSITORY'))
        stringParam('OPENZFSCI_BRANCH', System.getenv('OPENZFSCI_BRANCH'))
        stringParam('OPENZFSCI_DIRECTORY', 'openzfs-ci')

        stringParam('OPENZFS_REPOSITORY', 'openzfs/openzfs')
        stringParam('OPENZFS_BRANCH', 'master')
        stringParam('OPENZFS_DIRECTORY', 'openzfs')
    }

    environmentVariables {
        env('DCENTER_HOST', 'dcenter')
        env('DCENTER_IMAGE', 'oi-hipster')
        env('DCENTER_EXPIRATION', '1')

        env('MANTA_DIRECTORY_PREFIX', 'install-media')
        env('MEDIA_DIRECTORY', '/rpool/dc/media')
    }

    definition {
        cps {
            script(readFileFromWorkspace('jenkins/pipelines/build_install_media.groovy'))
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
