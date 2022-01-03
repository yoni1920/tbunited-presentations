package SpringAWS.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
@Accessors(fluent = true)
public class S3Service {

    @Autowired
    private static AmazonS3 s3Client;

//    public List<String> getAllBucketNames() {
//        return this.s3Client.listBuckets().stream()
//                .map(Bucket::getName)
//                .collect(Collectors.toList());
//    }
//
//    public void createBucket(String bucketName) {
//        if (s3Client.doesBucketExist(bucketName)) {
//            throw new RuntimeException(String.format("Bucket with name '%s', already exists", bucketName));
//        }
//
//        s3Client.createBucket(bucketName);
//    }
}
