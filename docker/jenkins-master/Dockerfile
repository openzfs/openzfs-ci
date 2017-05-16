FROM jenkinsci/jenkins:2.61

USER root

RUN apt-get update && \
    apt-get install -y \
        software-properties-common python-software-properties && \
    (curl -sL https://deb.nodesource.com/setup_6.x | bash -) && \
    apt-get update && \
    apt-get install -y build-essential iproute2 jq libffi-dev libssl-dev \
        nodejs pv python-dev python-pip ruby-full sshpass unzip wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN pip install ansible cryptography awscli
RUN gem install octokit -v 4.3.0 && gem install netrc -v 0.11.0 && \
    gem install mail -v 2.6.4
RUN npm install manta -g && npm cache clear
RUN wget -qO /usr/bin/runurl run.alestic.com/runurl && chmod 755 /usr/bin/runurl
RUN wget -qO /tmp/hub.tgz \
    https://github.com/github/hub/releases/download/v2.2.9/hub-linux-amd64-2.2.9.tgz && \
    tar -xf /tmp/hub.tgz && ./hub-linux-amd64-2.2.9/install && rm /tmp/hub.tgz
RUN wget -qO /tmp/vault.zip \
    https://releases.hashicorp.com/vault/0.6.1/vault_0.6.1_linux_amd64.zip && \
    unzip -d /bin /tmp/vault.zip && rm /tmp/vault.zip && chmod 755 /bin/vault
RUN wget -qO /tmp/pup.zip \
    https://github.com/ericchiang/pup/releases/download/v0.4.0/pup_v0.4.0_linux_amd64.zip && \
    unzip -d /bin /tmp/pup.zip && rm /tmp/pup.zip && chmod 755 /bin/pup

USER jenkins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt
COPY init.groovy.d /usr/share/jenkins/ref/init.groovy.d
COPY openzfs-ci /opt/openzfs-ci
