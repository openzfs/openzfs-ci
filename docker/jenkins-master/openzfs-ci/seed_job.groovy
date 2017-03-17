job('seed-job') {
    scm {
        github(System.getenv('OPENZFS_REPOSITORY'), System.getenv('OPENZFS_BRANCH'), 'https')
    }

    triggers {
        scm('H/5 * * * *')
    }

    steps {
        dsl {
            external('jenkins/jobs/*.groovy')
            removeAction('DELETE')
        }
    }
}

// vim: tabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
