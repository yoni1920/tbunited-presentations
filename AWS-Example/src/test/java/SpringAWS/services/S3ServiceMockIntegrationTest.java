package SpringAWS.services;

import SpringAWS.configs.S3FolderConfig;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class S3ServiceMockIntegrationTest {

    @Autowired
    private S3FolderConfig bucketFolders;

    // region Statics & Constants
    public static S3Mock api;
    public static AmazonS3 s3Client;

    public static final int PORT = 8001;
    public static final String TEST_BUCKET = "test-bucket";
    private final static String S3_DIRECTORY = String.format("%s/src/test/java/tmp/s3",
            System.getProperty("user.dir"));
    private final static String FILES_DIRECTORY = String.format("%s/src/test/java/IntegrationFiles",
            System.getProperty("user.dir"));

    // endregion

    // region Init Localhost Endpoint
    @BeforeAll
    public static void init() {
        api = new S3Mock.Builder()
                .withPort(PORT)
                .withFileBackend(S3_DIRECTORY)
                .build();

//        api = new S3Mock.Builder()
//                .withPort(PORT)
//                .withInMemoryBackend()
//                .build();

        api.start();

        EndpointConfiguration endpoint = new EndpointConfiguration(
                "http://localhost:" + PORT,
                "eu-central-1");

        s3Client = AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .build();
    }

    // endregion

    // region Create New Bucket Tests
    @Test
    @Order(1)
    public void createBucket_validName() {
        s3Client.createBucket(TEST_BUCKET);
        List<String> actualBucketNames = s3Client.listBuckets().stream()
                .map(Bucket::getName)
                .collect(Collectors.toList());

        assertTrue(actualBucketNames.contains(TEST_BUCKET));
    }

    @Test
    @Order(1)
    public void createBucket_invalidName_capitalLetters() {
        String bucketName = "test-Bucket";

        assertThrows(IllegalArgumentException.class, () -> s3Client.createBucket(bucketName));
    }

    @Test
    @Order(1)
    public void createBucket_invalidName_characterLengthLimits() {
        String shortName = "hi";
        String longName = "test-bucket-inon-is-not-king-aws-s3-2100-07-13-00-00-00-words-words";

        assertThrows(IllegalArgumentException.class, () -> s3Client.createBucket(shortName));
        assertThrows(IllegalArgumentException.class, () -> s3Client.createBucket(longName));
    }

    @Test
    @Order(1)
    public void createBucket_invalidName_underscores() {
        String bucketName = "test_bucket";

        assertThrows(IllegalArgumentException.class, () -> s3Client.createBucket(bucketName));
    }

    @Test
    @Order(1)
    public void createBucket_invalidName_endsWithDash() {
        String bucketName = "test-bucket-";

        assertThrows(IllegalArgumentException.class, () -> s3Client.createBucket(bucketName));
    }

    @Test
    @Order(1)
    public void createBucket_invalidName_periods() {
        String adjPeriods = "test..Bucket";
        String dashesPeriods = "test-.Bucket";

        assertThrows(IllegalArgumentException.class, () -> s3Client.createBucket(adjPeriods));
        assertThrows(IllegalArgumentException.class, () -> s3Client.createBucket(dashesPeriods));
    }
    // endregion

    // region Check if Bucket Exists
    @Test
    @Order(2)
    public void checkBucketIsFound_doesBucketExist() {
        boolean actualBucketFeedback = s3Client.doesBucketExistV2(TEST_BUCKET);

        assertTrue(actualBucketFeedback);
    }

    @Test
    @Order(2)
    public void checkBucketIsFound_headBucketAPI() {
        HeadBucketRequest headBucketRequest = new HeadBucketRequest(TEST_BUCKET);

        assertDoesNotThrow(() -> s3Client.headBucket(headBucketRequest));
    }
    // endregion

    // region Put New Object Default Settings

    @Test
    @Order(3)
    public void putObject_fromFileDefault() {
        final String expectedName = "the-rock-reg-file.jpg";
        final File expectedImage = new File(String.format("%s/%s", FILES_DIRECTORY, expectedName));
        final String expectedKey = this.bucketFolders.images() + "/the-rock-file-default.jpg";

        s3Client.putObject(TEST_BUCKET, expectedKey, expectedImage);

        final boolean actualFileFeedback = s3Client.doesObjectExist(TEST_BUCKET, expectedKey);

        assertTrue(actualFileFeedback);
    }

    @Test
    @Order(3)
    public void putObject_fromInputStreamDefault() {
        final String expectedName = "the-rock-reg-file.jpg";
        final File expectedImage = new File(String.format("%s/%s", FILES_DIRECTORY, expectedName));
        final String expectedKey = this.bucketFolders.images() + "/the-rock-input-stream.jpg";

        try (InputStream inputStream = new FileInputStream(expectedImage)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata("creator", "inon");
            metadata.addUserMetadata("creating-system", "oren-bank");

            s3Client.putObject(TEST_BUCKET, expectedKey, inputStream, metadata);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        final boolean actualFileFeedback = s3Client.doesObjectExist(TEST_BUCKET, expectedKey);

        assertTrue(actualFileFeedback);
    }

    @Test
    @Order(3)
    public void putObject_nonImageDefault() {
        final String expectedName = "Splunk.docx";
        final File expectedFile = new File(String.format("%s/%s", FILES_DIRECTORY, expectedName));
        final String expectedKey = this.bucketFolders.docs() + "/" + expectedFile.getName();

        PutObjectRequest request = new PutObjectRequest(TEST_BUCKET, expectedKey, expectedFile);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("creator", "moter");
        metadata.addUserMetadata("location", "Jerusalem");
        request.setMetadata(metadata);

        s3Client.putObject(request);
        final boolean actualFileFeedback = s3Client.doesObjectExist(TEST_BUCKET, expectedKey);

        assertTrue(actualFileFeedback);
    }

    @Test
    @Order(3)
    public void putObject_fromBase64Default() {
        final String expectedName = "terminator.jpg";
        final String base64Data = "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";
        byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
        String filePath = String.format("%s/%s", FILES_DIRECTORY, expectedName);

        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            outputStream.write(decodedBytes);

            final File expectedFile = new File(filePath);
            final String expectedKey = this.bucketFolders.images() + "/" + expectedFile.getName();

            s3Client.putObject(TEST_BUCKET, expectedKey, expectedFile);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    // endregion

    // region Remove All Objects in Bucket
    @Test
    @Order(4)
    public void removeAllObjects_deleteObjectsRequest_withKeyList() {
        List<DeleteObjectsRequest.KeyVersion> objectKeys = s3Client.listObjects(TEST_BUCKET)
                .getObjectSummaries()
                .stream()
                .map(obj -> new DeleteObjectsRequest.KeyVersion(obj.getKey()))
                .collect(Collectors.toList());

        DeleteObjectsRequest request = new DeleteObjectsRequest(TEST_BUCKET).withKeys(objectKeys);
        s3Client.deleteObjects(request);

        List<S3ObjectSummary> actualObjects = s3Client.listObjects(TEST_BUCKET).getObjectSummaries();

        assertTrue(actualObjects.isEmpty());
    }
    // endregion

    // region Delete Bucket & Check Bucket is Not Found
    @Test
    @Order(5)
    public void deleteBucket_deletesBucketSuccessfully() {
        s3Client.deleteBucket(TEST_BUCKET);
        boolean actualBucketFeedback = s3Client.doesBucketExistV2(TEST_BUCKET);

        assertFalse(actualBucketFeedback);
    }

    @Test
    @Order(6)
    public void checkBucketIsNotFound_headBucketAPI() {
        HeadBucketRequest headBucketRequest = new HeadBucketRequest(TEST_BUCKET);

        assertThrows(AmazonS3Exception.class, () -> s3Client.headBucket(headBucketRequest));
    }

    @Test
    @Order(6)
    public void checkBucketIsNotFound_listBuckets() {
        List<String> actualBucketNames = s3Client.listBuckets().stream()
                .map(Bucket::getName)
                .collect(Collectors.toList());

        assertFalse(actualBucketNames.contains(TEST_BUCKET));
    }
    // endregion

    // region Shutdown API
    @AfterAll
    public static void tearDown() {
        api.shutdown();
    }
    // endregion
}
