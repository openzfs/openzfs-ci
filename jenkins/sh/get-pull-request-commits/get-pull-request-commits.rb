#!/usr/bin/env ruby

require 'fileutils'
require 'json'
require 'octokit'
require 'optparse'
require 'ostruct'

APPROVAL_ORGS = ['delphix']
APPROVAL_STRING = '@zettabot go'

def get_options()
    options = OpenStruct.new

    options.netrc_file = nil
    options.repository = nil
    options.pulls_file = nil
    options.pull_request_variant = nil

    OptionParser.new do |opts|
        opts.on('--netrc-file FILE') do |file|
            options.netrc_file = file
        end
        opts.on('--repository REPO') do |repo|
            options.repository = repo
        end
        opts.on('--pull-requests-file FILE') do |file|
            options.pulls_file = file
        end
        opts.on('--pull-request-variant VARIANT') do |variant|
            options.pull_request_variant = variant
        end
    end.parse!

    if options.netrc_file.nil? or options.repository.nil? or
            options.pulls_file.nil? or options.pull_request_variant.nil?
        raise OptionParser::MissingArgument
    end

    if options.pull_request_variant != 'head' and
            options.pull_request_variant != 'merge' and
            options.pull_request_variant != 'all'
        raise OptionParser::InvalidOption
    end

    return options
end

def read_old_pulls(pulls_file, repository)
    old_pulls = {}
    if File.file?(pulls_file)
        file = File.read(pulls_file)
        old_pulls = JSON.parse(file)
    end

    #
    # It's possible that the repository that was queried last time, is
    # different than the repository being queried this time. Thus, we
    # need to verify the old repository matches the current one, before
    # returning the old data. If the repositories don't match, then we
    # have to start fresh, without any old data to use.
    #
    # It would be nice to support multiple repositories better, as that
    # would ease testing experimental changes, but this limitation
    # should suffice for now.
    #
    return old_pulls["repository"] == repository ? old_pulls["pulls"] : {}
end


def write_new_pulls(pull_file, pulls, repository)
    if File.file?(pull_file)
        FileUtils.mv(pull_file, pull_file + '.old')
    end

    File.open(pull_file, "w") do |file|
        file.write(JSON.pretty_generate({
            "repository" => repository,
            "pulls" => pulls
        }))
    end
end

def username_in_approved_organization?(client, username)
    APPROVAL_ORGS.each do |org|
        if client.organization_member?(org, username)
            return true
        end
    end
    return false
end

def comment_grants_approval?(client, comment)
    if comment[:body] != APPROVAL_STRING
        return false
    end

    #
    # This function is very slow (it makes a new network request to
    # github's API, so we want to call it after we've already verified
    # that the comment's content would have granted approval. Checking
    # the comment's content doesn't require any new network activity.
    #
    if !username_in_approved_organization?(client, comment[:user][:login])
        return false
    end

    return true
end


def fetch_comments_for_pull(client, repository, pull_number)
    comments = []
    client.issue_comments(repository, pull_number).each do |comment|
        if comment_grants_approval?(client, comment)
            comments.push(comment[:id])
        end
    end
    return comments
end

def fetch_all_open_pulls(client, repository)
    pulls = {}
    client.pulls(repository).each do |pull|
        comments = fetch_comments_for_pull(client, repository, pull[:number])

        #
        # We must use strings for the keys because when this is serialized
        # to disk, and then read back, the keys in the dictionary read from
        # disk will be strings.
        #
        pulls[pull[:number].to_s] = comments
    end
    return pulls
end

def puts_pull_request_commit(client, repository, pull_number,
        pull_request_variant)
    pr = client.pull(repository, pull_number)

    if pull_request_variant == 'head' or pull_request_variant == 'all'
        printf("%s %s %s\n",
                pr[:head][:sha], pr[:base][:sha], pr[:number])
    end

    if pull_request_variant == 'merge' or pull_request_variant == 'all'
        printf("%s %s %s\n",
                pr[:merge_commit_sha], pr[:base][:sha], pr[:number])
    end
end

def main()
    options = get_options()

    client = Octokit::Client.new(:netrc => true,
            :netrc_file => options.netrc_file)
    client.auto_paginate = true

    old_pulls = read_old_pulls(options.pulls_file, options.repository)
    open_pulls = fetch_all_open_pulls(client, options.repository)

    open_pulls.each do |pull_number, new_comments|
        old_comments = []
        if old_pulls.key?(pull_number)
            old_comments = old_pulls[pull_number]
        end

        merged_comments = old_comments | new_comments
        if merged_comments.length > old_comments.length
            puts_pull_request_commit(client, options.repository,
                    pull_number, options.pull_request_variant)
        end
    end

    write_new_pulls(options.pulls_file, open_pulls, options.repository)
end

if __FILE__ == $0
    main()
end

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
