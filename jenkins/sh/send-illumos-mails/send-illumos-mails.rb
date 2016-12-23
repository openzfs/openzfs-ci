#!/usr/bin/env ruby

require 'octokit'
require 'optparse'
require 'ostruct'
require 'mail'

#
# TODO: This needs to be changed to the illumos developer list.
#
MAIL_TO = 'Prakash Surya <prakash.surya@delphix.com>'

NEED_MAIL_LABEL_NAME = "need illumos mail"
SEND_MAIL_LABEL_NAME = "send illumos mail"
SENT_MAIL_LABEL_NAME = "sent illumos mail"

def get_options()
    options = OpenStruct.new

    options.netrc_file = nil
    options.repository = nil
    options.user = nil
    options.password = nil

    OptionParser.new do |opts|
        opts.on('--netrc-file FILE') do |file|
            options.netrc_file = file
        end
        opts.on('--repository REPO') do |repo|
            options.repository = repo
        end
        opts.on('--smtp-user USER') do |user|
            options.user = user
        end
        opts.on('--smtp-password PASSWORD') do |password|
            if password == "-"
                options.password = STDIN.gets()
            else
                options.password = password
            end
        end
    end.parse!

    if options.netrc_file.nil? or options.repository.nil? or
            options.user.nil? or options.password.nil?
        raise OptionParser::MissingArgument
    end

    return options
end

def fetch_pulls_needing_illumos_mail(client, repository)
    pulls = []

    client.pulls(repository).each do |pull|
        labels = client.labels_for_issue(repository, pull[:number])

        need_mail_label = false
        send_mail_label = false
        sent_mail_label = false

        labels.each do |label|
            if label[:name] == NEED_MAIL_LABEL_NAME
                need_mail_label = true
            end

            if label[:name] == SEND_MAIL_LABEL_NAME
                send_mail_label = true
            end

            if label[:name] == SENT_MAIL_LABEL_NAME
                sent_mail_label = true
            end
        end

        if need_mail_label and send_mail_label
            if sent_mail_label
                #
                # If we've already sent the mail, then we don't want to
                # send it again. In this case, remove the "send" label,
                # leaving only the "need" and "sent" labels to indicate
                # that the mail as already been sent, and we don't need
                # to send it again.
                #
                # Generally, this case shouldn't happen, since we'll
                # attempt to remove the "send" label right after we send
                # the mail successfully, and add the "sent" label. It's
                # possible, though, that some abnormal event prevented
                # the "send" label from being removed. If that were to
                # occur, we should eventually hit this block (e.g. the
                # next time this script is run) and remove the "send"
                # label.
                #
                remove_send_mail_label(client, repository, pull)
            else
                #
                # If the "sent" label isn't found, then we trust the
                # presence of the "send" label, and proceed to send the
                # mail. Usually this will happen prior to any mail has
                # been sent for the given PR, but can also occur if the
                # labels are manually modified (e.g. by removing the
                # "sent" label and adding the "send" label, to
                # intentionally cause another mail to be sent).
                #
                pulls << pull
            end
        end
    end

    return pulls
end

def send_illumos_mail(client, pull, user, password)
    Mail.defaults do
        delivery_method :smtp, :address              => "smtp.gmail.com",
                               :port                 => 587,
                               :domain               => 'gmail.com',
                               :user_name            => user,
                               :password             => password,
                               :authentication       => 'plain',
                               :enable_starttls_auto => true
    end

    author_name = client.user(pull[:user][:login]).name
    if author_name.nil?
        author_name = "Unknown"
    end

    author_email = client.user(pull[:user][:login]).email

    body = []
    body << "Review: " + pull[:html_url]

    if author_email.nil?
        body << "Author: #{author_name}"
    else
        body << "Author: #{author_name} <#{author_email}>"
    end

    body << ""
    body << "Diff: " + pull[:diff_url]
    body << "Patch: " + pull[:patch_url]
    body << ""
    body << pull[:body]

    Mail.deliver do
        to MAIL_TO.encode(Encoding::UTF_8)
        from author_name.encode(Encoding::UTF_8)
        subject pull[:title].encode(Encoding::UTF_8)
        body body.join("\n").encode(Encoding::UTF_8)
        content_type "text/plain; charset=utf-8".encode(Encoding::UTF_8)
    end
end

def remove_send_mail_label(client, repository, pull)
    client.remove_label(repository, pull[:number], SEND_MAIL_LABEL_NAME)
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
        send_illumos_mail(client, pull, options.user, options.password)

        #
        # Since we don't add the "sent" label and remove the "send"
        # label atomically, we need to ensure we successfully add the
        # "sent" label prior to removing the "send" label.
        #
        # If an abnormal event occurs and we end up adding the "sent"
        # label, but not removing the "send" label, this will be handled
        # gracefully and resolved the next time this script runs
        # (see the "fetch_pulls_needing_illumos_mail" function for more
        # details).
        #
        add_sent_mail_label(client, options.repository, pull)
        remove_send_mail_label(client, options.repository, pull)
    end
end

if __FILE__ == $0
    main()
end

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
