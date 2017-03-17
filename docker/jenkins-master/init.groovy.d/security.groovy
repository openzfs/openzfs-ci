import jenkins.model.*
import hudson.security.*
import org.acegisecurity.userdetails.*

def instance = Jenkins.getInstance()

/*
 * We only want to create the "zettabot" user if it doesn't already exist,
 * or else we could end up overwriting the existing user and deleting any
 * previous password that might have been created.
 */
try {
    instance.getSecurityRealm().loadUserByUsername('zettabot')
} catch (UsernameNotFoundException e) {
    def realm = new HudsonPrivateSecurityRealm(false)
    realm.createAccount('zettabot', 'password')
    instance.setSecurityRealm(realm)
    instance.save()
}

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(true)
instance.setAuthorizationStrategy(strategy)
instance.save()

// vim: tabstop=4 shiftwidth=4 expandtab textwidth=112 colorcolumn=120
