import jenkins.model.*

/*
 * Disable the Ping Thread to alleviate timeout with slaves, and resultant test failures.
 * See also: https://wiki.jenkins.io/display/JENKINS/Ping+Thread
 */
Jenkins.instance.injector.getInstance(hudson.slaves.ChannelPinger.class).@pingIntervalSeconds = -1
Jenkins.instance.injector.getInstance(hudson.slaves.ChannelPinger.class).@pingTimeoutSeconds = -1

// vim: tabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
