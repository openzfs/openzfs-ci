import org.apache.commons.lang.RandomStringUtils

BUILDER          = 'openzfsci-build-' + RandomStringUtils.randomAlphanumeric(16)
BUILDER_SNAPSHOT = 'openzfsci-snap-' + RandomStringUtils.randomAlphanumeric(16)
ZLOOP_TESTER     = 'openzfsci-zloop-' + RandomStringUtils.randomAlphanumeric(16)
ZFSTEST_TESTER   = 'openzfsci-zfstest-' + RandomStringUtils.randomAlphanumeric(16)

OPENZFS_DIRECTORY = 'openzfs'

if (!OPENZFS_COMMIT)
    error('Empty OPENZFS_COMMIT parameter.')

def OPENZFS_COMMIT_SHORT = OPENZFS_COMMIT.take(7)
if (!OPENZFS_PULL_NUMBER) {
    currentBuild.displayName = "#${env.BUILD_NUMBER} ${OPENZFS_REPOSITORY} commit ${OPENZFS_COMMIT_SHORT}"
} else {
    currentBuild.displayName =
        "#${env.BUILD_NUMBER} ${OPENZFS_REPOSITORY} commit ${OPENZFS_COMMIT_SHORT} PR #${OPENZFS_PULL_NUMBER}"
}

node('master') {
    checkout([$class: 'GitSCM', changelog: false, poll: false,
            userRemoteConfigs: [[name: 'origin', url: "https://github.com/${OPENZFSCI_REPOSITORY}"]],
            branches: [[name: OPENZFSCI_BRANCH]],
            extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: OPENZFSCI_DIRECTORY]]])
    stash(name: 'openzfs-ci', includes: "${OPENZFSCI_DIRECTORY}/**")
}

