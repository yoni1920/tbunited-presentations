package SpringAWS.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "aws.s3.folder")
@Configuration
public class S3FolderConfig {
    private String images;
    private String docs;

    public void setImages(String images) {
        this.images = images;
    }

    public String images() {
        return this.images;
    }

    public void setDocs(String docs) {
        this.docs = docs;
    }

    public String docs() {
        return this.docs;
    }
}
