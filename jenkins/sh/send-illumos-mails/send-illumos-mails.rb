#!/usr/bin/env ruby

require 'octokit'
require 'optparse'
require 'ostruct'
require 'net/smtp'

#
# TODO: This is currently hardcoded to Delphix's SMTP server's IP
# address. This needs to be modified to use some public SMTP service;
# e.g. Gmail.
#
SMTP_SERVER_ADDRESS = '172.16.101.11'

#
# TODO: Is this the correct prefix to be using?
#
SMTP_MAIL_SUBJECT_PREFIX = '[REVIEW]'

#
# TODO: This needs to be changed to the illumos developer list.
#
SMTP_MAIL_TO = 'Prakash Surya <prakash.surya@delphix.com>'

#
# This address is only used if the user that "owns" the pull request,
# doesn't have a public email address listed on their GitHub profile.
#
# TODO: Is this the correct address to use as a fallback?
#
SMTP_MAIL_FROM_FALLBACK = 'No Reply <noreply@openzfs.org>'

#
# TODO: Are these the correct names to use as labels?
#
NEED_MAIL_LABEL_NAME = "need illumos mail"
SENT_MAIL_LABEL_NAME = "sent illumos mail"

def get_options()
    options = OpenStruct.new

    options.netrc_file = nil
    options.repository = nil

    OptionParser.new do |opts|
        opts.on('--netrc-file FILE') do |file|
            options.netrc_file = file
        end
        opts.on('--repository REPO') do |repo|
            options.repository = repo
        end
    end.parse!

    if options.netrc_file.nil? or options.repository.nil?
        raise OptionParser::MissingArgument
    end

    return options
end

def fetch_pulls_needing_illumos_mail(client, repository)
    pulls = []

    client.pulls(repository).each do |pull|
        labels = client.labels_for_issue(repository, pull[:number])

        need_mail_label = false
        sent_mail_label = false

        labels.each do |label|
            if label[:name] == NEED_MAIL_LABEL_NAME
                need_mail_label = true
            end

            if label[:name] == SENT_MAIL_LABEL_NAME
                sent_mail_label = true
            end
        end

        if need_mail_label and not sent_mail_label
            pulls << pull
        end
    end

    return pulls
end

def send_illumos_mail(client, pull)
    #
    # We want to use the GitHub user's name and email address listed on
    # their profile, but it's possible for a user to not have these listed.
    # We detect this case when either of these fields are "nil", and
    # then use another email address as a fallback.
    #
    user_email = client.user(pull[:user][:login]).email
    user_name = client.user(pull[:user][:login]).name
    smtp_mail_from = SMTP_MAIL_FROM_FALLBACK
    if not user_email.nil? and not user_name.nil?
        smtp_mail_from = "#{user_name} <#{user_email}>"
    end

    message = []
    message << "From: #{smtp_mail_from}"
    message << "To: #{SMTP_MAIL_TO}"
    message << "Subject: #{SMTP_MAIL_SUBJECT_PREFIX} #{pull[:title]}"
    message << ""
    message << pull[:html_url]
    message << ""
    message << pull[:body]

    puts message.join("\n")

    Net::SMTP.start(SMTP_SERVER_ADDRESS) do |smtp|
        smtp.send_message(message.join("\n"), smtp_mail_from, SMTP_MAIL_TO)
    end
end

def add_sent_mail_label(client, repository, pull)
    client.add_labels_to_an_issue(repository, pull[:number],
                                [SENT_MAIL_LABEL_NAME])
end

def main()
    options = get_options()

    client = Octokit::Client.new(:netrc => true,
            :netrc_file => options.netrc_file)
    client.auto_paginate = true

    pulls = fetch_pulls_needing_illumos_mail(client, options.repository)
    pulls.each do |pull|
        send_illumos_mail(client, pull)
        add_sent_mail_label(client, options.repository, pull)
    end
end

if __FILE__ == $0
    main()
end

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
