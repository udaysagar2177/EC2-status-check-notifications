package io.github.udaysagar2177.ec2StatusChecks.database;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


import io.github.udaysagar2177.ec2StatusChecks.model.SerializableInstanceStatus;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.model.InstanceStatus;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Stores the {@link InstanceStatus}s in a file on AWS S3.
 *
 * @author uday
 */
public class InstanceStatusDBOnS3 implements InstanceStatusDB {
    private static final Logger LOG = LogManager.getLogger(InstanceStatusDBOnS3.class);
    private static final TypeReference<HashMap<String, SerializableInstanceStatus>>
            DATA_MAP_TYPE_REFERENCE = new TypeReference<HashMap<String, SerializableInstanceStatus>>() {
    };
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    }

    private final S3Client s3Client;
    private final String bucket;
    private final String dbFileOnS3;

    private Map<String, SerializableInstanceStatus> data;

    public InstanceStatusDBOnS3(String region, String bucket, String dbFileOnS3) {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .build();
        this.bucket = bucket;
        this.dbFileOnS3 = dbFileOnS3;
    }

    public SerializableInstanceStatus get(String instanceId) {
        return this.data.get(instanceId);
    }

    public void set(String instanceId, SerializableInstanceStatus instanceStatus) {
        this.data.put(instanceId, instanceStatus);
    }

    public Map<String, SerializableInstanceStatus> getAll() {
        return data;
    }

    public void clear() {
        this.data.clear();
    }

    public void save() {
        try {
            String dataString = MAPPER.writeValueAsString(this.data);
            LOG.debug("Saving data {}", dataString);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(this.dbFileOnS3)
                    .build();
            this.s3Client.putObject(putObjectRequest, RequestBody.fromString(dataString));
        } catch (Exception e) {
            LOG.error("Unable to save object {}", this.data, e);
            throw new RuntimeException(e);
        }
    }

    public int getCount() {
        return this.data.size();
    }

    public void load() {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(this.dbFileOnS3)
                    .build();
            byte[] bytes = this.s3Client.getObjectAsBytes(getObjectRequest)
                    .asByteArray();
            Map<String, SerializableInstanceStatus> dataMap = MAPPER
                    .readValue(bytes, DATA_MAP_TYPE_REFERENCE);
            LOG.info("Loaded {} items from db", dataMap.size());
            LOG.debug("Data from db is {}", dataMap);
            data = dataMap;
        } catch (Exception var4) {
            LOG.error("key {}/{} not found!", this.bucket, this.dbFileOnS3, var4);
            data = new HashMap<>();
        }
    }
}
