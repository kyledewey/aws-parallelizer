aws-parallelizer
================

Massively parallel file processing framework on AWS.

This is an extremely generic framework for processing files.
Intended for situations in which all of the following are true:
- There are many files
- Each file takes a decent amount of time to process (at least several seconds)
- Files can be processed in any order
- Files are completely independent of each other
- The result of processing a file is a single output file, which may or may
  not be produced
- There is a single command that does the processing
- Optionally, there is an associated environment needed for running the command

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

### Basic Design ###
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

### System Overview ###
The system has the following components:

1. An S3 bucket holding input files
2. An S3 bucket holding the file anlaysis program and its execution environment
3. An S3 bucket holding output files
4. An SQS queue holding filenames that have yet to be processed
5. Any number of EC2 instances performing the actual processing, using a
   custom AMI (herein referred to as "workers").

The lifecycle of each worker is roughly as follows:

```java
bootUp();
String zipName = param( ENVIRONMENT_ZIP_FILE_NAME );
s3EnvironmentBucket.getFile( zipName );
execute( "unzip", zipName );
execute( "cd", param( ENVIRONMENT_PREFIX ) );
execute( "chmod", "a+x", param( ANALYSIS_FILE_NAME ) );

while ( sqsQueue.containsMessages() ) {
  Message message = sqsQueue.getMessage();
  String filename = message.getContents();
  s3InputBucket.getFile( filename );
  String commandOutput = execute( param( ANALYSIS_FILE_NAME ),
                                  filename );
  if ( isFile( commandOutput ) ) {
    s3OutputBucket.putFile( commandOutput );
    deleteFile( commandOutput );
  }
  sqsQueue.doneWithMessage( message );
  deleteFile( filename );
}

if ( param( TERMINATE_ON_SHUTDOWN ) == true ) {
  if ( param( PERSISTENT_SPOT_INSTANCE ) == true ) {
    ec2.cancelSpotInstanceRequest( this );
  }
  execute( "shutdown", "-h", "now" );
}
```
  

### General Usage ###
These are the basic steps to using the framework.  Many of these steps are at least
partially automated with some included scripts.  This information is provided mostly
for troubleshooting reasons.

1. Make an S3 bucket specifically for input files.
2. Upload all input files to the S3 bucket
3. Make an S3 bucket specifically for the execution environment
4. Package up the entire execution environment into a zip file
5. Upload the zip file to the execution environment bucket
6. Make an S3 bucket specifically for the output files
7. Put the names of all of the files you want to analyze in the input
   bucket is a dedicated SQS queue.
8. Start up instances that will run this code, passing the necessary parameters
    as user-defined text at instance creation time.
9. Wait until they complete.
10. Download all the output from the S3 bucket.

### Usage in More Detail ###

Edit your `parameters.txt` file to contain at least your access key and
your secret key.
Then run `PrepBucket` to make and upload your files to a bucket of your choice.
The command works as such:
```console
java PrepBucket bucket_name file_1 file_2 ... file_n
```

...where `bucket_name` is the name of your bucket.  If `bucket_name`
doesn't already exist, it will create a new bucket with that name.  Each of the
files specified will be uploaded to that bucket.

Now make S3 buckets for the environment and for the output files.  Personally I just
do this through the AWS Management Console.

Now package up your analysis script and any environment it needs into a zip file.
For example, say our analysis script is named `analysis.sh`, and it needs
the files `foo.txt` and `bar.txt` in order to function.  Then we
would do:
```console
mkdir environment
cp analysis.sh foo.txt bar.txt environment
zip -r environment.zip environment
```

...which leaves us with the complete environment `environment.zip`.
This file can be uploaded with `PrepBucket`, through the AWS Management Console,
or with whatever you prefer.

Now the SQS queue needs to be prepped.  Make a new SQS queue in the AWS Management Console,
and **set the retention period to 14 days and the visibility timeout to whatever you want**.
Due to a oddity in the AWS APIs, if you don't set these properly things can get very confused,
as this configuration information is used in identifying SQS queues to some degree.

At this point, you need to add all the parameters needed to run `BucketToQueue`
(see the "Parameters" section for this) to `parameters.txt`.  **Be sure to use the
same visibility timeout as specified before for the SQS queue creation.**

Once the parameters are added, you can run `BucketToQueue` like so:
```console
java BucketToQueue input_bucket_name queue_name
```

...this will examine the files in `input_bucket`.  For each file, it will
put an SQS message into the queue `queue_name` containing the name of the file.

