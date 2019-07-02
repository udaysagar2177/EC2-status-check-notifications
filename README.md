# EC2-status-check-notifications

On a medium-large scale deployment, Amazon Web Services (AWS) EC2 instances
status checks could fail more often than expected. When they fail, they usually
get into a Zombie state where the instance is partially connected to the
cluster. Hence, the failed node could still receive requests and clients
can get blocked while waiting for results.

The goal of this application is to get notified about failing EC2 status checks
soon so that we can take appropriate action. Other alternatives to this app:

*  __AWS EC2 status check alarms__:
AWS can notify you when an EC2 instance has a failed status check but the
drawback is that configuration is per instance ID and it can get tedious with
large number of instances and their replacements.

* __AWS Health API__:
You can create AWS CloudWatch events for AWS Health. But the problem is that it
only detects events, it does not warn you about failing status checks. Also,
based on my observations, these notifications can get delayed sometimes.


The configuration of this application is very simple. The application relies on [Serverless](https://serverless.com/).
```
# install serverless
git clone git@github.com:udaysagar2177/EC2-status-check-notifications.git
cd EC2-status-check-notifications/
mvn clean install
# create a bucket in the same region as EC2 instances that you want to monitor
serverless deploy \
  --bucket <bucket-name> \
  --region <aws-region-where-bucket-and-EC2-is-located> \
  --stage <stage-name> \
  --slack_webhook <slack-webook-url>
```


For example:
```
# create a bucket called my-bucket-name in us-east-1, assumes EC2 instances are
# in us-east-1 as well.
serverless deploy \
  --bucket my-bucket-name \
  --region us-east-1 \
  --stage prod \
  --slack_webhook https://hooks.slack.com/services/P3TLWNM7B/V1E7FMJL3/xM7MgPvCqHJEaNgto2ijuplA
```

