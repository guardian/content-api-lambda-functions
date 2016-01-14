# Crossword Uploader Lambdas
Lambdas used to move crosswords from s3 into flexible content. They are managed
by the Editorial Tools team.

## crossword-xml-uploader
This lambda fetches crossword xml files from s3 and puts them on a kinesis stream for composer to consume.

## crossword-pdf-uploader
This lambda fetches crossword pdf files from a private s3 bucket
(first verifying that they are not set to be published in the future), then posts
them to a public s3 bucket.