Now for the fun part: starting instances.  For this, you'll need to add all the parameters
necessary for running `StartInstances` to `parameters.txt` (see 
the "Parameters" section for more on this). Once the parameters are added to 
`parameters.txt`, run `StartInstances` like so:
```console
java StartInstances number_to_start max_price
```

...where `number_to_start` is the number of instances to start and
`max_price` is the maximum bidding price for spot instances. (Due to the nature
of this application, only spot instances can be used in this manner.)

Now you wait.  You can check on the progress of the analysis through the AWS Management
Console.  The number of messages in flight corresponds to how many files are being processed
at once, and the number of messages enqueued are the number of files that are awaiting
processing.  You can also see output files rolling into your S3 output bucket.

Once the analysis is complete, instances will start terminating (as long as you've configured
them to do so).  At any point, you can download the output files using the `Download`
command, which has been included.  It can be run like so:
```console
java Download bucket_name
```

...where `bucket_name` is the name of the bucket from which you want to download
files.

### Notes ###
The following are just some scattered notes about the framework.

1. Instances can be added at any time with the `StartInstances`.  It's perfectly
   fine to add additional instances later to something that is already running.
2. The framework is highly fault-tolerant.  A single instance failing will result in only
   the loss of progress on whatever specific file it was analyzing.  The file itself will
   restart analysis on another instance.

### Known Bugs ###
Just one highly tricky one: once the SQS queue is prepped with `BucketToQueue`,
analysis must complete within 14 days (the maximum retention period).  After this point,
messages will spontaneously delete themselves, making the analysis terminate prematurely.
Dealing with this in an automated fashion is a *very* hard problem, and really isn't worth
the effort.  If this happens, here is a workaround:

1. Download all output files
2. Determine which input files were processed based this output.
3. Delete these files which had been processed from the input bucket.  Alternatively, create
   a new bucket containing only those files which have *not* been processed, and reupload
   those input files to this new bucket.
4. Run `BucketToQueue` again on the input bucket (or the new bucket if you went
   with that route).
5. Run `StartInstances` again.  Note that if you went the alternate route you'll
   need to change the parameter for the input bucket before you can start instances again.

### Parameters ###
Parameters are specified in a file named `parameters.txt`, except for
`StartInstances` which explicitly asks for its file containing parameters.
There is one parameter per line, in the format:
```
parameter_name=value
```
Note that it is sensitive to whitespace.

All programs need the `secretKey` and the `accessKey` parameters specified.
`CancelPersistent` (cloud only) and `Client` (cloud only) use all the
parameters up until `shouldShutdown` in the below parameters table.
`StartInstances` needs all the parameters in the table below.

There are a number of parameters in the framework, and different commands need different ones.
A listing of these parameters, along with information pertaining to what they are and their
default values, is below.  Note that if any parameter's default value is "N/A", this
means that the parameter is required as it has no reasonable default.

