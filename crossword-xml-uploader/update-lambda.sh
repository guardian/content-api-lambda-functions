#!/usr/bin/env bash

# update-lambda.sh STAGE

set -e

test -z $1 && echo 'Stage missing' && exit 1

STAGE=$1

my_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

sbt assembly

jar_file=$(echo $my_dir/target/scala-2.11/crossword-xml-uploader-assembly*.jar)

aws lambda update-function-code \
  --function-name crossword-xml-uploader-$STAGE \
  --zip-file fileb://$jar_file