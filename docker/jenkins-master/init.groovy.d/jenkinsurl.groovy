import jenkins.model.*

def location = JenkinsLocationConfiguration.get()
location.setUrl(System.env.JENKINS_URL)
location.save()