try {
    create_commit_status(env.JOB_NAME, 'pending', "OpenZFS testing of commit ${OPENZFS_COMMIT_SHORT} in progress.")

    def commit_directory = create_commit_directory_on_manta()
    def pull_directory = create_pull_directory_on_manta()

    try {
        stage('create-builder') {
            create_commit_status('01-create-builder', 'pending', 'Creation of the build machine in progress.')

            def job = build(job: 'regression-steps/01-create-builder',
                            propagate: false, quietPeriod: 0, wait: true, parameters: [
                [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                [$class: 'StringParameterValue', name: 'DCENTER_HOST', value: env.DCENTER_HOST],
                [$class: 'StringParameterValue', name: 'DCENTER_IMAGE', value: env.DCENTER_IMAGE],
                [$class: 'StringParameterValue', name: 'BUILDER', value: BUILDER],
            ])

            post_job_status(job, commit_directory, pull_directory, '01-create-builder',
                'Creation of the build machine has finished.')
            error_if_job_result_not_success(job)
        }

        stage('checkout') {
            create_commit_status('02-checkout', 'pending', "Checkout of commit ${OPENZFS_COMMIT_SHORT} in progress.")

            def job = build(job: 'regression-steps/02-checkout',
                            propagate: false, quietPeriod: 0, wait: true, parameters: [
                [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                [$class: 'StringParameterValue', name: 'OPENZFS_REPOSITORY', value: OPENZFS_REPOSITORY],
                [$class: 'StringParameterValue', name: 'OPENZFS_COMMIT', value: OPENZFS_COMMIT],
                [$class: 'StringParameterValue', name: 'OPENZFS_DIRECTORY', value: OPENZFS_DIRECTORY],
                [$class: 'NodeParameterValue', name: 'BUILDER', labels: [BUILDER],
                    nodeEligibility: [$class: 'AllNodeEligibility']],
            ])

            env.BUILDER_WORKSPACE = job.rawBuild.environment.get('WORKSPACE')

            post_job_status(job, commit_directory, pull_directory, '02-checkout',
                "Checkout of commit ${OPENZFS_COMMIT_SHORT} has finished.")
            error_if_job_result_not_success(job)
        }

        if (!env.BUILDER_WORKSPACE)
            error('empty BUILDER_WORKSPACE environment variable.')

        stage('build') {
            create_commit_status('03-build', 'pending', "Build of commit ${OPENZFS_COMMIT_SHORT} in progress.")

            def job = build(job: 'regression-steps/03-build',
                            propagate: false, quietPeriod: 0, wait: true, parameters: [
                [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                [$class: 'StringParameterValue', name: 'OPENZFS_DIRECTORY', value: OPENZFS_DIRECTORY],
                [$class: 'StringParameterValue', name: 'WORKSPACE', value: env.BUILDER_WORKSPACE],
                [$class: 'NodeParameterValue', name: 'BUILDER', labels: [BUILDER],
                    nodeEligibility: [$class: 'AllNodeEligibility']],
            ])

            post_job_status(job, commit_directory, pull_directory, '03-build',
                "Build of commit ${OPENZFS_COMMIT_SHORT} has finished.")
            error_if_job_result_not_success(job)
        }

        stage('nits') {
            create_commit_status('04-nits', 'pending', "Checking nits of commit ${OPENZFS_COMMIT_SHORT} in progress.")

            def job = build(job: 'regression-steps/04-nits',
                            propagate: false, quietPeriod: 0, wait: true, parameters: [
                [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                [$class: 'StringParameterValue', name: 'OPENZFS_DIRECTORY', value: OPENZFS_DIRECTORY],
                [$class: 'StringParameterValue', name: 'OPENZFS_BASE_COMMIT', value: OPENZFS_COMMIT_BASE],
                [$class: 'StringParameterValue', name: 'WORKSPACE', value: env.BUILDER_WORKSPACE],
                [$class: 'NodeParameterValue', name: 'BUILDER', labels: [BUILDER],
                    nodeEligibility: [$class: 'AllNodeEligibility']],
            ])

            post_job_status(job, commit_directory, pull_directory, '04-nits',
                "Checking nits of commit ${OPENZFS_COMMIT_SHORT} has finished.")
            error_if_job_result_not_success(job)
        }

        stage('install') {
            create_commit_status('05-install', 'pending', "Installation of commit ${OPENZFS_COMMIT_SHORT} in progress.")

            def job = build(job: 'regression-steps/05-install',
                            propagate: false, quietPeriod: 0, wait: true, parameters: [
                [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                [$class: 'StringParameterValue', name: 'OPENZFS_DIRECTORY', value: OPENZFS_DIRECTORY],
                [$class: 'StringParameterValue', name: 'WORKSPACE', value: env.BUILDER_WORKSPACE],
                [$class: 'NodeParameterValue', name: 'BUILDER', labels: [BUILDER],
                    nodeEligibility: [$class: 'AllNodeEligibility']],
            ])

            post_job_status(job, commit_directory, pull_directory, '05-install',
                "Installation of commit ${OPENZFS_COMMIT_SHORT} has finished.")
            error_if_job_result_not_success(job)
        }
    } finally {
        stage('unregister-builder') {
            create_commit_status('06-unregister-builder', 'pending', 'Unregistration of the build machine in progress.')

            def job = build(job: 'regression-steps/06-unregister-builder',
                            propagate: false, quietPeriod: 0, wait: true, parameters: [
                [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                [$class: 'StringParameterValue', name: 'DCENTER_HOST', value: env.DCENTER_HOST],
                [$class: 'StringParameterValue', name: 'DCENTER_EXPIRATION', value: env.DCENTER_EXPIRATION],
                [$class: 'StringParameterValue', name: 'BUILDER', value: BUILDER],
            ])

            post_job_status(job, commit_directory, pull_directory, '06-unregister-builder',
                'Unregistration of the build machine has finished.')
            error_if_job_result_not_success(job)
        }
    }

    stage('snapshot-builder') {
        create_commit_status('07-snapshot-builder', 'pending', 'Snapshotting the build machine in progress.')

        def job = build(job: 'regression-steps/07-snapshot-builder',
                        propagate: false, quietPeriod: 0, wait: true, parameters: [
            [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
            [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
            [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
            [$class: 'StringParameterValue', name: 'DCENTER_HOST', value: env.DCENTER_HOST],
            [$class: 'StringParameterValue', name: 'BUILDER', value: BUILDER],
            [$class: 'StringParameterValue', name: 'BUILDER_SNAPSHOT', value: BUILDER_SNAPSHOT],
        ])

        post_job_status(job, commit_directory, pull_directory, '07-snapshot-builder',
            'Snapshotting the build machine has finished.')
        error_if_job_result_not_success(job)
    }

    try {
        stage('create-testers') {
            create_commit_status('08-create-testers', 'pending', 'Creation of the test machines in progress.')

            def job = build(job: 'regression-steps/08-create-testers',
                            propagate: false, quietPeriod: 0, wait: true, parameters: [
                [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                [$class: 'StringParameterValue', name: 'DCENTER_HOST', value: env.DCENTER_HOST],
                [$class: 'StringParameterValue', name: 'DCENTER_IMAGE', value: env.DCENTER_IMAGE],
                [$class: 'StringParameterValue', name: 'BUILDER', value: BUILDER],
                [$class: 'StringParameterValue', name: 'BUILDER_SNAPSHOT', value: BUILDER_SNAPSHOT],
                [$class: 'StringParameterValue', name: 'ZLOOP_TESTER', value: ZLOOP_TESTER],
                [$class: 'StringParameterValue', name: 'ZFSTEST_TESTER', value: ZFSTEST_TESTER],
            ])

            post_job_status(job, commit_directory, pull_directory, '08-create-testers',
                'Creation of the test machines has finished.')
            error_if_job_result_not_success(job)
        }

        stage('run-tests') {
            parallel('zloop': {
                create_commit_status('09-zloop', 'pending',
                    "Run of 'zloop' for commit ${OPENZFS_COMMIT_SHORT} in progress.")

                def job = build(job: 'regression-steps/09-zloop',
                                propagate: false, quietPeriod: 0, wait: true, parameters: [
                    [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
                    [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
                    [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                    [$class: 'NodeParameterValue', name: 'ZLOOP_TESTER', labels: [ZLOOP_TESTER],
                        nodeEligibility: [$class: 'AllNodeEligibility']],
                ])

                post_job_status(job, commit_directory, pull_directory, '09-zloop',
                    "Run of 'zloop' for commit ${OPENZFS_COMMIT_SHORT} has finished.")
                post_remote_job_test_results(job,
                    commit_directory, pull_directory, '09-zloop-results', 'zloop', '/var/tmp/test_results')
                post_remote_job_test_logfile(job,
                    commit_directory, pull_directory, '09-zloop-logfile', 'zloop', '/var/tmp/test_results/ztest.out')
                error_if_job_result_not_success(job)
            }, 'zfstest': {
                create_commit_status('10-zfstest', 'pending',
                    "Run of 'zfstest' for commit ${OPENZFS_COMMIT_SHORT} in progress.")

                def job = build(job: 'regression-steps/10-zfstest',
                                propagate: false, quietPeriod: 0, wait: true, parameters: [
                    [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
                    [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
                    [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                    [$class: 'NodeParameterValue', name: 'ZFSTEST_TESTER', labels: [ZFSTEST_TESTER],
                        nodeEligibility: [$class: 'AllNodeEligibility']],
                ])

                post_job_status(job, commit_directory, pull_directory, '10-zfstest',
                    "Run of 'zfstest' for commit ${OPENZFS_COMMIT_SHORT} has finished.")
                post_remote_job_test_results(job,
                    commit_directory, pull_directory, '10-zfstest-results', 'zfstest', '/var/tmp/test_results')
                post_remote_job_test_logfile(job,
                    commit_directory, pull_directory, '10-zfstest-logfile', 'zfstest', '/var/tmp/test_results/*/log')
                error_if_job_result_not_success(job)
            })
        }
    } finally {
        stage('unregister-testers') {
            create_commit_status('11-unregister-testers', 'pending', 'Unregistration of the test machines in progress.')

            def job = build(job: 'regression-steps/11-unregister-testers',
                            propagate: false, quietPeriod: 0, wait: true, parameters: [
                [$class: 'StringParameterValue', name: 'OPENZFSCI_REPOSITORY', value: OPENZFSCI_REPOSITORY],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_BRANCH', value: OPENZFSCI_BRANCH],
                [$class: 'StringParameterValue', name: 'OPENZFSCI_DIRECTORY', value: OPENZFSCI_DIRECTORY],
                [$class: 'StringParameterValue', name: 'DCENTER_HOST', value: env.DCENTER_HOST],
                [$class: 'StringParameterValue', name: 'DCENTER_EXPIRATION', value: env.DCENTER_EXPIRATION],
                [$class: 'StringParameterValue', name: 'ZLOOP_TESTER', value: ZLOOP_TESTER],
                [$class: 'StringParameterValue', name: 'ZFSTEST_TESTER', value: ZFSTEST_TESTER],
            ])

            post_job_status(job, commit_directory, pull_directory, '11-unregister-testers',
                'Unregistration of the test machines has finished.')
            error_if_job_result_not_success(job)
        }
    }

    create_commit_status(env.JOB_NAME, 'success', "OpenZFS testing of commit ${OPENZFS_COMMIT_SHORT} was successful.")
} catch (e) {
    create_commit_status(env.JOB_NAME, 'failure', "OpenZFS testing of commit ${OPENZFS_COMMIT_SHORT} failed.")
    throw e
}

def post_job_status(job, commit_directory, pull_directory, context, description) {
    def url = upload_job_console(job, commit_directory, pull_directory)

    def state = 'failure'
    if (job.result == 'SUCCESS')
        state = 'success'

    create_commit_status(context, state, description, url)
}

def error_if_job_result_not_success(job) {
    def job_name = job.rawBuild.environment.get('JOB_BASE_NAME')
    def build_number = Integer.toString(job.number)

    if (job.result != 'SUCCESS')
        error("build #${build_number} of job '${job_name}' failed.")
}

def post_remote_job_test_results(job, commit_directory, pull_directory, context, name, remote_directory) {
    try {
        create_commit_status(context, 'pending', "Upload results for '${name}' in progress.")
        def url = upload_remote_job_test_results(job, commit_directory, pull_directory, remote_directory)
        create_commit_status(context, 'success', "Upload results for '${name}' was successful.", url)
    } catch (e) {
        create_commit_status(context, 'failure', "Upload results for '${name}' failed.")
    }
}

def post_remote_job_test_logfile(job, commit_directory, pull_directory, context, name, logfile) {
    try {
        create_commit_status(context, 'pending', "Upload logfile for '${name}' in progress.")
        def url = upload_remote_job_test_logfile(job, commit_directory, pull_directory, logfile)
        create_commit_status(context, 'success', "Upload logfile for '${name}' was successful.", url)
    } catch (e) {
        create_commit_status(context, 'failure', "Upload logfile for '${name}' failed.")
    }
}

def create_commit_directory_on_manta() {
    node('master') {
        unstash(name: 'openzfs-ci')
        def common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")

        return common.openzfscish(OPENZFSCI_DIRECTORY, 'create-commit-directory-on-manta', true, [
            ['REPOSITORY', OPENZFS_REPOSITORY],
            ['PREFIX', env.MANTA_DIRECTORY_PREFIX],
            ['COMMIT', OPENZFS_COMMIT],
        ]).trim()
    }

}

def create_pull_directory_on_manta() {
    if (!OPENZFS_PULL_NUMBER)
        return null

    node('master') {
        unstash(name: 'openzfs-ci')
        def common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")

        return common.openzfscish(OPENZFSCI_DIRECTORY, 'create-pull-directory-on-manta', true, [
            ['REPOSITORY', OPENZFS_REPOSITORY],
            ['PREFIX', env.MANTA_DIRECTORY_PREFIX],
            ['PULL_NUMBER', OPENZFS_PULL_NUMBER],
        ]).trim()
    }

}

def create_commit_status(context, state, description, url = null) {
    if (COMMIT_STATUS_ENABLED != 'yes')
        return

    node('master') {
        unstash(name: 'openzfs-ci')
        def common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")

        common.openzfscish(OPENZFSCI_DIRECTORY, 'create-commit-status', false, [
            ['REPOSITORY', OPENZFS_REPOSITORY],
            ['COMMIT', OPENZFS_COMMIT],
            ['DESCRIPTION', description],
            ['CONTEXT', context],
            ['STATE', state],
            ['TARGET_URL', url ? url : ''],
        ])
    }
}

def upload_job_console(job, commit_directory, pull_directory) {
    def job_name = job.rawBuild.environment.get('JOB_BASE_NAME')
    def build_number = Integer.toString(job.number)

    node('master') {
        unstash(name: 'openzfs-ci')
        def common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")

        retry(count: 3) {
            return common.openzfscish(OPENZFSCI_DIRECTORY, 'upload-job-console-to-manta', true, [
                ['BUILD_URL', job.rawBuild.environment.get('BUILD_URL')],
                ['JOB_BASE_NAME', job.rawBuild.environment.get('JOB_BASE_NAME')],
                ['COMMIT_DIRECTORY', commit_directory],
                ['PULL_DIRECTORY', pull_directory ? pull_directory : ''],
            ]).trim()
        }
    }
}

def upload_remote_job_test_results(job, commit_directory, pull_directory, remote_directory) {
    node('master') {
        unstash(name: 'openzfs-ci')
        def common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")

        retry(count: 3) {
            return common.openzfscish(OPENZFSCI_DIRECTORY, 'upload-remote-directory-to-manta', true, [
                ['JOB_BASE_NAME', job.rawBuild.environment.get('JOB_BASE_NAME')],
                ['DCENTER_HOST', env.DCENTER_HOST],
                ['DCENTER_GUEST', job.rawBuild.environment.get('NODE_NAME')],
                ['DCENTER_IMAGE', env.DCENTER_IMAGE],
                ['REMOTE_DIRECTORY', remote_directory],
                ['COMMIT_DIRECTORY', commit_directory],
                ['PULL_DIRECTORY', pull_directory ? pull_directory : ''],
            ]).trim()
        }
    }
}

def upload_remote_job_test_logfile(job, commit_directory, pull_directory, logfile) {
    node('master') {
        unstash(name: 'openzfs-ci')
        def common = load("${OPENZFSCI_DIRECTORY}/jenkins/pipelines/library/common.groovy")

        retry(count: 3) {
            return common.openzfscish(OPENZFSCI_DIRECTORY, 'upload-remote-logfile-to-manta', true, [
                ['JOB_BASE_NAME', job.rawBuild.environment.get('JOB_BASE_NAME')],
                ['DCENTER_HOST', env.DCENTER_HOST],
                ['DCENTER_GUEST', job.rawBuild.environment.get('NODE_NAME')],
                ['DCENTER_IMAGE', env.DCENTER_IMAGE],
                ['REMOTE_LOGFILE', logfile],
                ['COMMIT_DIRECTORY', commit_directory],
                ['PULL_DIRECTORY', pull_directory ? pull_directory : ''],
            ]).trim()
        }
    }
}


// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
