#!/usr/bin/env bash

# update-lambda.sh STAGE

set -e

test -z $1 && echo 'Stage missing' && exit 1

STAGE=$1

my_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

sbt assembly

jar_file=$(ls $my_dir/target/scala-2.11/crossword-xml-uploader-assembly*.jar)
jar_file_base=$(basename $jar_file)

aws s3 cp \
  --profile composer \
  $jar_file \
  s3://crossword-dist/crosswords/$STAGE/crossword-xml-uploader-lambda/$jar_file_base

aws lambda update-function-code \
  --function-name crossword-xml-uploader-$STAGE \
  --profile composer \
  --s3-bucket crossword-dist \
  --s3-key crosswords/$STAGE/crossword-xml-uploader-lambda/$jar_file_base
