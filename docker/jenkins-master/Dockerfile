FROM jenkins/jenkins:2.150

USER root

RUN apt-get update && \
    apt-get install -y build-essential iproute2 jq libffi-dev libssl-dev \
        pv python-dev python-pip ruby-full sshpass unzip wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

RUN pip install ansible cryptography awscli
RUN gem install octokit -v 4.3.0 && gem install netrc -v 0.11.0 && \
    gem install mail -v 2.6.4
RUN wget -qO /tmp/hub.tgz \
    https://github.com/github/hub/releases/download/v2.2.9/hub-linux-amd64-2.2.9.tgz && \
    tar -xf /tmp/hub.tgz && ./hub-linux-amd64-2.2.9/install && rm /tmp/hub.tgz
RUN wget -qO /tmp/vault.zip \
    https://releases.hashicorp.com/vault/0.6.1/vault_0.6.1_linux_amd64.zip && \
    unzip -d /bin /tmp/vault.zip && rm /tmp/vault.zip && chmod 755 /bin/vault

USER jenkins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
COPY init.groovy.d /usr/share/jenkins/ref/init.groovy.d
COPY openzfs-ci /opt/openzfs-ci
