package SpringAWS.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.services.s3.model.lifecycle.LifecycleAndOperator;
import com.amazonaws.services.s3.model.lifecycle.LifecycleFilter;
import com.amazonaws.services.s3.model.lifecycle.LifecyclePrefixPredicate;
import com.amazonaws.services.s3.model.lifecycle.LifecycleTagPredicate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static com.amazonaws.services.s3.model.BucketLifecycleConfiguration.Transition;

public class LifecycleConfiguration {

    @Autowired
    private AmazonS3 s3Client;

    public void demo() {
        String bucketName = "test-bucket";

        // Create a rule to archive objects with the "glacierobjects/" prefix to Glacier immediately.
        BucketLifecycleConfiguration.Rule rule1 = new BucketLifecycleConfiguration.Rule()
                .withId("Archive immediately rule")
                .withFilter(new LifecycleFilter(new LifecyclePrefixPredicate("glacierobjects/")))
                .addTransition(new Transition().withDays(0).withStorageClass(StorageClass.Glacier))
                .withStatus(BucketLifecycleConfiguration.ENABLED);

        // Create a rule to transition objects to the Standard-Infrequent Access storage class
        // after 30 days, then to Glacier after 365 days. Amazon S3 will delete the objects after 3650 days.
        // The rule applies to all objects with the tag "archive" set to "true".
        BucketLifecycleConfiguration.Rule rule2 = new BucketLifecycleConfiguration.Rule()
                .withId("Archive and then delete rule")
                .withFilter(new LifecycleFilter(new LifecycleTagPredicate(new Tag("archive", "true"))))
                .addTransition(new Transition().withDays(30).withStorageClass(StorageClass.StandardInfrequentAccess))
                .addTransition(new Transition().withDays(365).withStorageClass(StorageClass.Glacier))
                .withExpirationInDays(3650)
                .withStatus(BucketLifecycleConfiguration.ENABLED);

        // Add the rules to a new BucketLifecycleConfiguration.
        BucketLifecycleConfiguration configuration = new BucketLifecycleConfiguration()
                .withRules(Arrays.asList(rule1, rule2));

        // Save the configuration.
        s3Client.setBucketLifecycleConfiguration(bucketName, configuration);

        // Retrieve the configuration.
        configuration = s3Client.getBucketLifecycleConfiguration(bucketName);

        // Add a new rule with both a prefix predicate and a tag predicate.
        configuration.getRules().add(new BucketLifecycleConfiguration.Rule().withId("NewRule")
                .withFilter(new LifecycleFilter(new LifecycleAndOperator(
                        Arrays.asList(new LifecyclePrefixPredicate("YearlyDocuments/"),
                                new LifecycleTagPredicate(new Tag("expire_after", "ten_years"))))))
                .withExpirationInDays(3650)
                .withStatus(BucketLifecycleConfiguration.ENABLED));

        // Save the configuration.
        s3Client.setBucketLifecycleConfiguration(bucketName, configuration);
    }
}
