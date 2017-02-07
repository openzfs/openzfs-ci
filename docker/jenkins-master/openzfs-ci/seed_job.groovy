job('seed-job') {
    scm {
        github('openzfs/openzfs-ci', 'master', 'https')
    }

    triggers {
        scm('@hourly')
    }

    steps {
        dsl {
            external('jenkins/jobs/folders.groovy')

            external('jenkins/jobs/*.groovy')
            external('jenkins/jobs/*/*.groovy')

            removeAction('DELETE')
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
