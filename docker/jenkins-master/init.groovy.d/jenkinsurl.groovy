import jenkins.model.*

def location = JenkinsLocationConfiguration.get()
location.setUrl(System.env.JENKINS_URL)
location.save()

// vim: tabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
