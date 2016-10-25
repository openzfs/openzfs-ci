#!/usr/bin/env ruby

require 'octokit'
require 'optparse'
require 'ostruct'

def get_options()
    options = OpenStruct.new

    options.netrc_file = nil

    options.repository = nil
    options.sha = nil
    options.state = nil
    options.context = nil

    options.target_url = nil
    options.description = nil

    OptionParser.new do |opts|
        opts.on('--netrc-file FILE') do |file|
            options.netrc_file = file
        end
        opts.on('--repository REPO') do |repo|
            options.repository = repo
        end
        opts.on('--sha SHA') do |sha|
            options.sha = sha
        end
        opts.on('--state STATE') do |state|
            options.state = state
        end
        opts.on('--context CONTEXT') do |context|
            options.context = context
        end
        opts.on('--target-url URL') do |target_url|
            options.target_url = target_url
        end
        opts.on('--description DESCRIPTION') do |description|
            options.description = description
        end
    end.parse!

    if options.netrc_file.nil? or options.repository.nil? or
            options.sha.nil? or options.state.nil? or options.context.nil?
        raise OptionParser::MissingArgument
    end

    return options
end

def main()
    cli = get_options()

    client = Octokit::Client.new(:netrc => true,
            :netrc_file => cli.netrc_file)

    opts = {}
    opts[:context] = cli.context

    if !cli.target_url.nil?
        opts[:target_url] = cli.target_url
    end

    if !cli.description.nil?
        opts[:description] = cli.description
    end

    client.create_status(cli.repository, cli.sha, cli.state, opts)
end

if __FILE__ == $0
    main()
end

# vim: tabstop=4 softtabstop=4 shiftwidth=4 expandtab textwidth=72 colorcolumn=80
