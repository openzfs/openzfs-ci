import jenkins.model.Jenkins
import hudson.model.FreeStyleProject
import javaposse.jobdsl.plugin.ExecuteDslScripts

def jobName = 'bootstrap'

def job = Jenkins.instance.getItem(jobName)
if (job == null)
    job = Jenkins.instance.createProject(FreeStyleProject, jobName)
job.displayName = jobName

def seedJobFile = new File("/opt/openzfs-ci/seed_job.groovy")
def seedJobScript = new ExecuteDslScripts(seedJobFile.text)

job.buildersList.clear()
job.buildersList.add(seedJobScript)
job.save()

// vim: tabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
