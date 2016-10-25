#!/usr/bin/env python

import sys
import argparse
from jenkinsapi.jenkins import Jenkins


def create_arg_parser():
    parser = argparse.ArgumentParser()
    parser.add_argument('--url', default='http://localhost:8080')
    parser.add_argument('--username', default=None)
    parser.add_argument('--password', default=None)
    return parser


def main(argv):
    parser = create_arg_parser()
    args = parser.parse_args(argv[1:])

    server = Jenkins(args.url, username=args.username, password=args.password)
    for plugin in server.get_plugins().values():
        if plugin.active and plugin.enabled:
            print '{}:{}'.format(plugin.shortName, plugin.version)

if __name__ == '__main__':
    main(sys.argv)
