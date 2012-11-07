aws-parallelizer
================

Massively parallel file processing framework on AWS.

This is an extremely generic framework for processing files.
Intended for situations in which all of the following are true:
-There are many files
-Each file takes a decent amount of time to process (at least several seconds)
-Files can be processed in any order
-Files are completely independent of each other
-The result of processing a file is a single output file, which may or may
 not be produced
-There is a single command that does the processing
-Optionally, there is an associated environment needed for running the command

If all of the above are true, then this framework can be used.  The framework
automatically handles load balancing and fault tolerance issues.  Any number
of instances can be used, and instances can be added or removed dynamically
without any penalty.

The command must be able to be invoked like so:
my_command_name input_file

...where my_command_name is a user-configurable name of a command (more on
that later), and input_file is an input file that is selected by the framework.

If there is a file that is output from the command, then the command must
write the name of the file to stdout, and ONLY the name of the file.  If the
result from stdout is that of a valid file, then the file will be uploaded into
a single S3 bucket (more on that later).  If there is no output or the output
is not that of a valid file, then the framework will simply move on to the next
file.

At this time there is no way to upload more than a single file at once.  If you
would like to do so, you can make your command output as many files as you
want, and finally zip them together into a single output file.

BASIC DESIGN:
This code runs on an instance.  It is intended to be started automatically
at instance start time.  In most applications, it is also appropriate to
shut down the instance once this code completes.

All processing occurs on the local filesystem of an instance.  The command may
do whatever it wants with the local filesystem, but such changes are local.
You will need to condense everything into a file and output the name of said
file if you want anything to be permanently saved.

SQS is used heavily.  All input filenames that have yet to be processed are
put into SQS, one file per message.  Instances pull messages off of the queue
one at a time, retrieving the input file from an S3 bucket, performing whatever
user-defined processing there is, and possibly writing the output to another S3
bucket.  SQS confers load balancing, as it acts as a global queue upon which
instances can pull more work from.  Load balancing is at the granularity of a
single file.  SQS also grants fault tolerance.  Messages ultimately remain in
the queue until they are explicitly deleted, and this delete occurs only when
we have completely finished processing a file and optionally uploaded the
output.  If there is a failure in between, the message to process the file
remains, and another instance will later process the file.

LOW-LEVEL USAGE:
(TODO: Make this much, much more complete.  Also, automate as much of this as
 possible.  A good portion is already automated, but it could be better.)
1.) Make an S3 bucket specifically for input files
2.) Upload all input files to the S3 bucket
3.) Make an S3 bucket specifically for the execution environment
4.) Package up the entire execution environment into a zip file
5.) Upload the zip file to the execution environment bucket
6.) Make an S3 bucket specifically for the output files
7.) Put the names of all of the files you want to analyze in the input
    bucket is a dedicated SQS queue.
8.) Start up instances that will run this code, passing the necessary parameters
    as user-defined text at instance creation time.
9.) Wait until they complete.
10.) Download all the output from the S3 bucket.

