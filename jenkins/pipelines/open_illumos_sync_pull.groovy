currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.OPENZFS_REPOSITORY}"

node('master') {
    stage('setup') {
        /*
         * The script that's used to open the pull request implicitly assumes the only git remotes that will be
         * contained in the local git repository are "OPENZFS_REMOTE" and "ILLUMOS_REMOTE". Thus, we can't
         * simply use the GitSCM's "WipeWorkspace" extension, as that won't remove any extra remotes that might
         * be contained in the repository.
         *
         * By using "deleteDir", we ensure a new git repository will be generated for each build.
         */
        deleteDir()

        checkout([$class: 'GitSCM', changelog: false, poll: false,
                  userRemoteConfigs: [[name: 'origin', url: "https://github.com/${OPENZFSCI_REPOSITORY}"]],
                  branches: [[name: OPENZFSCI_BRANCH]],
                  extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: OPENZFSCI_DIRECTORY]]])
        common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")

        checkout([$class: 'GitSCM', changelog: true, poll: true,
                  userRemoteConfigs: [[name: "${env.OPENZFS_REMOTE}",
                                       url: "https://github.com/${env.OPENZFS_REPOSITORY}"],
                                      [name: "${env.ILLUMOS_REMOTE}",
                                       url: "https://github.com/${env.ILLUMOS_REPOSITORY}"]],
                  branches: [[name: env.OPENZFS_BRANCH]],
                  extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: env.OPENZFS_DIRECTORY]]])

    }

    stage('merge') {
        common.openzfscish('openzfs-ci', 'open-illumos-sync-pull', false, [
            ['OPENZFS_DIRECTORY', env.OPENZFS_DIRECTORY],
            ['OPENZFS_REPOSITORY', env.OPENZFS_REPOSITORY],
            ['OPENZFS_REMOTE', env.OPENZFS_REMOTE],
            ['OPENZFS_BRANCH', env.OPENZFS_BRANCH],
            ['ILLUMOS_REPOSITORY', env.ILLUMOS_REPOSITORY],
            ['ILLUMOS_REMOTE', env.ILLUMOS_REMOTE],
            ['ILLUMOS_BRANCH', env.ILLUMOS_BRANCH],
        ])
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
