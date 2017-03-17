import jenkins.model.*
import hudson.model.RootAction
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

def approvals = []

ScriptApproval approval = Jenkins.instance.getExtensionList(RootAction.class).get(ScriptApproval.class);
approvals.each { approval.approveSignature(it) }
approval.save()

// vim: tabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
