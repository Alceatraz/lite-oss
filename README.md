# Lite-OSS

```text
#==== LiteOSS ==================================================================
# A cURL and media library friendly object storage service
#===============================================================================
>> STARTUP SETTING - Use ENV then System Property then Default
OSS_SERVER_HOST/server.host = 0.0.0.0 ------------ Ktor bind config
OSS_SERVER_PORT/server.port = 80 ----------------- Ktor bind config
OSS_SCHEMA_BLOB/schema.blob = schema/blob -------- Path for data file
OSS_SCHEMA_DATA/schema.data = schema/data -------- Path for future usage
OSS_SCHEMA_LINK/schema.link = schema/link -------- Path for symbol links
OSS_SCHEMA_TEMP/schema.temp = schema/temp -------- Path for uploading file
>> WARNING: Link file use relative path, Which means:
1. Schema path can't change anymore OR ALL LINK'S TARGET MISSING
2. Container mount must as same as the host, For example:
/photoprism/originals/liteoss/blob:/foo/bar/schema/blob
/photoprism/originals/liteoss/link:/foo/bar/schema/link
#===============================================================================
>> ADMIN API
GET  /api/blob/size --------------- Calculate total blob size
GET  /api/blob/list --------------- List all blob name and size, Very Long !!!
GET  /api/link/size --------------- List all blob name and target, Very Long !!!
POST /api/blob/list --------------- Delete all link which target file not exist
#===============================================================================
# FILE MANAGE API
PUT     /oss/blob --------------------- Upload file (-d binary, Req body)
DELETE  /oss/blob/{hash} -------------- Delete file
HEAD    /oss/blob/{hash} -------------- Check is file exists
GET     /oss/blob/{hash} -------------- Download file content
#===============================================================================
>> BLOB MANAGE API
PUT     /oss/blob --------------------- Upload file (-d binary, Req body)
DELETE  /oss/blob/{hash} -------------- Delete file
HEAD    /oss/blob/{hash} -------------- Check is file exists
GET     /oss/blob/{hash} -------------- Download file content
#===============================================================================
>> LINK MANAGE API
PUT    /oss/link/{name} -------------- Create link to blob (-d hash, Req body)
DELETE /oss/link/{hash} -------------- Delete link
HEAD   /oss/link/{hash} -------------- Check is link exist
GET    /oss/link/{hash} -------------- Get target file name
#===============================================================================
>> DATA MANAGE API
PUT     /oss/data/{name...} ---------- Upload file just override
POST    /oss/data/{name...} ---------- Upload file if not exist 
DELETE  /oss/data/{name...} ---------- Delete file
HEAD    /oss/data/{name...} ---------- Check is file exists
GET     /oss/data/{name...} ---------- Download file content
#===============================================================================
# By Alceatraz Warprays with Ktor and GraalVM
#===============================================================================
```

# Q&A

- Q: What mean media library friendly
- A: Let's face the elephant in the room: photoprism/immich refuse use file signature to index images, The only thing they care is file-extension in file name `foo-bar.jpg`, But with OSS service, A blob file should named as it's hash value like `36a9e7f1c95b82ffb99743e0c5c4ce95d83c9a430aac59f84ef3cbfab6145068`. So we have the link feature.


- Q: Why /oss/data not share data with /oss/blob
- A: LiteOSS has two goals: cURL friendly and media library friendly.
    - /oss/data for cURL, Keep file name and folder structure
    - /oss/blob and /oss/link for library index. Unify all file into hash


- Q: Where is the docker image
- A: Here it is [DockerHUB](https://hub.docker.com/repository/docker/zwischenspiell/liteoss), And a demo:

```shell
docker run \
--deamon \
--restart always \
--publish 8080:8080 \
--volume /scheam/data:/foo/bar/storage \
--volume /scheam/blob:/foo/bar/media/blob \
--volume /scheam/link:/foo/bar/media/link \
--env OSS_SERVER_PORT=8080 \
zwischenspiell/liteoss:latest
```

- Q: Why not compatible AmazonS3?
- A: S3 API is not even close with friendly, Neither cURL or library.
  And: In cause you have business requirement, Please use [Minio](https://min.io/)


