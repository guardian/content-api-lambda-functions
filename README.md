# Crossword Uploader Lambdas
Lambdas used to move crosswords from a bucket in s3 into flexible content. They are managed
by the Editorial Tools team.

Crosswords are uploaded on a regular basis to the S3 bucket by fingerpost. This process is managed by ESD.

The lambdas are set up in the composer AWS account. Both the CODE and PROD versions read from the same bucket, but write PDF files to different buckets, and talk to the CODE/PROD versions of the microapp respectively. When debugging problems with the crosswords, a good place to start is the cloudwatch logs for the lambdas.

There is an unfinished, but possibly still useful diagram of the crossword publication system [here](https://docs.google.com/drawings/d/1q0FDZIbTDRtRMcMnciEywwgg3VnDYBDxxOATRBfWPsM/).

## crossword-xml-uploader
This lambda fetches crossword xml files from s3 and puts them on a kinesis stream for composer to consume.

General process:

 - XML file uploaded to S3
 - Lambda runs every hour, fetches all XML files. For each XML file, it
   - Uploads the file to the [crossword microapp](https://github.com/guardian/crossword). If an error is thrown by the microapp (which will happen if the XML is invalid for some reason), stop here. Otherwise...
   - Send the crossword on the flex kinesis stream, where flex integration picks it up and turns it into a composer article with a scheduled launch time. The code for this is [here](https://github.com/guardian/flexible-content/blob/master/flexible-content-integration/src/main/scala/com/gu/flexiblecontent/integration/integration/CrosswordImportCommand.scala).


Common problems:
 - XML file invalid e.g. full stop used instead of a comma. Run the lambda by clicking 'test' in the aws console, then if it throws an error check the microapp logs in the google developers console to get a better idea of what's happened. You should get some indication of what was wrong with the XML file, then you can download it from the source s3 bucket, fix it, then re-upload it and run the lambda again.
 - XML file not added to the source bucket - you can check the files-for-processing and processed-files buckets for the file (called <crosswordnumber>.xml) - if it's not there then we need to get fingerpost/ESD to re-upload it then run the lambda again.

## crossword-pdf-uploader
This lambda fetches crossword pdf files from a private s3 bucket
(first verifying that they are not set to be published in the future), moves
them to a public s3 bucket, then calls the crossword microapp to provide the link to the PDF file for the crossword.

Each crossword may have more than one pdf version of it in the source bucket. The lambda only ever publishes the most recent version of the PDF file. Source files are only moved from the bucket once a crossword has been published.

Common problems:
 - PDF file not added to the source bucket
 - Wrong version of the PDF file uploaded or picked by the lambda
 - Crossword doesn't exist in the crossword microapp, so the lambda has nowhere to put the link to the pdf file.
