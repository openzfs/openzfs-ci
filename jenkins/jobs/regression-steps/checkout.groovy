job('regression-steps/02-checkout') {
    concurrentBuild(true)

    parameters {
        stringParam('OPENZFSCI_REPOSITORY')
        stringParam('OPENZFSCI_BRANCH')
        stringParam('OPENZFSCI_DIRECTORY')

        stringParam('OPENZFS_REPOSITORY')
        stringParam('OPENZFS_COMMIT')
        stringParam('OPENZFS_DIRECTORY')

        nodeParam('BUILDER')
    }

    multiscm {
        git {
            remote {
                name('origin')
                github('${OPENZFSCI_REPOSITORY}')
            }

            branch('${OPENZFSCI_BRANCH}')

            extensions {
                relativeTargetDirectory('${OPENZFSCI_DIRECTORY}')
            }
        }

        git {
            remote {
                name('origin')
                github('${OPENZFS_REPOSITORY}')
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }

            branch('${OPENZFS_COMMIT}')

            extensions {
                relativeTargetDirectory('${OPENZFS_DIRECTORY}')
                cleanBeforeCheckout()
            }
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