<table border="1">
  <tr>
    <th>Name</th>
    <th>Description</th>
    <th>Default Value</th>
  </tr>

  <tr>
    <td>`accessKey`</td>
    <td>Your AWS access key</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`secretKey`</td>
    <td>Your AWS secret key</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`EC2SQSRegion`</td>
    <td>Which region you want the analysis to run in.  For a listing of available
      regions, consult the <a href="http://docs.amazonwebservices.com/general/latest/gr/rande.html">Amazon AWS documentation</a>.</td>
    <td>`us-east-1`</td>
  </tr>

  <tr>
    <td>`inputBucket`</td>
    <td>The name of the S3 bucket that holds input files</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`outputBucket`</td>
    <td>The name of the S3 bucket that holds input files</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`queueURL`</td>
    <td>The URL of the SQS queue that holds the names of files that have yet to be processed</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`environmentBucket`</td>
    <td>The name of the S3 bucket that holds execution environment zip files</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`environmentPrefix`</td>
    <td>The directory in the zip file that contains the analysis program.  In the example
      in the "Usage in More Detail" section, this would be "environment".</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`environmentZip`</td>
    <td>Name of the zip file containing the execution environment.  Must exist within the S3
      bucket specified by the `environmentBucket` parameter.</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`analysisProgram`</td>
    <td>Name of the analysis program contained underneath `environmentPrefix` in the
      zip file specified by `environmentZip`</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`visibilityTimeout`</td>
    <td>The visibility timeout for the SQS queue specified by `queueURL`, 
      in seconds.</td>
    <td>`600` (10 minutes)</td>
  </tr>

  <tr>
    <td>`numThreads`</td>
    <td>The number of files to process in parallel on a given instance.  Use `0` to
      use the number of available virtual threads on the given instance.</td>
    <td>`0`</td>
  </tr>

  <tr>
    <td>`shouldShutdown`</td>
    <td>Whether or not to engage shutdown procedures whenever we run out of files or the analysis
      crashes, whatever comes first.  For a persistent spot instance, this will also mean
      canceling the spot request behind the instance.  As to what exactly happens on shutdown,
      see the `shutdownBehavior` parameter.</td>
    <td>`true`</td>
  </tr>

  <tr>
    <td>`keyPair`</td>
    <td>The name of your key pair used for starting instances on AWS.</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`imageID`</td>
    <td>The ID of the AMI that has been instrumented with the framework code.</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`securityGroup`</td>
    <td>The name of the AWS security group to use</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`instanceType`</td>
    <td>The type of instance to use.  The name must be one of the names listed in the
      <a href="http://docs.amazonwebservices.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/model/InstanceType.html">
      	 Amazon AWS documentation</a>.</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td>`shutdownBehavior`</td>
    <td>What to do when `shutdown -h now` is run.
      Specify `stop` to pause the instance, allowing execution to be resumed later
      (through some means external to this framework).  You will still be charged for the
      time an instance is stopped, even though it is not actively executing anything.
      Specify `terminate` to completely end the instance.  You stop being charged
      for execution time at this point.  For further information, consult 
      <a href="http://support.rightscale.com/06-FAQs/FAQ_0149_-_What%27s_the_difference_between_Terminating_and_Stopping_an_EC2_Instance%3F">this FAQ</a>.</td>
    <td>`terminate`</td>
  </tr>

  <tr>
    <td>`spotType`</td>
    <td>The kind of spot instance to use.
      Specify `on-time` for a spot request that ends once the instance terminates.
      Specify `persistent` for a spot request that only ends once the request is
      explicitly canceled.  If `shouldShutdown` is set to `true`, then
      the request will be canceled once we are done processing files, or if the framework
      crashes but the machine remains online.  If termination happens for any other reason
      (i.e. AWS failure, max bid price exceeds current maximum bid, etc.), then the request
      remains, and can result in the instance automatically being brought back online.  Cost
      is only accumulated while a machine is online.</td>
    <td>`one-time`</td>
  </tr>
</table>

### Making Your Own AMI ###
The framework expects a certain kind of AMI, as specified through the `imageID`
parameter.  To make your own AMI that can be used in tandem with `imageID`:

1. Download the AWS parallelizer code (`git clone git://github.com/kyledewey/aws-parallelizer.git`).
2. Go to the `src/` directory. (`cd src`)
3. Download [version 1.3.25 of the AWS Java SDK](http://sdk-for-java.amazonwebservices.com/aws-java-sdk-1.3.25.zip), putting it in the same directory. (`wget http://sdk-for-java.amazonwebservices.com/aws-java-sdk-1.3.25.zip`).
4. Unzip the archive (`unzip aws-java-sdk-1.3.25.zip`).
5. Include all the .jar files in the archive in your Java classpath (``export CLASSPATH=.:`find . -name '*.jar' | xargs | tr ' ' ':'` ``).
6. Compile the code (`javac *.java`)
7. Start an instance on AWS
8. Copy over all `.class` files to the instance, along with `init_stub.sh` and the
   uncompressed AWS Java SDK archive. Put these in the home folder of `ec2-user`.
9. `SSH` into the machine.
10. Append the contents of `init_stub.sh` to `/etc/rc.local`.
11. From the AWS management console, right click on your instance and choose 
   "Create Image (EBS AMI)"
12. Once that completes, you'll be able to see your new AMI underneath `IMAGES/AMIs`
   in the AWS Management Console.  Use the AMI ID for `imageID`.

### Pre-made AMI ###
I currently do not have a public version of the AMI I use, since:

1. I would like to test it more for reliability
2. Bundling an AMI for public use is a hassle
3. The AMI bundling guide within the [AWS Documentation](http://docs.amazonwebservices.com/AWSEC2/latest/UserGuide/AESDG-chapter-sharingamis.html) does not make me feel confident that I won't somehow accidentally leak my AWS credentials.

At some point in the future I may release this, but it likely won't be any time soon (if at all).
If you want your own AMI I'd be happy to answer any emails regarding making your own.
Additionally, if there is a demand for an AMI it will encourage me to make a public one.

### Disclaimer ###
There is guarentee that this will work, and there is absolutely no warranty, implied or otherwise.
This code might work exactly as I've layed out here, or it may hang indefinitely doing nothing as
AWS charges you for time.  (At least once this happened due to a bug.)

Personally I check on runs every few hours making sure progress is still being made, 
and I'll even go into the metrics in the AWS console to make sure that CPU usage is still around
100%.  (CPU usage will vary on your specific workloads.)