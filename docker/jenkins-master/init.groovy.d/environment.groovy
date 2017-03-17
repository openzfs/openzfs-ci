import jenkins.model.*
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

def environment = [
    new Entry('JENKINS_URL', System.getenv('JENKINS_URL')),
    new Entry('JENKINS_SLAVE_AGENT_PORT', System.getenv('JENKINS_SLAVE_AGENT_PORT')),
]

Jenkins.instance.globalNodeProperties.replaceBy([new EnvironmentVariablesNodeProperty(environment)])

// vim: tabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
