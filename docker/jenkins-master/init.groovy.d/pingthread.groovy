import jenkins.model.*

// Bump these to 10x the default values to try and alleviate Ping Thread timeouts.
System.setProperty("hudson.slaves.ChannelPinger.pingIntervalSeconds", "3000")
System.setProperty("hudson.slaves.ChannelPinger.pingTimeoutSeconds", "2400")

// vim: tabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
