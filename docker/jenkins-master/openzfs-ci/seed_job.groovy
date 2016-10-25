job('seed-job') {
    scm {
        github('openzfs/openzfs-ci', 'master', 'https')
    }

    steps {
        dsl {
            external('jenkins/jobs/*.groovy')
        }
    }
}

// vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
