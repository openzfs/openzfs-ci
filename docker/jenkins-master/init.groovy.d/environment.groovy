import jenkins.model.*
import hudson.slaves.EnvironmentVariablesNodeProperty
import hudson.slaves.EnvironmentVariablesNodeProperty.Entry;

def environment = [
    new Entry('JENKINS_URL', System.getenv('JENKINS_URL')),
    new Entry('JENKINS_SLAVE_AGENT_PORT', System.getenv('JENKINS_SLAVE_AGENT_PORT')),
    new Entry('OPENZFSCI_PRODUCTION', System.getenv('OPENZFSCI_PRODUCTION')),
    new Entry('OPENZFSCI_REPOSITORY', System.getenv('OPENZFSCI_REPOSITORY')),
    new Entry('OPENZFSCI_BRANCH', System.getenv('OPENZFSCI_BRANCH')),
]

Jenkins.instance.globalNodeProperties.replaceBy([new EnvironmentVariablesNodeProperty(environment)])
